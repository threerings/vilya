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

import com.google.inject.Inject;

import com.samskivert.util.Invoker;

import com.samskivert.jdbc.WriteOnlyUnit;

import com.threerings.presents.annotation.MainInvoker;

import com.threerings.crowd.data.Place;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.ScenePlace;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.UpdateList;

import static com.threerings.whirled.Log.log;

/**
 * The scene manager extends the place manager and takes care of basic scene services. Presently
 * that is little more than registering the scene manager with the scene registry so that the
 * manager can be looked up by scene id in addition to place object id.
 */
public class SceneManager extends PlaceManager
{
    /**
     * Returns the scene object (not the scene distributed object) being managed by this scene
     * manager.
     */
    public Scene getScene ()
    {
        return _scene;
    }

    /**
     * Returns {@link UpdateList#getUpdates} for this scene's updates.
     */
    public SceneUpdate[] getUpdates (int fromVersion)
    {
        return _updates.getUpdates(fromVersion);
    }

    @Override
    public Place getLocation ()
    {
        return new ScenePlace(_plobj.getOid(), _scene.getId());
    }

    /**
     * Returns true if this scene stores data in the database, or false if it's instantiated anew
     * with each server restart
     */
    public boolean isPersistent ()
    {
        return true;
    }

    /**
     * Called by the scene registry once the scene manager has been created (and initialized), but
     * before it is started up.
     */
    protected void setSceneData (Scene scene, UpdateList updates, Object extras, SceneRegistry screg)
    {
        _scene = scene;
        _screg = screg;
        _updates = updates;

        // make sure the list and our version of the scene are in accordance
        if (!_updates.validate(scene.getVersion())) {
            log.warning("Provided with invalid updates; flushing [where=" + where() +
                        ", sceneId=" + scene.getId() + ", version=" + scene.getVersion() + "].");
            // clear out the update list as it will not allow us to bring clients up to date with
            // our current scene version; instead they'll have to download the whole thing
            _updates = new UpdateList();
        }

        // let derived classes react to the receipt of scene data
        gotSceneData(extras);
    }

    /**
     * A method that can be overridden by derived classes to perform initialization processing
     * after we receive our scene information but before we're started up (and hence registered as
     * an active place).
     *
     * @param extras optional additional information supplied by the repository when the scene was
     * loaded, or null if the repository provided no extras.
     */
    protected void gotSceneData (Object extras)
    {
    }

    /**
     * We're fully ready to go, so now we register ourselves with the scene registry which will
     * make us available to the clients and system at large.
     */
    @Override
    protected void didStartup ()
    {
        super.didStartup();

        // Wait until us and all of our subclasses have completely finished running didStartup
        // prior to registering the scene as being ready.
        _omgr.postRunnable(new Runnable() {
            public void run () {
                _screg.sceneManagerDidStart(SceneManager.this);
            }
        });
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();

        // unregister ourselves with the scene registry
        _screg.unmapSceneManager(this);
    }

    /**
     * When a modification is made to a scene, the scene manager should create a SceneUpdate
     * instance and pass it to this method which will update the in-memory scene, and apply and
     * record the update in the scene repository.
     *
     * <p> This update will be stored persistently and provided (along with any other accumulated
     * updates) to clients that later request to enter the scene with an old version of the scene
     * data. Updates are not stored forever, but a sizable number of recent updates are stored so
     * that moderately current clients can apply incremental patches to their scenes rather than
     * redownloading entire scenes when they change.
     */
    protected void recordUpdate (final SceneUpdate update)
    {
        // instruct our in-memory copy of the scene to apply the update
        _scene.updateReceived(update);

        // add it to our in memory update list
        _updates.addUpdate(update);

        // and apply and store it in the repository
        if (isPersistent()) {
            _invoker.postUnit(new WriteOnlyUnit("recordUpdate(" + update + ")") {
                @Override
                public void invokePersist () throws Exception {
                    _screg.getSceneRepository().applyAndRecordUpdate(_scene.getSceneModel(), update);
                }
            });
        }

        // broadcast the update to all occupants of the scene
        _plobj.postMessage(SceneCodes.SCENE_UPDATE, new Object[] { update });
    }

    @Override
    public String where ()
    {
        return _scene.getName() + " (" + super.where() + ":" + _scene.getId() + ")";
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", scene=").append(_scene);
    }

    /** A reference to our scene implementation which provides a meaningful interpretation of the
     * data in the scene model. */
    protected Scene _scene;

    /** A list of the updates tracked for this scene. These will be used to attempt to bring
     * clients up to date efficiently if they request to enter our scene with old scene model
     * data. */
    protected UpdateList _updates;

    /** A reference to the scene registry so that we can call back to it when we're fully
     * initialized. */
    protected SceneRegistry _screg;

    /** The invoker on which we'll do our database operations. */
    @Inject protected @MainInvoker Invoker _invoker;
}
