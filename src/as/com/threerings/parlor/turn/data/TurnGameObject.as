//
// $Id: TurnGameObject.java 3667 2005-08-03 07:46:54Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.parlor.turn.data {

import com.threerings.io.TypedArray;

import com.threerings.util.Name;

import com.threerings.parlor.game.data.GameObject;

/**
 * Games that wish to support turn-based play must implement this
 * interface with their {@link GameObject}.
 */
public interface TurnGameObject
{
    /**
     * Returns the distributed object field name of the
     * <code>turnHolder</code> field in the object that implements this
     * interface.
     */
    function getTurnHolderFieldName () :String;

    /**
     * Returns the username of the player who is currently taking their
     * turn in this turn-based game or <code>null</code> if no user
     * currently holds the turn.
     */
    function getTurnHolder () :Name;

//    /**
//     * Requests that the <code>turnHolder</code> field be set to the specified
//     * value.
//     */
//    function setTurnHolder (turnHolder :Name) :void;

    /**
     * Returns the array of player names involved in the game.
     */
    function getPlayers () :TypedArray /* of Name */;

    /**
     * Returns true if the game is in play, false if not. If a game is not in
     * play after a turn has ended, the next turn will not be started.
     */
    function isInPlay () :Boolean;
}
}
