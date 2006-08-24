package com.threerings.ezgame {

/**
 * Implement this interface to automagically be registered to received
 * StateChangedEvents.
 */
public interface StateChangedListener
{
    /**
     * Handle a StateChangedEvent.
     */
    function stateChanged (event :StateChangedEvent) :void;
}
}
