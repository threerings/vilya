package com.threerings.whirled.zone.peer.data;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

/**
 * Represents a zone hosted on some node.
 */
public class HostedZone extends SimpleStreamableObject
    implements DSet.Entry
{
    /** The unique ID for the zone in question. */
    public int zoneId;

    /** The name of the zone being hosted. */
    public Name name;

    public HostedZone ()
    {
    }

    public HostedZone (int zoneId, Name name)
    {
        this.zoneId = zoneId;
        this.name = name;
    }

    // from DSet.Entry
    public Comparable<?> getKey ()
    {
        return zoneId;
    }


}
