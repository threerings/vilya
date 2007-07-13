//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.whirled.server;

import java.util.ArrayList;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.Invoker;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.whirled.Log;
import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.data.ScenedBodyObject;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.util.SceneFactory;
import com.threerings.whirled.util.UpdateList;

/**
 * The scene registry is responsible for the management of all scenes. It handles interaction with
 * the scene repository and ensures that scenes are loaded into memory when needed and flushed from
 * memory when not needed.
 *
 * <p> The scene repository also takes care of bridging from the blocking, synchronous world of the
 * scene repository to the non-blocking asynchronous world of the distributed object event
 * queue. Thus its interfaces for accessing scenes are structured so as to not block the dobjmgr
 * thread while waiting for scenes to be read from or written to the repository.
 *
 * <p><em>Note:</em> All access to the scene registry should take place from the dobjmgr thread.
 */
public class SceneRegistry
    implements SceneCodes, SceneProvider
{
    /**
     * Used to create {@link PlaceConfig} instances for scenes.
     */
    public static interface ConfigFactory
    {
        /**
         * Creates the place config instance appropriate to the specified scene.
         */
        PlaceConfig createPlaceConfig (SceneModel model);
    }

    /**
     * Because scenes must be loaded from the scene repository and this must not be done on the
     * dobjmgr thread, the interface for resolving scenes requires that the entity that wishes for
     * a scene to be resolved implement this callback interface so that it can be notified when a
     * scene has been loaded and initialized.
     */
    public static interface ResolutionListener
    {
        /**
         * Called when the scene has been successfully resolved. The scene manager instance
         * provided can be used to obtain a reference to the scene, or the scene distributed
         * object.
         */
        public void sceneWasResolved (SceneManager scmgr);

        /**
         * Called if some failure occurred in the scene resolution process.
         */
        public void sceneFailedToResolve (int sceneId, Exception reason);
    }

    /**
     * Constructs a scene registry, instructing it to load and store scenes using the supplied
     * scene repository.
     */
    public SceneRegistry (InvocationManager invmgr, SceneRepository screp,
                          SceneFactory scfact, ConfigFactory confact)
    {
        _screp = screp;
        _scfact = scfact;
        _confact = confact;

        // register our scene service
        invmgr.registerDispatcher(new SceneDispatcher(this), SceneCodes.WHIRLED_GROUP);
    }

    /**
     * Fetches the scene manager assosciated with the specified scene.
     *
     * @return the scene manager for the specified scene or null if no scene manager is loaded for
     * that scene.
     */
    public SceneManager getSceneManager (int sceneId)
    {
        return (SceneManager)_scenemgrs.get(sceneId);
    }

    /**
     * Returns a reference to the scene repository in use by this registry.
     */
    public SceneRepository getSceneRepository ()
    {
        return _screp;
    }

    /**
     * Returns {@link SceneManager#where} for the specified scene or <code>null:sceneId</code> if
     * no scene manager exists for that scene.
     */
    public String where (int sceneId)
    {
        SceneManager scmgr = getSceneManager(sceneId);
        return (scmgr == null) ? ("null:" + sceneId) : scmgr.where();
    }

    /**
     * Requests that the specified scene be resolved, which means loaded into the server and
     * initialized if the scene is not currently active. The supplied callback instance will be
     * notified, on the dobjmgr thread, when the scene has been resolved. If the scene is already
     * active, it will be notified immediately (before the call to {@link #resolveScene} returns).
     *
     * @param sceneId the id of the scene to resolve.
     * @param target a reference to a callback instance that will be notified when the scene has
     * been resolved (which may be immediately if the scene is already active).
     */
    public void resolveScene (int sceneId, ResolutionListener target)
    {
        SceneManager mgr = (SceneManager)_scenemgrs.get(sceneId);
        if (mgr != null) {
            // if the scene is already resolved, we're ready to roll
            target.sceneWasResolved(mgr);
            return;
        }

        if (Log.debug()) {
            Log.debug("Resolving scene [id=" + sceneId + "].");
        }

        // otherwise we've got to resolve the scene and call them back later; we can manipulate the
        // penders table with impunity here because we only do so on the dobjmgr thread
        ArrayList penders = (ArrayList)_penders.get(sceneId);

        // if we're already in the process of resolving this scene, just add these guys to the list
        // to be notified when it finally is resolved
        if (penders != null) {
            penders.add(target);

        } else {
            // otherwise we've got to initiate the resolution process, create the penders list
            _penders.put(sceneId, penders = new ArrayList());
            penders.add(target);

            // i don't like cluttering up method declarations with final keywords...
            final int fsceneId = sceneId;

            if (Log.debug()) {
                Log.debug("Invoking scene lookup [id=" + sceneId + "].");
            }

            // then we queue up an execution unit that'll load the scene and initialize it etc.
            WhirledServer.invoker.postUnit(new Invoker.Unit() {
                // this is run on the invoker thread
                public boolean invoke () {
                    try {
                        _model = _screp.loadSceneModel(fsceneId);
                        _updates = _screp.loadUpdates(fsceneId);
                    } catch (Exception e) {
                        _cause = e;
                    }
                    return true;
                }

                // this is run on the dobjmgr thread
                public void handleResult () {
                    if (_cause != null) {
                        processFailedResolution(fsceneId, _cause);
                    } else if (_model != null) {
                        processSuccessfulResolution(_model, _updates);
                    } else {
                        Log.warning("Scene loading unit finished with neither a scene nor a " +
                                    "reason for failure!?");
                    }
                }

                public String toString () {
                    return "SceneRegistry.SceneLoader " + (_model == null ? "" : _model.name) +
                        "(" + fsceneId + ")";
                }

                protected SceneModel _model;
                protected UpdateList _updates;
                protected Exception _cause;
            });
        }
    }

    // from interface SceneService
    public void moveTo (ClientObject caller, int sceneId, final int sceneVer,
                        final SceneService.SceneMoveListener listener)
    {
        final BodyObject source = (BodyObject)caller;

        // create a callback object to handle the resolution or failed resolution of the scene
        SceneRegistry.ResolutionListener rl = null;
        rl = new SceneRegistry.ResolutionListener() {
            public void sceneWasResolved (SceneManager scmgr) {
                // make sure our caller is still around; under heavy load, clients might end their
                // session while the scene is resolving
                if (!source.isActive()) {
                    Log.info("Abandoning scene move, client gone [who=" + source.who()  +
                             ", dest=" + scmgr.where() + "].");
                    InvocationMarshaller.setNoResponse(listener);
                    return;
                }
                finishMoveToRequest(source, scmgr, sceneVer, listener);
            }

            public void sceneFailedToResolve (int rsceneId, Exception reason) {
                Log.warning("Unable to resolve scene [sceneid=" + rsceneId +
                            ", reason=" + reason + "].");
                // pretend like the scene doesn't exist to the client
                listener.requestFailed(NO_SUCH_PLACE);
            }
        };

        // make sure the scene they are headed to is actually loaded into the server
        resolveScene(sceneId, rl);
    }

    /**
     * Moves the supplied body into the supplied (already resolved) scene and informs the supplied
     * listener if the move is successful.
     *
     * @exception InvocationException thrown if a failure occurs attempting to move the user into
     * the place associated with the scene.
     */
    public void effectSceneMove (BodyObject source, SceneManager scmgr,
                                 int sceneVersion, SceneService.SceneMoveListener listener)
        throws InvocationException
    {
        // move to the place object associated with this scene
        int ploid = scmgr.getPlaceObject().getOid();
        PlaceConfig config = CrowdServer.plreg.locprov.moveTo(source, ploid);

        // now that we've finally moved, we can update the user object with the new scene id
        ((ScenedBodyObject)source).setSceneId(scmgr.getScene().getId());

        // check to see if they need a newer version of the scene data
        SceneModel model = scmgr.getScene().getSceneModel();
        if (sceneVersion != model.version) {
            SceneUpdate[] updates = null;
            if (sceneVersion < model.version) {
                // try getting updates to bring the client to the right version
                updates = scmgr.getUpdates(sceneVersion);
            }
            if (updates != null) {
                listener.moveSucceededWithUpdates(ploid, config, updates);
            } else {
                listener.moveSucceededWithScene(ploid, config, model);
            }
        } else {
            listener.moveSucceeded(ploid, config);
        }
    }

    /**
     * Ejects the specified body from their current scene and sends them a request to move to the
     * specified new scene. This is the scene-equivalent to {@link LocationProvider#moveBody}.
     */
    public void moveBody (BodyObject source, int sceneId)
    {
        // first remove them from their old place
        CrowdServer.plreg.locprov.leaveOccupiedPlace(source);

        // then send a forced move notification
        SceneSender.forcedMove(source, sceneId);
    }

    /**
     * Ejects the specified body from their current scene and zone. This is the zone equivalent to
     * {@link LocationProvider#leaveOccupiedPlace}.
     */
    public void leaveOccupiedScene (ScenedBodyObject source)
    {
        // remove them from their occupied place
        CrowdServer.plreg.locprov.leaveOccupiedPlace((BodyObject)source);

        // and clear out their scene information
        source.setSceneId(0);
    }

    /**
     * This is called after the scene to which we are moving is guaranteed to have been loaded into
     * the server.
     */
    protected void finishMoveToRequest (BodyObject source, SceneManager scmgr, int sceneVersion,
                                        SceneService.SceneMoveListener listener)
    {
        try {
            effectSceneMove(source, scmgr, sceneVersion, listener);

        } catch (InvocationException sfe) {
            listener.requestFailed(sfe.getMessage());

        } catch (RuntimeException re) {
            Log.logStackTrace(re);
            listener.requestFailed(INTERNAL_ERROR);
        }
    }

    /**
     * Called when the scene resolution has completed successfully.
     */
    protected void processSuccessfulResolution (SceneModel model, final UpdateList updates)
    {
        // now that the scene is loaded, we can create a scene manager for it. that will be
        // initialized by the place registry and when that is finally complete, then we can let our
        // penders know what's up

        try {
            // first create our scene instance
            final Scene scene = _scfact.createScene(model, _confact.createPlaceConfig(model));

            // now create our scene manager
            CrowdServer.plreg.createPlace(
                scene.getPlaceConfig(), new PlaceRegistry.PreStartupHook() {
                public void invoke (PlaceManager pmgr) {
                    ((SceneManager)pmgr).setSceneData(scene, updates, SceneRegistry.this);
                }
            });

            // when the scene manager completes its startup procedings, it will call back to the
            // scene registry and let us know that we can turn the penders loose

        } catch (Exception e) {
            // so close, but no cigar
            processFailedResolution(model.sceneId, e);
        }
    }

    /**
     * Called if resolving the scene fails for some reason.
     */
    protected void processFailedResolution (int sceneId, Exception cause)
    {
        Log.info("Failed to resolve scene [sceneId=" + sceneId + ", cause=" + cause + "].");
        Log.logStackTrace(cause);

        // alas things didn't work out, notify our penders
        ArrayList penders = (ArrayList)_penders.remove(sceneId);
        if (penders != null) {
            for (int i = 0; i < penders.size(); i++) {
                ResolutionListener rl = (ResolutionListener)penders.get(i);
                try {
                    rl.sceneFailedToResolve(sceneId, cause);
                } catch (Exception e) {
                    Log.warning("Resolution listener choked.");
                    Log.logStackTrace(e);
                }
            }
        }
    }

    /**
     * Called by the scene manager once it has started up (meaning that it has its place object and
     * is ready to roll).
     */
    protected void sceneManagerDidStart (SceneManager scmgr)
    {
        // register this scene manager in our table
        int sceneId = scmgr.getScene().getId();
        _scenemgrs.put(sceneId, scmgr);

        if (Log.debug()) {
            Log.debug("Registering scene manager [scid=" + sceneId + ", scmgr=" + scmgr + "].");
        }

        // now notify any penders
        ArrayList penders = (ArrayList)_penders.remove(sceneId);
        if (penders != null) {
            for (int i = 0; i < penders.size(); i++) {
                ResolutionListener rl = (ResolutionListener)penders.get(i);
                try {
                    rl.sceneWasResolved(scmgr);
                } catch (Exception e) {
                    Log.warning("Resolution listener choked.");
                    Log.logStackTrace(e);
                }
            }
        }
    }

    /**
     * Called by the scene manager when it is shut down.
     */
    protected void unmapSceneManager (SceneManager scmgr)
    {
        if (_scenemgrs.remove(scmgr.getScene().getId()) == null) {
            Log.warning("Requested to unmap unmapped scene manager [scmgr=" + scmgr + "].");
            return;
        }

        if (Log.debug()) {
            Log.debug("Unmapped scene manager " + scmgr + ".");
        }
    }

    /** The entity from which we load scene models. */
    protected SceneRepository _screp;

    /** Used to generate place configs for our scenes. */
    protected ConfigFactory _confact;

    /** The entity via which we create scene instances from scene models. */
    protected SceneFactory _scfact;

    /** A mapping from scene ids to scene managers. */
    protected HashIntMap _scenemgrs = new HashIntMap();

    /** The table of pending resolution listeners. */
    protected HashIntMap _penders = new HashIntMap();
}
