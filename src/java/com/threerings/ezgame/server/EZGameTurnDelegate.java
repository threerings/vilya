//
// $Id$

package com.threerings.ezgame.server;

import com.samskivert.util.ListUtil;

import com.threerings.util.Name;

import com.threerings.parlor.turn.server.TurnGameManagerDelegate;

/**
 * A special turn delegate for ez games.
 */
public class EZGameTurnDelegate extends TurnGameManagerDelegate
{
    public EZGameTurnDelegate (EZGameManager mgr)
    {
        super(mgr);
    }

    /**
     * A form of endTurn where you can specify the next turn holder oid.
     */
    public void endTurn (Name nextPlayer)
    {
        _nextPlayer = nextPlayer;
        endTurn();
    }

    @Override
    protected void setNextTurnHolder ()
    {
        // if the user-supplied value seems to make sense, use it!
        if (_nextPlayer != null) {
            // copy the value, clear out _nextPlayer.
            Name nextPlayer = _nextPlayer;
            _nextPlayer = null;

            int index = ListUtil.indexOf(_turnGame.getPlayers(), _nextPlayer);
            if (index != -1) {
                _turnIdx = index;
                return;
            }
        }

        // otherwise, do the default behavior
        super.setNextTurnHolder();
    }

    /** An override next turn holder, or null. */
    protected Name _nextPlayer;
}
