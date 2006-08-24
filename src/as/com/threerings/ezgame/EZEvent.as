package com.threerings.ezgame {

import flash.events.Event;

// TODO: there may not be much point in using the standard flash event
// architecture. I'm about thiiiiiiis close to not.
//
public /*abstract*/ class EZEvent extends Event
{
    /**
     * Access the game object to which this event applies.
     */
    public function get gameObject () :EZGame
    {
        return _ezgame;
    }

    public function EZEvent (type :String, ezgame :EZGame)
    {
        super(type);
        _ezgame = ezgame;
    }

    /** The game object for this event. */
    protected var _ezgame :EZGame;
}
}
