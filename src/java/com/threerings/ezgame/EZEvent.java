package com.threerings.ezgame;

public class EZEvent
{
    public EZEvent (EZGame ezgame)
    {
        _ezgame = ezgame;
    }

    /**
     * Access the game object to which this event applies.
     */
    public EZGame getGameObject ()
    {
        return _ezgame;
    }

    /** The game object for this event. */
    protected EZGame _ezgame;
}
