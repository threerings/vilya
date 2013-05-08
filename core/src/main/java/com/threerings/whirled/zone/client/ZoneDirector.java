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

package com.threerings.whirled.zone.client;

import java.util.ArrayList;

import com.google.common.collect.Lists;

import com.samskivert.util.ResultListener;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.util.WhirledContext;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.util.ZoneUtil;

import static com.threerings.whirled.zone.Log.log;

/**
 * The zone director augments the scene services with the notion of zones.  Zones are
 * self-contained, connected groups of scenes. The normal scene director services can be used to
 * move from scene to scene, but moving to a new zone requires a special move request which can be
 * accomplished via the zone director. The zone director also makes available the zone summary
 * which provides information on the zone which can be used to generate an overview map or similar.
 */
public class ZoneDirector extends BasicDirector
    implements ZoneReceiver, ZoneService.ZoneMoveListener, SceneDirector.MoveHandler
{
    /**
     * Constructs a zone director with the supplied context, and delegate scene director (which the
     * zone director will coordinate with when moving from scene to scene). A zone director is
     * required on the client side for systems that wish to use the zone services.
     */
    public ZoneDirector (WhirledContext ctx, SceneDirector scdir)
    {
        super(ctx);
        _ctx = ctx;
        _scdir = scdir;
        _scdir.setMoveHandler(this);

        // register for zone notifications
        _ctx.getClient().getInvocationDirector().registerReceiver(new ZoneDecoder(this));
    }

    /**
     * Returns the summary for the zone currently occupied by the client or null if the client does
     * not currently occupy a zone (not a normal situation).
     */
    public ZoneSummary getZoneSummary ()
    {
        return _summary;
    }

    /**
     * Adds a zone observer to the list. This observer will subsequently be notified of effected
     * and failed zone changes.
     */
    public void addZoneObserver (ZoneObserver observer)
    {
        _observers.add(observer);
    }

    /**
     * Removes a zone observer from the list.
     */
    public void removeZoneObserver (ZoneObserver observer)
    {
        _observers.remove(observer);
    }

    /**
     * Requests that this client move the specified scene in the specified zone. A request will be
     * made and when the response is received, the location observers will be notified of success
     * or failure.
     */
    public boolean moveTo (int zoneId, int sceneId)
    {
        return moveTo(zoneId, sceneId, null);
    }

    /**
     * Requests that this client move the specified scene in the specified zone. A request will be
     * made and when the response is received, the location observers will be notified of success
     * or failure.
     */
    public boolean moveTo (int zoneId, int sceneId, ResultListener<PlaceConfig> rl)
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

    protected void sendMoveRequest ()
    {
        // let our zone observers know that we're attempting to switch zones
        notifyObservers(Integer.valueOf(_pendingZoneId));

        // check the version of our cached copy of the scene to which we're requesting to move; if
        // we were unable to load it, assume a cached version of zero
        int sceneVers = 0;
        int sceneId = _scdir.getPendingSceneId();
        SceneModel pendingModel = _scdir.getPendingModel();
        if (pendingModel != null) {
            sceneVers = pendingModel.version;
        }

        // issue a moveTo request
        log.info("Issuing zoned moveTo(" + ZoneUtil.toString(_pendingZoneId) +
                 ", " + sceneId + ", " + sceneVers + ").");
        _zservice.moveTo(_pendingZoneId, sceneId, sceneVers, this);
    }

    @Override
    protected void fetchServices (Client client)
    {
        _zservice = client.requireService(ZoneService.class);
    }

    @Override
    public void clientDidLogoff (Client client)
    {
        super.clientDidLogoff(client);

        // clear out our business
        _zservice = null;
        _summary = null;
        _previousZoneId = -1;
    }

    // from interface SceneService.SceneMoveListener
    public void moveRequiresServerSwitch (String hostname, int[] ports)
    {
        // ship on over to the other server
        _ctx.getClient().moveToServer(hostname, ports, new ZoneService.ConfirmListener() {
            public void requestProcessed () {
                // resend our move request now that we're connected to the new server
                sendMoveRequest();
            }
            public void requestFailed (String reason) {
                ZoneDirector.this.requestFailed(reason);
            }
        });
    }

    // from interface ZoneService.ZoneMoveListener
    public void moveSucceeded (int placeId, PlaceConfig config, ZoneSummary summary)
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
    public void moveSucceededWithUpdates (
        int placeId, PlaceConfig config, ZoneSummary summary, SceneUpdate[] updates)
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
    public void moveSucceededWithScene (
        int placeId, PlaceConfig config, ZoneSummary summary, SceneModel model)
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

    // from interface ZoneService.ZoneMoveListener
    public void requestFailed (String reason)
    {
        // let the scene director cope
        _scdir.requestFailed(reason);

        // and let the observers know what's up
        notifyObservers(reason);
    }

    // documentation inherited from interface
    public void forcedMove (final int zoneId, final int sceneId)
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
                // We have a move pending to a DIFFERENT scene - we'll do this as soon as that's
                //  done.
                _scdir.addPendingForcedMove(new Runnable() {
                    public void run () {
                        forcedMove(zoneId, sceneId);
                    }
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
    public void recoverMoveTo (int previousSceneId)
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
    protected void notifyObservers (Object data)
    {
        // let our observers know that all is well on the western front
        for (ZoneObserver obs : _observers) {
            try {
                if (data instanceof Integer) {
                    obs.zoneWillChange(((Integer)data).intValue());
                } else if (data instanceof ZoneSummary) {
                    obs.zoneDidChange((ZoneSummary)data);
                } else {
                    obs.zoneChangeFailed((String)data);
                }

            } catch (Throwable t) {
                log.warning("Zone observer choked during notification",
                    "data", data, "obs", obs, t);
            }
        }
    }

    /** A reference to the active client context. */
    protected WhirledContext _ctx;

    /** A reference to the scene director with which we coordinate. */
    protected SceneDirector _scdir;

    /** Provides access to zone services. */
    protected ZoneService _zservice;

    /** A reference to the zone summary for the currently occupied zone. */
    protected ZoneSummary _summary;

    /** Our zone observer list. */
    protected ArrayList<ZoneObserver> _observers = Lists.newArrayList();

    /** Our previous zone id. */
    protected int _previousZoneId = -1;

    /** Where we're headed. */
    protected int _pendingZoneId = -1;
}
