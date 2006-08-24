package com.threerings.ezgame {

/**
 * Implement this interface to automagically be registered to received
 * MessageReceivedEvents.
 */
public interface MessageReceivedListener
{
    /**
     * Handle a MessageReceivedEvent.
     */
    function messageReceived (event :MessageReceivedEvent) :void;
}
}
