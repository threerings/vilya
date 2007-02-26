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

package com.threerings.ezgame {

public class SeatingControl extends SubControl
{
    public function SeatingControl (ctrl :EZGameControl)
    {
        super(ctrl);
    }

    /**
     * Get the player's position (seated index), or -1 if not a player.
     */
    public function getPlayerPosition (playerId :int) :int
    {
        return int(_ctrl.callEZCodeFriend("getPlayerPosition_v1", playerId));
    }

    /**
     * A convenient function to get our own player position,
     * or -1 if we're not a player.
     */
    public function getMyPosition () :int
    {
        return int(_ctrl.callEZCodeFriend("getMyPosition_v1"));
    }

    /**
     * Get all the players at the table, in their seated position.
     * Absent players will be represented by a 0.
     */
    public function getPlayerIds () :Array /* of playerId (int) */
    {
        return (_ctrl.callEZCodeFriend("getPlayers_v1") as Array);
    }

    // TODO: methods for allowing a player to pick a seat
}
}
