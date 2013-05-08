//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.Invoker;

import com.samskivert.jdbc.RepositoryUnit;

import com.threerings.presents.annotation.MainInvoker;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.BodyLocator;
import com.threerings.crowd.server.LocationManager;
import com.threerings.crowd.server.LocationProvider;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneMarshaller;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.util.NoSuchSceneException;
import com.threerings.whirled.util.SceneFactory;
import com.threerings.whirled.util.UpdateList;

import static com.threerings.whirled.Log.log;

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
@Singleton
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
     * Constructs a scene registry.
     */
    @Inject public SceneRegistry (InvocationManager invmgr)
    {
        // register our scene service
        invmgr.registerProvider(this, SceneMarshaller.class, SceneCodes.WHIRLED_GROUP);
    }

    /**
     * Fetches the scene manager associated with the specified scene.
     *
     * @return the scene manager for the specified scene or null if no scene manager is loaded for
     * that scene.
     */
    public SceneManager getSceneManager (int sceneId)
    {
        return _scenemgrs.get(sceneId);
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
        SceneManager mgr = _scenemgrs.get(sceneId);
        if (mgr != null) {
            // the scene is already resolved, we're ready to roll
            target.sceneWasResolved(mgr);
            return;
        }

        // if the scene is already being resolved, we need do no more
        if (!addResolutionListener(sceneId, target)) {
            return;
        }

        // otherwise we have to load the scene from the repository
        final int fsceneId = sceneId;
        _invoker.postUnit(new RepositoryUnit("resolveScene(" + sceneId + ")") {
            @Override public void invokePersist () throws Exception {
                _model = _screp.loadSceneModel(fsceneId);
                _updates = _screp.loadUpdates(fsceneId);
                _extras = _screp.loadExtras(fsceneId, _model);
            }
            @Override public void handleSuccess () {
                processSuccessfulResolution(_model, _updates, _extras);
            }
            @Override public void handleFailure (Exception error) {
                processFailedResolution(fsceneId, error);
            }
            protected SceneModel _model;
            protected UpdateList _updates;
            protected Object _extras;
        });
    }

    // from interface SceneService
    public void moveTo (ClientObject caller, int sceneId, int sceneVer,
                        SceneService.SceneMoveListener listener)
    {
        BodyObject body = _locator.forClient(caller);
        resolveScene(sceneId, new SceneMoveHandler(_locman, body, sceneVer, listener));
    }

    /**
     * Ejects the specified body from their current scene and sends them a request to move to the
     * specified new scene. This is the scene-equivalent to {@link LocationProvider#moveTo}.
     */
    public void moveBody (BodyObject source, int sceneId)
    {
        // first remove them from their old place
        _locman.leaveOccupiedPlace(source);

        // then send a forced move notification to their client object
        SceneSender.forcedMove(source.getClientObject(), sceneId);
    }

    /**
     * Ejects the specified body from their current scene and zone. This is the zone equivalent to
     * {@link LocationProvider#leavePlace}.
     */
    public void leaveOccupiedScene (BodyObject source)
    {
        // remove them from their occupied place (clears out scene info as well)
        _locman.leaveOccupiedPlace(source);
    }

    /**
     * Adds a callback for when the scene is resolved. Returns true if this is the first such
     * thing (and thusly, the caller should actually fire off scene resolution) or false if we've
     * already got a list and have just added this listener to it.
     */
    protected boolean addResolutionListener (int sceneId, ResolutionListener rl)
    {
        List<ResolutionListener> penders = _penders.get(sceneId);
        boolean newList = false;

        if (penders == null) {
            _penders.put(sceneId, penders = Lists.newArrayList());
            newList = true;
        }

        penders.add(rl);
        return newList;
    }

    /**
     * Called when the scene resolution has completed successfully.
     */
    protected void processSuccessfulResolution (
        SceneModel model, final UpdateList updates, final Object extras)
    {
        // now that the scene is loaded, we can create a scene manager for it. that will be
        // initialized by the place registry and when that is finally complete, then we can let our
        // penders know what's up

        try {
            // first create our scene instance
            final Scene scene = _scfact.createScene(model, _confact.createPlaceConfig(model));

            // now create our scene manager
            _plreg.createPlace(scene.getPlaceConfig(), new PlaceRegistry.PreStartupHook() {
                public void invoke (PlaceManager pmgr) {
                    ((SceneManager)pmgr).setSceneData(scene, updates, extras, SceneRegistry.this);
                }
            });

            // when the scene manager completes its startup proceedings, it will call back to the
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
        // if this is not simply a missing scene, log a warning
        if (!(cause instanceof NoSuchSceneException)) {
            log.info("Failed to resolve scene [sceneId=" + sceneId + "].", cause);
        }

        // alas things didn't work out, notify our penders
        List<ResolutionListener> penders = _penders.remove(sceneId);
        if (penders != null) {
            for (ResolutionListener rl : penders) {
                try {
                    rl.sceneFailedToResolve(sceneId, cause);
                } catch (Exception e) {
                    log.warning("Resolution listener choked.", e);
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

        log.debug("Registering scene manager", "scid", sceneId, "scmgr", scmgr);

        // now notify any penders
        List<ResolutionListener> penders = _penders.remove(sceneId);
        if (penders != null) {
            for (ResolutionListener rl : penders) {
                try {
                    rl.sceneWasResolved(scmgr);
                } catch (Exception e) {
                    log.warning("Resolution listener choked.", e);
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
            log.warning("Requested to unmap unmapped scene manager [scmgr=" + scmgr + "].");
            return;
        }

        log.debug("Unmapped scene manager", "scmgr", scmgr);
    }

    /** The entity from which we load scene models. */
    @Inject protected SceneRepository _screp;

    /** Used to generate place configs for our scenes. */
    @Inject protected ConfigFactory _confact;

    /** The entity via which we create scene instances from scene models. */
    @Inject protected SceneFactory _scfact;

    /** The invoker on which we do database operations. */
    @Inject protected @MainInvoker Invoker _invoker;

    /** Used to translate ClientObjects into BodyObjects. */
    @Inject protected BodyLocator _locator;

    /** Provides access to place managers. */
    @Inject protected PlaceRegistry _plreg;

    /** Provides location services. */
    @Inject protected LocationManager _locman;

    /** A mapping from scene ids to scene managers. */
    protected IntMap<SceneManager> _scenemgrs = IntMaps.newHashIntMap();

    /** The table of pending resolution listeners. */
    protected IntMap<List<ResolutionListener>> _penders = IntMaps.newHashIntMap();
}
