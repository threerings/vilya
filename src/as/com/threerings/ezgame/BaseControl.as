package com.threerings.ezgame {

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;

/**
 * The abstract base class for EZ controls.
 */
public class BaseControl extends EventDispatcher
{
    public function BaseControl ()
    {
        if (Object(this).constructor == BaseControl) {
            throw new IllegalOperationError("Abstract");
        }
    }

    /**
     * Your own events may not be dispatched here.
     */
    override public function dispatchEvent (event :Event) :Boolean
    {
        // Ideally we want to not be an EventDispatcher so that people
        // won't try to do this on us, but if we do that, then some other
        // object will be the target during dispatch, and that's confusing.
        throw new IllegalOperationError();
    }

    /**
     * Secret function to dispatch property changed events.
     */
    internal function dispatch (event :Event) :void
    {
        try {
            super.dispatchEvent(event);
        } catch (err :Error) {
            trace("Error dispatching event to user game.");
            trace(err.getStackTrace());
        }
    }
}
}
