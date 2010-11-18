//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
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

package com.threerings.parlor.game.data {

import com.threerings.presents.data.InvocationCodes;

/**
 * Constants relating to the game services.
 */
public class GameCodes extends InvocationCodes
{
    /** The message bundle identifier for general game messages. */
    public static const GAME_MESSAGE_BUNDLE :String = "game.general";

    /** The name of the message event to a placeObject that a player
     * was knocked out of a puzzle. */
    public static const PLAYER_KNOCKED_OUT :String = "playerKnocked";

    /** The name of the message event to a placeObject that reports
     * the winners and losers of a game. */
    public static const WINNERS_AND_LOSERS :String = "winnersAndLosers";

    /** A chat type for chatting on the game object. */
    public static const GAME_CHAT_TYPE :String = "gameChat";
}
}
