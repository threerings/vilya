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

package com.threerings.ezgame.data;

import com.threerings.parlor.game.data.GameConfig;

/**
 * Extends {@link MatchConfig} with information about match-making in the table style.
 */
public class TableMatchConfig extends MatchConfig
{
    /** The minimum number of seats at this table. */
    public int minSeats;

    /** The starting setting for the number of seats at this table. */
    public int startSeats;

    /** The maximum number of seats at this table. */
    public int maxSeats;

    /** This is set to true if this is a party game. */
    public boolean isPartyGame;

    @Override // from MatchConfig
    public int getMatchType ()
    {
        return isPartyGame ? GameConfig.PARTY : GameConfig.SEATED_GAME;
    }

    @Override // from MatchConfig
    public int getMinimumPlayers ()
    {
        return minSeats;
    }
}
