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

package com.threerings.whirled.zone.peer.server;

import com.google.inject.Inject;
import com.samskivert.util.ResultListener;
import com.samskivert.util.Tuple;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.LocationManager;

import com.threerings.whirled.server.SceneRegistry;
import com.threerings.whirled.zone.client.ZoneService;
import com.threerings.whirled.zone.data.ZoneCodes;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.peer.data.HostedZone;
import com.threerings.whirled.zone.server.ZoneManager;
import com.threerings.whirled.zone.server.ZoneMoveHandler;
import com.threerings.whirled.zone.server.ZoneRegistry;
import com.threerings.whirled.zone.server.ZoneManager.ResolutionListener;

import static com.threerings.whirled.zone.Log.log;

public abstract class PeeredZoneRegistry extends ZoneRegistry
{
    /**
     * Peer-awareness for finding out about zones.
     */
    public static interface PeerZoneResolutionListener extends ResolutionListener
    {
        /**
         * Called when the zone is already hosted on another node.
         */
        public void zoneOnNode (Tuple<String, HostedZone> nodeInfo);
    }

    /**
     * Peered zones should be sure to call back to one of these when they're going away.
     */
    public static interface PeerZoneShutdownListener
    {
        public void zoneDidShutdown (int zoneId);
    }

    /**
     * A ZoneMoveHandler that can receive the sceneOnNode() callback.
     */
    public abstract static class PeerZoneMoveHandler extends ZoneMoveHandler
        implements PeerZoneResolutionListener
    {
        public PeerZoneMoveHandler (
            LocationManager locman, ZoneManager zmgr, SceneRegistry scReg, BodyObject body,
            int sceneId, int sceneVer, ZoneService.ZoneMoveListener listener)
        {
            super(locman, zmgr, scReg, body, sceneId, sceneVer, listener);
        }
    }

    protected abstract PeerZoneMoveHandler createMoveHandler (LocationManager locman,
        ZoneManager zmgr, SceneRegistry scReg, BodyObject body, int sceneId, int sceneVer,
        ZoneService.ZoneMoveListener listener);

    @Inject public PeeredZoneRegistry (InvocationManager invmgr, ZonePeerManager peerMgr)
    {
        super(invmgr);
        _peerMgr = peerMgr;
    }

    @Override
    public void registerZoneManager (byte zoneType, ZoneManager manager)
    {
        if (!(manager instanceof PeeredZoneManager)) {
            throw new IllegalArgumentException("All ZoneManagers received by PeeredZoneRegistry " +
            		"must be PeeredZoneManagers");
        }
        super.registerZoneManager(zoneType, manager);
    }

    @Override
    public PeeredZoneManager getZoneManager (int qualifiedZoneId)
    {
        return (PeeredZoneManager)super.getZoneManager(qualifiedZoneId);
    }

    @Override
    public void moveTo (ClientObject caller, int zoneId, int sceneId,
        int sceneVer, ZoneService.ZoneMoveListener listener)
        throws InvocationException
    {
        if (!(caller instanceof BodyObject)) {
            log.warning("Request to switch zones by non-BodyObject " +
                        "[clobj=" + caller.getClass() + "].");
            throw new InvocationException(ZoneCodes.INTERNAL_ERROR);
        }
        BodyObject body = (BodyObject)caller;

        PeerZoneMoveHandler handler = createMoveHandler(_locman, getZoneManager(zoneId),
            _screg, body, sceneId, sceneVer, listener);

        resolvePeerZone(zoneId, handler);
    }

    public void resolveZone (int zoneId, ResolutionListener listener)
    {
        PeeredZoneManager zmgr = getZoneManager(zoneId);
        if (zmgr == null) {
            log.warning("Trying to resolve a zone for which we have no manager", "zoneId", zoneId);
            listener.zoneFailedToResolve(zoneId, new Exception(InvocationCodes.INTERNAL_ERROR));
        } else {
            zmgr.resolveZone(zoneId, listener);
        }
    }

    /**
     * Resolve a zone, or return the information on the peer on which it's hosted.
     */
    public void resolvePeerZone (final int zoneId, final PeerZoneResolutionListener listener)
    {
        // check to see if the destination zone is already hosted on a server
        Tuple<String, HostedZone> nodeInfo = _peerMgr.getZoneHost(zoneId);

        // if it's already hosted...
        if (nodeInfo != null) {
            // it's hosted on this server! It should already be resolved...
            if (_peerMgr.getNodeObject().nodeName.equals(nodeInfo.left)) {
                resolveZone(zoneId, listener);
            } else {
                listener.zoneOnNode(nodeInfo); // somewhere else, pass the buck
            }
            return;

        } else {
            resolveNewZone(zoneId, listener);
        }
    }

    /**
     * Resolve a zone that's not yet hosted.
     */
    protected void resolveNewZone (final int zoneId, final PeerZoneResolutionListener listener)
    {
        // otherwise the zone is not resolved here nor there; so we claim the zone by acquiring a
        // distributed lock and then resolve it locally
        _peerMgr.acquireLock(ZonePeerManager.getZoneLock(zoneId), new ResultListener<String>() {
            public void requestCompleted (String nodeName) {
                resolveZoneForNode(zoneId, nodeName, listener);
            }
            public void requestFailed (Exception cause) {
                log.warning("Failed to acquire zone resolution lock", "id", zoneId, cause);
                listener.zoneFailedToResolve(zoneId, cause);
            }
        });
    }

    /**
     * Resolve a zone that's hosted or to-be-hosted on a zone.
     */
    protected void resolveZoneForNode (final int zoneId, String nodeName,
        final PeerZoneResolutionListener listener)
    {
        if (_peerMgr.getNodeObject().nodeName.equals(nodeName)) {
            log.debug("Got lock, resolving zone", "zoneId", zoneId);
            resolveZone(zoneId, new ResolutionListener() {
                public void zoneWasResolved (ZoneSummary zonesum) {
                    releaseLock();
                    _peerMgr.zoneDidStartup(zonesum.zoneId, zonesum.name);
                    PeerZoneShutdownListener shutdowner = new PeerZoneShutdownListener() {
                        public void zoneDidShutdown (int zoneId) {
                            _peerMgr.zoneDidShutdown(zoneId);
                        }
                    };
                    getZoneManager(zonesum.zoneId).setShutdownListener(
                        zonesum.zoneId, shutdowner);
                    listener.zoneWasResolved(zonesum);
                }
                public void zoneFailedToResolve (int zoneId, Exception reason) {
                    releaseLock();
                    listener.zoneFailedToResolve(zoneId, reason);
                }
                protected void releaseLock () {
                    _peerMgr.releaseLock(ZonePeerManager.getZoneLock(zoneId),
                        new ResultListener.NOOP<String>());
                }
            });

        } else {
            // we didn't get the lock, so let's see what happened by re-checking
            Tuple<String, HostedZone> nodeInfo = _peerMgr.getZoneHost(zoneId);
            if (nodeName == null || nodeInfo == null || !nodeName.equals(nodeInfo.left)) {
                log.warning("Zone resolved on wacked-out node?",
                    "zoneId", zoneId, "nodeName", nodeName, "nodeInfo", nodeInfo);
                listener.zoneFailedToResolve(zoneId,
                    new Exception("Zone on bogus host node"));
            } else {
                listener.zoneOnNode(nodeInfo); // somewhere else
            }
        }
    }

    /**
     * Pass on that the zone has shutdown.
     */
    public void zoneDidShutdown (int zoneId)
    {
        _peerMgr.zoneDidShutdown(zoneId);
    }

    protected ZonePeerManager _peerMgr;
}
