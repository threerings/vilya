package com.threerings.ezgame {

import flash.events.Event;

public class OccupantChangedEvent extends EZEvent
{
    public static const OCCUPANT_ENTERED :String = "OccupantEntered";
    public static const OCCUPANT_LEFT :String = "OccupantLeft";

    /** The occupantId of the occupant that entered or left. */
    public function get occupantId () :int
    {
        return _occupantId;
    }

    /** Is/was the occupant a player? If false, they are/were a watcher. */
    public function get player () :Boolean
    {
        return _player;
    }

    public function OccupantChangedEvent (
        type :String, ezgame :EZGameControl, occupantId :int, player :Boolean)
    {
        super(type, ezgame);
        _occupantId = occupantId;
        _player = player;
    }

    override public function toString () :String
    {
        return "[OccupantChangedEvent type=" + type +
            ", occupantId=" + _occupantId +
            ", player=" + _player + "]";
    }

    override public function clone () :Event
    {
        return new OccupantChangedEvent(type, _ezgame, _occupantId, _player);
    }

    protected var _occupantId :int;
    protected var _player :Boolean;
}
}
