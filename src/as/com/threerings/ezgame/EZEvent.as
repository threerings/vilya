package com.threerings.ezgame {

import flash.events.Event;

// TODO: there may not be much point in using the standard flash event
// architecture. I'm about thiiiiiiis close to not.
//
public /*abstract*/ class EZEvent extends Event
{
    /**
     * Access the game control to which this event applies.
     */
    public function get gameControl () :EZGameControl
    {
        return _ezgame;
    }

    public function EZEvent (type :String, ezgame :EZGameControl)
    {
        super(type);
        _ezgame = ezgame;
    }

    /** The game control for this event. */
    protected var _ezgame :EZGameControl;
}
}
