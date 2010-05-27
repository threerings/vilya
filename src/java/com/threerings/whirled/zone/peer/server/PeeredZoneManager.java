package com.threerings.whirled.zone.peer.server;

import com.threerings.whirled.zone.peer.server.PeeredZoneRegistry.PeerZoneShutdownListener;
import com.threerings.whirled.zone.server.ZoneManager;

public interface PeeredZoneManager
    extends ZoneManager
{
    /** Registers a shutdown listener with the zone. */
    public void setShutdownListener (int zoneId, PeerZoneShutdownListener shutdowner);
}
