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

package com.threerings.whirled.zone.server;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneRegistry;

import com.threerings.whirled.zone.Log;
import com.threerings.whirled.zone.client.ZoneService.ZoneMoveListener;
import com.threerings.whirled.zone.data.ZoneCodes;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.data.ZonedBodyObject;

/**
 * Provides zone related services which are presently the ability to move from zone to zone.
 */
public class ZoneProvider
    implements ZoneCodes, InvocationProvider
{
    /**
     * Constructs a zone provider that will interoperate with the supplied zone and scene
     * registries. The zone provider will automatically be constructed and registered by the {@link
     * ZoneRegistry}, which a zone-using system must create and initialize in their server.
     */
    public ZoneProvider (LocationProvider locprov, ZoneRegistry zonereg, SceneRegistry screg)
    {
        _locprov = locprov;
        _zonereg = zonereg;
        _screg = screg;
    }

    /**
     * Processes a request from a client to move to a scene in a new zone.
     *
     * @param caller the user requesting the move.
     * @param zoneId the qualified zone id of the new zone.
     * @param sceneId the identifier of the new scene.
     * @param sceneVer the version of the scene model currently held by the client.
     * @param listener the entity to inform of success or failure.
     */
    public void moveTo (ClientObject caller, int zoneId, int sceneId,
                        int sceneVer, ZoneMoveListener listener)
        throws InvocationException
    {
        if (!(caller instanceof ZonedBodyObject)) {
            Log.warning("Request to switch zones by non-ZonedBodyObject " +
                        "[clobj=" + caller.getClass() + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // look up the caller's current zone id and make sure it is happy about their departure
        // from the current zone
        BodyObject body = (BodyObject)caller;
        ZoneManager ozmgr = _zonereg.getZoneManager(((ZonedBodyObject)caller).getZoneId());
        if (ozmgr != null) {
            String msg = ozmgr.ratifyBodyExit(body);
            if (msg != null) {
                throw new InvocationException(msg);
            }
        }

        // look up the zone manager for the zone
        ZoneManager zmgr = _zonereg.getZoneManager(zoneId);
        if (zmgr == null) {
            Log.warning("Requested to enter a zone for which we have no manager " +
                        "[user=" + body.who() + ", zoneId=" + zoneId + "].");
            throw new InvocationException(NO_SUCH_ZONE);
        }

        // resolve the zone and move the user
        zmgr.resolveZone(zoneId, new ZoneMoveHandler(
                             zmgr, (BodyObject)caller, sceneId, sceneVer, listener));
    }

    /**
     * Ejects the specified body from their current scene and sends them a request to move to the
     * specified new zone and scene. This is the zone-equivalent to {@link
     * LocationProvider#moveBody}.
     *
     * @return null if the user was forcibly moved, or a string indicating the reason for denial of
     * departure of their current zone (from {@link ZoneManager#ratifyBodyExit}).
     */
    public String moveBody (ZonedBodyObject source, int zoneId, int sceneId)
    {
        if (source.getZoneId() == zoneId) {
            // handle the case of moving somewhere in the same zone
            _screg.moveBody((BodyObject) source, sceneId);

        } else {
            // first remove them from their old location
            String reason = leaveOccupiedZone(source);
            if (reason != null) {
                return reason;
            }

            // then send a forced move notification
            ZoneSender.forcedMove((BodyObject)source, zoneId, sceneId);
        }
        return null;
    }

    /**
     * Ejects the specified body from their current scene and zone. This is the zone equivalent to
     * {@link LocationProvider#leaveOccupiedPlace}.
     *
     * @return null if the user was forcibly moved, or a string indicating the reason for denial of
     * departure of their current zone (from {@link ZoneManager#ratifyBodyExit}).
     */
    public String leaveOccupiedZone (ZonedBodyObject source)
    {
        // look up the caller's current zone id and make sure it is happy about their departure
        // from the current zone
        ZoneManager zmgr = _zonereg.getZoneManager(source.getZoneId());
        String msg;
        if (zmgr != null &&
            (msg = zmgr.ratifyBodyExit((BodyObject)source)) != null) {
            return msg;
        }

        // remove them from their occupied scene
        _screg.leaveOccupiedScene((BodyObject)source);

        // and clear out their zone information
        source.setZoneId(-1);

        return null;
    }

    /** The entity that handles basic location changes. */
    protected LocationProvider _locprov;

    /** The zone registry with which we communicate. */
    protected ZoneRegistry _zonereg;

    /** The scene registry with which we communicate. */
    protected SceneRegistry _screg;
}
