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

import javax.annotation.Generated;
import com.threerings.presents.dobj.DSet;
import com.threerings.crowd.peer.data.CrowdNodeObject;

import com.threerings.whirled.zone.peer.data.HostedZone;

public class ZoneNodeObject extends CrowdNodeObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>hostedZones</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String HOSTED_ZONES = "hostedZones";
    // AUTO-GENERATED: FIELDS END

    /** Contains info on all zones hosted by this server. */
    public DSet<HostedZone> hostedZones = DSet.newDSet();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>hostedZones</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToHostedZones (HostedZone elem)
    {
        requestEntryAdd(HOSTED_ZONES, hostedZones, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>hostedZones</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromHostedZones (Comparable<?> key)
    {
        requestEntryRemove(HOSTED_ZONES, hostedZones, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>hostedZones</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateHostedZones (HostedZone elem)
    {
        requestEntryUpdate(HOSTED_ZONES, hostedZones, elem);
    }

    /**
     * Requests that the <code>hostedZones</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setHostedZones (DSet<HostedZone> value)
    {
        requestAttributeChange(HOSTED_ZONES, value, this.hostedZones);
        DSet<HostedZone> clone = (value == null) ? null : value.clone();
        this.hostedZones = clone;
    }
    // AUTO-GENERATED: METHODS END
}
