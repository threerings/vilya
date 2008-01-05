//
// $Id$

package com.threerings.ezgame.server;

public interface EZGameTurnDelegate
{
    /**
     * Start the next turn, specifying the id of the next turn holder or 0.
     */
    public void endTurn (int nextTurnHolderId);
}
