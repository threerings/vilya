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

package com.threerings.ezgame.data {

import com.threerings.io.SimpleStreamableObject;

import com.threerings.parlor.game.data.GameConfig;

/**
 * Used to configure the match-making interface for a game. Particular match-making mechanisms
 * extend this class and specify their own special configuration parameters.
 */
public /*abstract*/ class MatchConfig extends SimpleStreamableObject
{
    public function MatchConfig ()
    {
    }

    /** Returns the matchmaking type to use for this game, e.g. {@link GameConfig.SEATED_GAME}. */
    public function getMatchType () :int
    {
        throw new Error("Abstract");
    }

    /** Returns the minimum number of players needed to play this game. */
    public function getMinimumPlayers () :int
    {
        throw new Error("Abstract");
    }
}
}
