package com.threerings.ezgame;

/**
 * Implement this interface to automagically be registered to receive
 * StateChangedEvents.
 */
public interface StateChangedListener
{
    /**
     * Handle a StateChangedEvent.
     */
    public void stateChanged (StateChangedEvent event);
}
