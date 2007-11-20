//
// $Id$

package com.whirled.util
{

/**
 * Interface for game modes, handled by the GameModeManager. Game modes are notified when they
 * become activated and deactivated, so that they can adjust themselves accordingly.
 */
public interface GameMode
{
    /**
     * Called when this instance of GameMode is added to the top of the stack.
     */
    function pushed () :void;

    /**
     * Called when this instance of GameMode is removed from the top of the stack.
     */
    function popped () :void;

    /**
     * Called when this instance of GameMode was the top of the stack, but another instance
     * is being pushed on top of it.
     */
    function pushedOnto (mode :GameMode) :void;

    /**
     * Called when another instance is being removed from the top of the stack,
     * making this instance the new top.
     */
    function poppedFrom (mode :GameMode) :void;
}
}
