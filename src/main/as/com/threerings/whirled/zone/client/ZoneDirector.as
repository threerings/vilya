//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.zone.client {

import com.threerings.io.TypedArray;
import com.threerings.util.Log;
import com.threerings.util.ResultListener;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.ConfirmAdapter;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.client.PendingData;
import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.client.SceneDirector_MoveHandler;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.WhirledContext;

import com.threerings.whirled.zone.client.ZoneService_ZoneMoveListener;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.util.ZoneUtil;


public class ZoneDirector extends BasicDirector
    implements ZoneReceiver, ZoneService_ZoneMoveListener, SceneDirector_MoveHandler
{
    private static const log :Log = Log.getLog(ZoneDirector);

    // We could be streamed one of these...
    SceneUpdate;

    /**
     * Constructs a zone director with the supplied context, and delegate scene director (which the
     * zone director will coordinate with when moving from scene to scene). A zone director is
     * required on the client side for systems that wish to use the zone services.
     */
    public function ZoneDirector (ctx :WhirledContext, scdir :SceneDirector)
    {
        super(ctx);
        _wCtx = ctx;
        _scdir = scdir;
        _scdir.setMoveHandler(this);

        // register for zone notifications
        _wCtx.getClient().getInvocationDirector().registerReceiver(new ZoneDecoder(this));
    }

    /**
     * Returns the summary for the zone currently occupied by the client or null if the client does
     * not currently occupy a zone (not a normal situation).
     */
    public function getZoneSummary () :ZoneSummary
    {
        return _summary;
    }

    /**
     * Adds a zone observer to the list. This observer will subsequently be notified of effected
     * and failed zone changes.
     */
    public function addZoneObserver (observer :ZoneObserver) :void
    {
        _observers.add(observer);
    }

    /**
     * Removes a zone observer from the list.
     */
    public function removeZoneObserver (observer :ZoneObserver) :void
    {
        _observers.remove(observer);
    }

    /**
     * Requests that this client move the specified scene in the specified zone. A request will be
     * made and when the response is received, the location observers will be notified of success
     * or failure.
     */
    public function moveTo (zoneId :int, sceneId :int, rl :ResultListener = null) :Boolean
    {
        // make sure the zoneId and sceneId are valid
        if (zoneId < 0 || sceneId < 0) {
            log.warning("Refusing moveTo(): invalid sceneId or zoneId",
                "zoneId", zoneId, "sceneId", sceneId);
            return false;
        }

        // if the requested zone is the same as our current zone, we just want a regular old moveTo
        // request
        if (_summary != null && zoneId == _summary.zoneId) {
            return _scdir.moveTo(sceneId);
        }

        // otherwise, we make a zoned moveTo request; prepare to move to this scene (sets up
        // pending data)
        if (!_scdir.prepareMoveTo(sceneId, rl)) {
            return false;
        }

        _pendingZoneId = zoneId;

        sendMoveRequest();

        return true;
    }

    protected function sendMoveRequest () :void
    {
        // let our zone observers know that we're attempting to switch zones
        notifyObservers(_pendingZoneId);

        // check the version of our cached copy of the scene to which we're requesting to move; if
        // we were unable to load it, assume a cached version of zero
        var sceneVers :int = 0;
        var sceneId :int = _scdir.getPendingSceneId();
        var pendingModel :SceneModel = _scdir.getPendingModel();
        if (pendingModel != null) {
            sceneVers = pendingModel.version;
        }

        // issue a moveTo request
        log.info("Issuing zoned moveTo(" + ZoneUtil.toString(_pendingZoneId) +
                 ", " + sceneId + ", " + sceneVers + ").");
        _zservice.moveTo(_pendingZoneId, sceneId, sceneVers, this);
    }

    override protected function fetchServices (client :Client) :void
    {
        _zservice = (ZoneService)(client.requireService(ZoneService));
    }

    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);

        // clear out our business
        _zservice = null;
        _summary = null;
        _previousZoneId = -1;
    }

    // from interface ZoneService.ZoneMoveListener
    public function moveSucceeded (placeId :int, config :PlaceConfig, summary :ZoneSummary) :void
    {
        if (_summary != null) {
            // keep track of our previous zone info
            _previousZoneId = _summary.zoneId;
        }

        // keep track of the summary
        _summary = summary;

        // We're not heading there any more.
        _pendingZoneId = -1;

        // pass the rest off to the standard scene transition code
        _scdir.moveSucceeded(placeId, config);

        // and let the zone observers know what's up
        notifyObservers(summary);
    }

    // from interface ZoneService.ZoneMoveListener
    public function moveSucceededWithUpdates (
        placeId :int, config :PlaceConfig, summary :ZoneSummary,
        updates :TypedArray /* of SceneUpdate */) :void
    {
        // keep track of the summary
        _summary = summary;

        // We're not heading there any more.
        _pendingZoneId = -1;

        // pass the rest off to the standard scene transition code
        _scdir.moveSucceededWithUpdates(placeId, config, updates);

        // and let the zone observers know what's up
        notifyObservers(summary);
    }

    // from interface ZoneService.ZoneMoveListener
    public function moveSucceededWithScene (
        placeId :int, config :PlaceConfig, summary :ZoneSummary, model :SceneModel) :void
    {
        // keep track of the summary
        _summary = summary;

        // We're not heading there any more.
        _pendingZoneId = -1;

        // pass the rest off to the standard scene transition code
        _scdir.moveSucceededWithScene(placeId, config, model);

        // and let the zone observers know what's up
        notifyObservers(summary);
    }

    // from interface ZoneService_ZoneMoveListener
    public function moveRequiresServerSwitch (hostname :String, ports :TypedArray) :void
    {
        log.info("Zone switch requires server switch", "host", hostname, "ports", ports);
        // ship on over to the other server

        // keep track of our current pending data because it will be cleared when we log off of
        // this server and onto the next one
        var restorePending :Function = _scdir.getPendingDataRestoreFunc();

        _wCtx.getClient().moveToServer(hostname, ports, new ConfirmAdapter(
            function () :void { // succeeded
                restorePending();
                sendMoveRequest();
            }, requestFailed));
    }

    // from interface ZoneService.ZoneMoveListener
    public function requestFailed (reason :String) :void
    {
        // let the scene director cope
        _scdir.requestFailed(reason);

        // and let the observers know what's up
        notifyObservers(reason);
    }


    // documentation inherited from interface
    public function forcedMove (zoneId :int, sceneId :int) :void
    {
        // if we're in the middle of a move, we can't abort it or we will screw everything up, so
        // just finish up what we're doing and assume that the repeated move request was the
        // spurious one as it would be in the case of lag causing rapid-fire repeat requests
        if (_scdir.movePending()) {
            if (_scdir.getPendingSceneId() == sceneId) {
                log.info("Dropping forced move because we have a move pending",
                    "pend", _scdir.getPendingModel(), "rzId", zoneId, "rsId", sceneId);
            } else {
                log.info("Delaying forced move because we have a move pending",
                    "pend", _scdir.getPendingModel(), "rzId", zoneId, "rsId", sceneId);
                _scdir.addPendingForcedMove(function() :void {
                        forcedMove(zoneId, sceneId);
                });
            }
            return;
        }

        log.info("Moving at request of server",
            "zoneId", zoneId, "sceneId", sceneId);
        // clear out our old scene and place data
        _scdir.didLeaveScene();
        // move to the new zone and scene
        moveTo(zoneId, sceneId, null);
    }

    // from SceneDirector.MoveHandler
    public function  recoverMoveTo (previousSceneId :int) :void
    {
        if (_summary != null) {
            return; // if we're currently somewhere, just stay there
        }

        // Not gonna get there, so clear it.
        _pendingZoneId = -1;

        // otherwise if we were previously in a zone/scene, try going back there
        if (_previousZoneId != -1) {
            moveTo(_previousZoneId, previousSceneId);
        } else {
            _scdir.moveTo(previousSceneId);
        }
    }

    /**
     * Notifies observers of success or failure, depending on the type of object provided as data.
     */
    protected function notifyObservers (data :Object) :void
    {
        // let our observers know that all is well on the western front
        for each (var obs :ZoneObserver in _observers) {
            try {
                if (data is Number) {
                    obs.zoneWillChange(data as Number);
                } else if (data is ZoneSummary) {
                    obs.zoneDidChange(data as ZoneSummary);
                } else {
                    obs.zoneChangeFailed(data as String);
                }

            } catch (e :Error) {
                log.warning("Zone observer choked during notification",
                    "data", data, "obs", obs, e);
            }
        }
    }

    /** A reference to the active client context. */
    protected var _wCtx :WhirledContext;

    /** A reference to the scene director with which we coordinate. */
    protected var _scdir :SceneDirector;

    /** Provides access to zone services. */
    protected var _zservice :ZoneService;

    /** A reference to the zone summary for the currently occupied zone. */
    protected var _summary :ZoneSummary;

    /** Our zone observer list. */
    protected var _observers :Array = [];

    /** Our previous zone id. */
    protected var _previousZoneId :int = -1;

    /** Where we're headed. */
    protected var _pendingZoneId :int = -1;
}
}
