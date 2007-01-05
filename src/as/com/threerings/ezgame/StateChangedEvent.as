package com.threerings.ezgame {

import flash.events.Event;

/**
 * Dispatched when the state of the game has changed.
 */
public class StateChangedEvent extends EZEvent
{
    /** Indicates that the game has transitioned to a started state. */
    public static const GAME_STARTED :String = "GameStarted";

    /** Indicates that the game has transitioned to a ended state. */
    public static const GAME_ENDED :String = "GameEnded";

    /** Indicates that the turn has changed. */
    // TODO: move to own event?
    public static const TURN_CHANGED :String = "TurnChanged";

    public function StateChangedEvent (type :String, ezgame :EZGameControl)
    {
        super(type, ezgame);
    }

    override public function toString () :String
    {
        return "[StateChangedEvent type=" + type + "]";
    }

    override public function clone () :Event
    {
        return new StateChangedEvent(type, _ezgame);
    }
}
}
