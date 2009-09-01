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

package com.threerings.whirled.client {

import flash.errors.IOError;
import flash.errors.IllegalOperationError;

import com.threerings.util.Log;
import com.threerings.util.Map;
import com.threerings.util.Maps;
import com.threerings.util.ResultListener;

import com.threerings.io.TypedArray;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.ConfirmAdapter;
import com.threerings.presents.data.InvocationCodes;

import com.threerings.crowd.client.LocationDirector;
import com.threerings.crowd.client.LocationDirector_FailureHandler;
import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneMarshaller;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneObject;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.NoSuchSceneError;
import com.threerings.whirled.util.SceneFactory;
import com.threerings.whirled.util.WhirledContext;

/**
 * The scene director is the client's interface to all things scene related. It interfaces with the
 * scene repository to ensure that scene objects are available when the client enters a particular
 * scene. It handles moving from scene to scene (it coordinates with the {@link LocationDirector}
 * in order to do this).
 *
 * <p> Note that when the scene director is in use instead of the location director, scene ids
 * instead of place oids will be supplied to {@link LocationObserver#locationMayChange} and {@link
 * LocationObserver#locationChangeFailed}.
 */
public class SceneDirector extends BasicDirector
    implements LocationDirector_FailureHandler, SceneReceiver, SceneService_SceneMoveListener,
               LocationObserver
{
    private static const log :Log = Log.getLog(SceneDirector);

    // statically reference classes we require
    SceneMarshaller;

    /**
     * Creates a new scene director with the specified context.
     *
     * @param ctx the active client context.
     * @param locdir the location director in use on the client, with which the scene director will
     * coordinate when changing location.
     * @param screp the entity from which the scene director will load scene data from the local
     * client scene storage. This may be null when the SceneDirector is constructed, but it should
     * be supplied via {@link #setSceneRepository} prior to really using this director.
     * @param fact the factory that knows which derivation of {@link Scene} to create for the
     * current system.
     */
    public function SceneDirector (ctx :WhirledContext, locdir :LocationDirector,
                                   screp :SceneRepository, fact :SceneFactory)
    {
        super(ctx);

        // we'll need these for later
        _wctx = ctx;
        _locdir = locdir;
        setSceneRepository(screp);
        _fact = fact;

        // we need to observe scene moves so that we can clear out if we change to a non-scene
        _locdir.addLocationObserver(this);

        // set ourselves up as a failure handler with the location director because we need to do
        // special processing
        _locdir.setFailureHandler(this);

        // register for scene notifications
        _wctx.getClient().getInvocationDirector().registerReceiver(new SceneDecoder(this));
    }

    /**
     * Set the scene repository.
     */
    public function setSceneRepository (screp :SceneRepository) :void
    {
        _screp = screp;
        _scache.clear();
    }

    /**
     * Returns the display scene object associated with the scene we currently occupy or null if we
     * currently occupy no scene.
     */
    public function getScene () :Scene
    {
        return _scene;
    }

    /**
     * Requests that this client move the specified scene. A request will be made and when the
     * response is received, the location observers will be notified of success or failure.
     *
     * @return true if the move to request was issued, false if it was rejected by a location
     * observer or because we have another request outstanding.
     */
    public function moveTo (sceneId :int) :Boolean
    {
        // make sure the sceneId is valid
        if (sceneId < 0) {
            log.warning("Refusing moveTo(): invalid sceneId", "sceneId", sceneId);
            return false;
        }

        // sanity-check the destination scene id
        if (sceneId == _sceneId) {
            log.warning("Refusing request to move to the same scene", "sceneId", sceneId);
            return false;
        }

        // prepare to move to this scene (sets up pending data)
        if (!prepareMoveTo(sceneId, null)) {
            return false;
        }

        // do the deed
        sendMoveRequest();
        return true;
    }

    /**
     * Prepares to move to the requested scene. The location observers are asked to ratify the move
     * and our pending scene mode is loaded from the scene repository. This can be called by
     * cooperating directors that need to coopt the moveTo process.
     */
    public function prepareMoveTo (sceneId :int, rl :ResultListener) :Boolean
    {
        // first check to see if our observers are happy with this move request
        if (!_locdir.mayMoveTo(sceneId, rl)) {
            return false;
        }

        // we need to call this both to mark that we're issuing a move request and to check to see
        // if the last issued request should be considered stale
        var refuse :Boolean = _locdir.checkRepeatMove();

        // complain if we're over-writing a pending request
        if (_pendingData != null) {
            if (refuse) {
                log.warning("Refusing moveTo; We have a request outstanding",
                    "psid", _pendingData.sceneId, "nsid", sceneId);
                return false;

            } else {
                log.warning("Overriding stale moveTo request",
                    "psid", _pendingData.sceneId, "nsid", sceneId);
            }
        }

        // create and initialize a new pending data record
        _pendingData = createPendingData();

        // load up the cached pending scene so that we can communicate its version to the server
        _pendingData.model = loadSceneModel(sceneId);

        // make a note of our pending scene id
        _pendingData.sceneId = sceneId;

        // all systems go
        return true;
    }

    /**
     * Returns the model loaded in preparation for a scene transition. This is made available only
     * for cooperating directors which may need to coopt the scene transition process. The pending
     * model is only valid immediately following a call to {@link #prepareMoveTo}.
     */
    public function getPendingModel () :SceneModel
    {
        return _pendingData == null ? null : _pendingData.model;
    }

    // from interface SceneService_SceneMoveListener
    public function moveSucceeded (placeId :int, config :PlaceConfig) :void
    {
        // our move request was successful, deal with subscribing to our new place object
        _locdir.didMoveTo(placeId, config);

        // keep track of our previous scene info
        _previousSceneId = _sceneId;

        // clear out the old info
        clearScene();

        // make the pending scene the active scene
        if (_pendingData == null) {
            log.warning("Aiya! Scene move succeeded but we have no pending data!",
                "prevId", _previousSceneId);
            return;
        }
        _sceneId = _pendingData.sceneId;
        _pendingData = null;

        // since we're committed to moving to the new scene, we'll parallelize and go ahead and
        // load up the new scene now rather than wait until subscription to our place object
        // succeeds
        _model = loadSceneModel(_sceneId);

        // complain if we didn't find a scene
        if (_model == null) {
            log.warning("Aiya! Unable to load scene", "sid", _sceneId, "plid", placeId);
            return;
        }

        // and finally create a display scene instance with the model and the place config
        _scene = _fact.createScene(_model, config);
    }

    // from interface SceneService_SceneMoveListener
    public function moveSucceededWithUpdates (
        placeId :int, config :PlaceConfig, updates :TypedArray) :void
    {
        log.info("Got updates", "placeId", placeId, "config", config, "updates", updates);

        // apply the updates to our cached scene
        var model :SceneModel = loadSceneModel(_pendingData.sceneId);
        var failure :Boolean = false;
        for each (var update :SceneUpdate in updates) {
            try {
                update.validate(model);
            } catch (ise :IllegalOperationError) {
                log.warning("Scene update failed validation",
                    "model", model, "update", update, "error", ise.message);
                failure = true;
                break;
            }

            try {
                update.apply(model);
            } catch (e :Error) {
                log.warning("Failure applying scene update", "model", model, "update", update, e);
                failure = true;
                break;
            }
        }

        if (failure) {
            // delete the now half-booched scene model from the repository
            try {
                _screp.deleteSceneModel(_pendingData.sceneId);
            } catch (ioe :IOError) {
                log.warning("Failure removing booched scene model",
                    "sceneId", _pendingData.sceneId, ioe);
            }

            // act as if the scene move failed, though we'll be in a funny state because the server
            // thinks we've changed scenes, but the client can try again without its booched scene
            // model
            requestFailed(InvocationCodes.INTERNAL_ERROR);
            return;
        }

        // store the updated scene in the repository
        persistSceneModel(model);

        // finally pass through to the normal success handler
        moveSucceeded(placeId, config);
    }

    // from interface SceneService_SceneMoveListener
    public function moveSucceededWithScene (
        placeId :int, config :PlaceConfig, model :SceneModel) :void
    {
        log.info("Got updated scene model",
            "placeId", placeId, "config", config,
            "scene", model.sceneId + "/" + model.name + "/" + model.version);

        // update the model in the repository
        persistSceneModel(model);

        // update our scene cache
        _scache.put(model.sceneId, model);

        // and pass through to the normal move succeeded handler
        moveSucceeded(placeId, config);
    }

    // from interface SceneService_SceneMoveListener
    public function moveRequiresServerSwitch (hostname :String, ports :TypedArray) :void
    {
        log.info("Scene switch requires server switch", "host", hostname, "ports", ports);
        // keep track of our current pending data because it will be cleared when we log off of
        // this server and onto the next one
        var pendingData :PendingData = _pendingData;
        // ship on over to the other server
        _wctx.getClient().moveToServer(hostname, ports, new ConfirmAdapter(
            function () :void { // succeeded
                // resend our move request now that we're connected to the new server
                _pendingData = pendingData;
                sendMoveRequest();
            }, requestFailed));
    }

    // from interface SceneService_SceneMoveListener
    public function requestFailed (reason :String) :void
    {
        // clear out our pending info
        var sceneId :int = _pendingData.sceneId;
        _pendingData = null;

        // let our observers know that something has gone horribly awry
        _locdir.failedToMoveTo(sceneId, reason);
    }

    // from interface LocationObserver
    public function locationMayChange (placeId :int) :Boolean
    {
        return true; // fine with us
    }

    // from interface LocationObserver
    public function locationDidChange (place :PlaceObject) :void
    {
        // if we're no longer in a scene, we need to clear out our scene information
        if (!(place is SceneObject)) {
            clearScene();
        }
    }

    // from interface LocationObserver
    public function locationChangeFailed (placeId :int, reason :String) :void
    {
        // we don't care about this notification as we're registered as a LocationDirector
        // FailureHandler so we will later be requested to recover from our failed move
    }

    /**
     * Called by SceneController instances to tell us about an update to the current scene.
     */
    public function updateReceived (update :SceneUpdate) :void
    {
        _scene.updateReceived(update);
        persistSceneModel(_scene.getSceneModel());
    }

    /**
     * Called to clean up our place and scene state information when we leave a scene.
     */
    public function didLeaveScene () :void
    {
        // let the location director know what's up
        _locdir.didLeavePlace();

        // clear out our own scene state
        clearScene();
    }

    // documentation inherited from interface
    public function forcedMove (sceneId :int) :void
    {
        log.info("Moving at request of server", "sceneId", sceneId);

        // clear out our old scene and place data
        didLeaveScene();

        // move to the new scene
        moveTo(sceneId);
    }

    /**
     * Sets the moveHandler for use in recoverFailedMove.
     */
    public function setMoveHandler (handler :SceneDirector_MoveHandler) :void
    {
        if (_moveHandler != null) {
            log.warning("Requested to set move handler, but we've already got one. The " +
                "conflicting entities will likely need to perform more sophisticated " +
                "coordination to deal with failures.", "old", _moveHandler, "new", handler);

        } else {
            _moveHandler = handler;
        }
    }

    /**
     * Called when something breaks down in the process of performing a <code>moveTo</code>
     * request.
     */
    public function recoverFailedMove (placeId :int) :void
    {
        // we'll need this momentarily
        var sceneId :int = _sceneId;

        // clear out our now bogus scene tracking info
        clearScene();

        // if we were previously somewhere (and that somewhere isn't where we just tried to go),
        // try going back to that happy place
        if (_previousSceneId != -1 && _previousSceneId != sceneId) {
            // if we have a move handler use that
            if (_moveHandler != null) {
                _moveHandler.recoverMoveTo(_previousSceneId);

            } else {
                moveTo(_previousSceneId);
            }
        }
    }

    // documentation inherited
    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);

        // clear out our business
        clearScene();
        _scache.clear();
        _pendingData = null;
        _previousSceneId = -1;
        _sservice = null;
    }

    /**
     * Issues the scene move request using information from the supplied pending data.
     */
    protected function sendMoveRequest () :void
    {
        // check the version of our cached copy of the scene to which we're requesting to move; if
        // we were unable to load it, assume a cached version of zero
        var sceneVers :int = 0;
        if (_pendingData.model != null) {
            sceneVers = _pendingData.model.version;
        }

        // issue a moveTo request
        log.info("Issuing moveTo(" + _pendingData.sceneId + ", " + sceneVers + ").");
        _sservice.moveTo(_pendingData.sceneId, sceneVers, this);
    }

    /**
     * Creates a pending scene move request. Derived classes may wish to extend this data with
     * additional information to be tracked during movement between scenes. This is encapsulated so
     * that if a scene move requires a switch to a new server, all necessary data can be properly
     * tracked during the server switch.
     */
    protected function createPendingData () :PendingData
    {
        return new PendingData();
    }

    /**
     * Clears out our current scene information and releases the scene model for the loaded scene
     * back to the cache.
     */
    protected function clearScene () :void
    {
        // clear out our scene id info
        _sceneId = -1;

        // clear out our references
        _model = null;
        _scene = null;
    }

    /**
     * Loads a scene from the repository. If the scene is cached, it will be returned from the
     * cache instead.
     */
    protected function loadSceneModel (sceneId :int) :SceneModel
    {
        // first look in the model cache
        var model :SceneModel = (_scache.get(sceneId) as SceneModel);

        // load from the repository if it's not cached
        if (model == null) {
            try {
                model = _screp.loadSceneModel(sceneId);
                _scache.put(sceneId, model);

            } catch (nsse :NoSuchSceneError) {
                // nothing special here, just fall through and return null

            } catch (ioe :IOError) {
                // complain first, then return null
                log.warning("Error loading scene", "scid", sceneId, "error", ioe);
            }
        }

        return model;
    }

    /**
     * Persist the scene model to the clientside persistant cache.
     */
    protected function persistSceneModel (model :SceneModel) :void
    {
        try {
            _screp.storeSceneModel(model);
        } catch (ioe :IOError) {
            log.warning("Failed to update repository with updated scene",
                "sceneId", model.sceneId, "nvers", model.version, ioe);
        }
    }

    // from BasicDirector
    override protected function registerServices (client :Client) :void
    {
        client.addServiceGroup(SceneCodes.WHIRLED_GROUP);
    }

    // from BasicDirector
    override protected function fetchServices (client :Client) :void
    {
        // get a handle on our scene service
        _sservice = (client.requireService(SceneService) as SceneService);
    }

    /** Access to general client services. */
    protected var _wctx :WhirledContext;

    /** Access to our scene services. */
    protected var _sservice :SceneService;

    /** The client's active location director. */
    protected var _locdir :LocationDirector;

    /** The entity via which we load scene data. */
    protected var _screp :SceneRepository;

    /** The entity we use to create scenes from scene models. */
    protected var _fact :SceneFactory;

    /** A cache of scene model information. */
    protected var _scache :Map = Maps.newBuilder(int).makeLR(5).build();

    /** The display scene object for the scene we currently occupy. */
    protected var _scene :Scene;

    /** The scene model for the scene we currently occupy. */
    protected var _model :SceneModel;

    /** The id of the scene we currently occupy. */
    protected var _sceneId :int = -1;

    /** Information for the scene for which we have an outstanding moveTo request, or null if we
     * have no outstanding request. */
    protected var _pendingData :PendingData;

    /** The id of the scene we previously occupied. */
    protected var _previousSceneId :int = -1;

    /** Reference to our move handler. */
    protected var _moveHandler :SceneDirector_MoveHandler = null;
}
}
