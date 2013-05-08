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

package com.threerings.whirled.zone.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.BodyLocator;
import com.threerings.crowd.server.LocationManager;
import com.threerings.crowd.server.LocationProvider;

import com.threerings.whirled.server.SceneRegistry;
import com.threerings.whirled.zone.client.ZoneService;
import com.threerings.whirled.zone.data.ZoneCodes;
import com.threerings.whirled.zone.data.ZoneMarshaller;
import com.threerings.whirled.zone.data.ZonedBodyObject;
import com.threerings.whirled.zone.util.ZoneUtil;

import static com.threerings.whirled.zone.Log.log;

/**
 * The zone registry takes care of mapping zone requests to the appropriate registered zone manager.
 */
@Singleton
public class ZoneRegistry
    implements ZoneProvider
{
    /**
     * Creates a zone manager with the supplied configuration.
     */
    @Inject public ZoneRegistry (InvocationManager invmgr)
    {
        invmgr.registerProvider(this, ZoneMarshaller.class, ZoneCodes.WHIRLED_GROUP);
    }

    /**
     * Registers the supplied zone manager as the manager for the specified zone type. Zone types
     * are 7 bits and managers are responsible for making sure they don't use a zone type that
     * collides with another manager (given that we have only three zone types at present, this
     * doesn't seem unreasonable).
     */
    public void registerZoneManager (byte zoneType, ZoneManager manager)
    {
        ZoneManager old = _managers.get(zoneType);
        if (old != null) {
            log.warning("Zone manager already registered with requested type",
                "type", zoneType, "old", old, "new", manager);
        } else {
            _managers.put(zoneType, manager);
        }
    }

    /**
     * Returns the zone manager that handles the specified zone id.
     *
     * @param qualifiedZoneId the qualified zone id for which the manager should be looked up.
     */
    public ZoneManager getZoneManager (int qualifiedZoneId)
    {
        return _managers.get(ZoneUtil.zoneType(qualifiedZoneId));
    }

    /**
     * Ejects the specified body from their current scene and sends them a request to move to the
     * specified new zone and scene. This is the zone-equivalent to
     * {@link LocationProvider#moveTo}.
     *
     * @return null if the user was forcibly moved, or a string indicating the reason for denial
     * of departure of their current zone (from {@link ZoneManager#ratifyBodyExit}).
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
     * {@link LocationProvider#leavePlace}.
     *
     * @return null if the user was forcibly moved, or a string indicating the reason for denial of
     * departure of their current zone (from {@link ZoneManager#ratifyBodyExit}).
     */
    public String leaveOccupiedZone (ZonedBodyObject source)
    {
        // look up the caller's current zone id and make sure it is happy about their departure
        // from the current zone
        ZoneManager zmgr = getZoneManager(source.getZoneId());
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

    // from interface ZoneProvider
    public void moveTo (ClientObject caller, int zoneId, int sceneId,
                        int sceneVer, ZoneService.ZoneMoveListener listener)
        throws InvocationException
    {
        if (!(caller instanceof ZonedBodyObject)) {
            log.warning("Request to switch zones by non-ZonedBodyObject",
                "clobj", caller.getClass());
            throw new InvocationException(ZoneCodes.INTERNAL_ERROR);
        }

        // look up the caller's current zone id and make sure it is happy about their departure
        // from the current zone
        BodyObject body = _locator.forClient(caller);
        ZoneManager ozmgr = getZoneManager(((ZonedBodyObject)caller).getZoneId());
        if (ozmgr != null) {
            String msg = ozmgr.ratifyBodyExit(body);
            if (msg != null) {
                throw new InvocationException(msg);
            }
        }

        // look up the zone manager for the zone
        ZoneManager zmgr = getZoneManager(zoneId);
        if (zmgr == null) {
            log.warning("Requested to enter a zone for which we have no manager",
                "user", body.who(), "zoneId", zoneId);
            throw new InvocationException(ZoneCodes.NO_SUCH_ZONE);
        }

        // resolve the zone and move the user
        zmgr.resolveZone(zoneId, createZoneMoveHandler(zmgr, body, sceneId, sceneVer, listener));
    }

    /**
     * Creates a handler to handle the described zone movement.
     */
    protected ZoneMoveHandler createZoneMoveHandler (
        ZoneManager zmgr, BodyObject body, int sceneId, int sceneVer,
        ZoneService.ZoneMoveListener listener)
    {
        return new ZoneMoveHandler(_locman, zmgr, _screg, body, sceneId, sceneVer, listener);
    }

    /** A table of zone managers. */
    protected IntMap<ZoneManager> _managers = IntMaps.newHashIntMap();

    /** Provides access to scene managers by scene. */
    @Inject protected SceneRegistry _screg;

    /** Provides location services. */
    @Inject protected LocationManager _locman;

    /** Used to translate ClientObjects into BodyObjects. */
    @Inject protected BodyLocator _locator;
}
