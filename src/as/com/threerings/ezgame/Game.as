package com.threerings.ezgame {

/**
 * This interface should be implemented by some class that lives on the
 * display list. When a DisplayObject that implements this interface
 * is added, it will be notified of the GameObject.
 */
public interface Game
{
    /**
     * Called by the mysterious powers of the cosmos to initialize
     * your game with the GameObject.
     */
    function setGameObject (gameObj :EZGame) :void;
}
}
