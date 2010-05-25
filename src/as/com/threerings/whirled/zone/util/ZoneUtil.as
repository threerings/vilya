package com.threerings.whirled.zone.util {

/**
 * Server-specific, zone-related utility functions.
 */
public class ZoneUtil
{
    /**
     * Composes the zone type and zone id into a qualified zone id. A
     * qualified zone id is what should be passed around so that the
     * server can determine the zone type from the zone id when necessary.
     */
    public static function qualifyZoneId (zoneType :int, zoneId :int) :int
    {
        var qualifiedZoneId :int = zoneType;
        qualifiedZoneId <<= 24;
        qualifiedZoneId |= zoneId;
        return qualifiedZoneId;
    }

    /**
     * Extracts the zone type from a qualified zone id.
     */
    public static function zoneType (qualifiedZoneId :int) :int
    {
        return (0xFF000000 & qualifiedZoneId) >> 24;
    }

    /**
     * Extracts the zone id from a qualified zone id.
     */
    public static function zoneId (qualifiedZoneId :int) :int
    {
        return (0x00FFFFFF & qualifiedZoneId);
    }

    /**
     * Returns an easier to read representation of the supplied qualified
     * zone id: <code>type:id</code>.
     */
    public static function toString (qualifiedZoneId :int) :String
    {
        return zoneType(qualifiedZoneId) + ":" + zoneId(qualifiedZoneId);
    }
}
}
