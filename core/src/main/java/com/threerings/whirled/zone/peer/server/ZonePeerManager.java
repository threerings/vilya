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

import com.google.common.base.Function;
import com.google.inject.Inject;
import com.samskivert.util.Lifecycle;
import com.samskivert.util.Tuple;

import com.threerings.util.Name;

import com.threerings.presents.peer.data.NodeObject;
import com.threerings.crowd.peer.server.CrowdPeerManager;

import com.threerings.whirled.zone.peer.data.HostedZone;

import static com.threerings.whirled.zone.Log.log;

public abstract class ZonePeerManager extends CrowdPeerManager
{
    /**
     * Creates an uninitialized peer manager.
     */
    @Inject public ZonePeerManager (Lifecycle cycle)
    {
        super(cycle);
    }

    @Override // from CrowdPeerManager
    protected NodeObject createNodeObject ()
    {
        return new ZoneNodeObject();
    }

    /** Returns a lock used to claim resolution of the specified scene. */
    public static NodeObject.Lock getZoneLock (int sceneId)
    {
        return new NodeObject.Lock("ZoneHost", sceneId);
    }

    /**
     * Returns the node name of the peer that is hosting the specified scene, or null if no peer
     * has published that they are hosting the scene.
     */
    public Tuple<String, HostedZone> getZoneHost (final int zoneId)
    {
        return lookupNodeDatum(new NodeFunc<Tuple<String, HostedZone>>() {
            @Override
            public Tuple<String, HostedZone> apply (ZoneNodeObject nodeobj) {
                HostedZone info = nodeobj.hostedZones.get(zoneId);
                return (info == null) ? null : Tuple.newTuple(nodeobj.nodeName, info);
            }
        });
    }

    /**
     * Called by the ZoneManager when it is hosting a zone.
     */
    public void zoneDidStartup (int zoneId, Name name)
    {
        log.debug("Hosting zone", "id", zoneId, "name", name);
        ((ZoneNodeObject)_nodeobj).addToHostedZones(new HostedZone(zoneId, name));
    }

    /**
     * Called by the ZoneManager when it is no longer hosting a scene.
     */
    public void zoneDidShutdown (int zoneId)
    {
        log.debug("No longer hosting zone", "id", zoneId);
        ((ZoneNodeObject)_nodeobj).removeFromHostedZones(zoneId);
    }

    /** Useful with {@link #lookupNodeDatum}. */
    public static abstract class NodeFunc<T> implements Function<NodeObject, T>
    {
        public abstract T apply (ZoneNodeObject mnobj);

        public T apply (NodeObject nodeobj) {
            return apply((ZoneNodeObject)nodeobj);
        }
    }
}
