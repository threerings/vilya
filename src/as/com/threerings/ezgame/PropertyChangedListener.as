package com.threerings.ezgame {

/**
 * Implement this interface to automagically be registered to received
 * PropertyChangedEvents.
 */
public interface PropertyChangedListener
{
    /**
     * Handle a PropertyChangedEvent.
     */
    function propertyChanged (event :PropertyChangedEvent) :void;
}
}
