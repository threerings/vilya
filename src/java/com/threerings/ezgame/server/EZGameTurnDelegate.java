//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.ezgame.server;

import com.samskivert.util.ListUtil;
import com.samskivert.util.RandomUtil;

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
    protected void setFirstTurnHolder ()
    {
        // make it nobody's turn
        _turnIdx = -1;
    }

    @Override
    protected void setNextTurnHolder ()
    {
        // if the user-supplied value seems to make sense, use it!
        if (_nextPlayer != null) {
            // copy the value, clear out _nextPlayer.
            Name nextPlayer = _nextPlayer;
            _nextPlayer = null;

            int index = ListUtil.indexOf(_turnGame.getPlayers(), nextPlayer);
            if (index != -1) {
                _turnIdx = index;
                return;
            }

        } else if (_turnIdx == -1) {
            // If it's nobody's turn, and the user does not specify a next-turn, randomize.
            // We set turnIdx to the player we want - 1, so that when it gets inc'd it'll
            // be right.
            _turnIdx = RandomUtil.getInt(_turnGame.getPlayers().length) - 1;
        }

        // otherwise, do the default behavior
        super.setNextTurnHolder();
    }

    /** An override next turn holder, or null. */
    protected Name _nextPlayer;
}
