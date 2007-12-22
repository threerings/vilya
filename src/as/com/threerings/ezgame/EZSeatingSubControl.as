//
// $Id: SeatingControl.as 271 2007-04-07 00:25:58Z dhoover $
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


/**
 * Access seating information for a seated game. Do not instantiate this class directly,
 * access it via GameControl.game.seating.
 */
// TODO: methods for allowing a player to pick a seat in SEATED_CONTINUOUS games.
public class EZSeatingSubControl extends AbstractSubControl
{
    public function EZSeatingSubControl (parent :AbstractControl, game :EZGameSubControl)
    {
        super(parent);
        _game = game;
    }

    /**
     * Get the player's position (seated index), or -1 if not a player.
     */
    public function getPlayerPosition (playerId :int) :int
    {
        return int(callHostCode("getPlayerPosition_v1", playerId));
    }

    /**
     * A convenient function to get our own player position,
     * or -1 if we're not a player.
     */
    public function getMyPosition () :int
    {
        return int(callHostCode("getMyPosition_v1"));
    }

    /**
     * Get all the players at the table, in their seated position.
     * Absent players will be represented by a 0.
     */
    public function getPlayerIds () :Array /* of playerId (int) */
    {
        return (callHostCode("getPlayers_v1") as Array);
    }

    /**
     * Get the names of the seated players, in the order of their seated position.
     */
    public function getPlayerNames () :Array /* of String */
    {
        return getPlayerIds().map(
            function (playerId :int, o2:*, o3:*) :String
            {
                return _game.getOccupantName(playerId);
            }
        );
    }

    /** Our direct parent. */
    protected var _game :EZGameSubControl;
}
}
