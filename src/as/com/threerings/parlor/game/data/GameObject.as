//
// $Id: GameObject.java 4191 2006-06-13 22:42:20Z ray $
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

package com.threerings.parlor.game.data {

import com.threerings.util.ArrayUtil;
import com.threerings.util.Integer;
import com.threerings.util.langBoolean;
import com.threerings.util.Name;
import com.threerings.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.crowd.data.PlaceObject;

/**
 * A game object hosts the shared data associated with a game played by
 * one or more players. The game object extends the place object so that
 * the game can act as a place where players actually go when playing the
 * game. Only very basic information is maintained in the base game
 * object. It serves as the base for a hierarchy of game object
 * derivatives that handle basic gameplay for a suite of different game
 * types (ie. turn based games, party games, board games, card games,
 * etc.).
 */
public class GameObject extends PlaceObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>gameService</code> field. */
    public static const GAME_SERVICE :String = "gameService";

    /** The field name of the <code>state</code> field. */
    public static const STATE :String = "state";

    /** The field name of the <code>isRated</code> field. */
    public static const IS_RATED :String = "isRated";

    /** The field name of the <code>isPrivate</code> field. */
    public static const IS_PRIVATE :String = "isPrivate";

    /** The field name of the <code>players</code> field. */
    public static const PLAYERS :String = "players";

    /** The field name of the <code>winners</code> field. */
    public static const WINNERS :String = "winners";

    /** The field name of the <code>roundId</code> field. */
    public static const ROUND_ID :String = "roundId";

    /** The field name of the <code>playerStatus</code> field. */
    public static const PLAYER_STATUS :String = "playerStatus";
    // AUTO-GENERATED: FIELDS END

    /** A game state constant indicating that the game has not yet started
     * and is still awaiting the arrival of all of the players. */
    public static const PRE_GAME :int = 0;

    /** A game state constant indicating that the game is in play. */
    public static const IN_PLAY :int = 1;

    /** A game state constant indicating that the game ended normally. */
    public static const GAME_OVER :int = 2;

    /** A game state constant indicating that the game was cancelled. */
    public static const CANCELLED :int = 3;

    /** The player status constant for a player whose game is in play. */
    public static const PLAYER_IN_PLAY :int = 0;

    /** The player status constant for a player whose has been knocked out
     * of the game. NOTE: This can include a player choosing to leave a
     * game prematurely. */
    public static const PLAYER_LEFT_GAME :int = 1;

    /** Provides general game invocation services. */
    public var gameService :GameMarshaller;

    /** The game state, one of {@link #PRE_GAME}, {@link #IN_PLAY},
     * {@link #GAME_OVER}, or {@link #CANCELLED}. */
    public var state :int = PRE_GAME;

    /** Indicates whether or not this game is rated. */
    public var isRated :Boolean;

    /** Indicates whether the game is "private". */
    public var isPrivate :Boolean;

    /** The usernames of the players involved in this game. */
    public var players :TypedArray; /* of Name */

    /** Whether each player in the game is a winner, or <code>null</code>
     * if the game is not yet over. */
    public var winners :TypedArray; /* of Boolean */

    /** The unique round identifier for the current round. */
    public var roundId :int;

    /** If null, indicates that all present players are active, or for
     * more complex games can be non-null to indicate the current status
     * of each player in the game. The status value is one of
     * {@link #PLAYER_LEFT_GAME} or {@link #PLAYER_IN_PLAY}. */
    public var playerStatus :TypedArray; /* of int */

    /**
     * Returns the number of players in the game.
     */
    public function getPlayerCount () :int
    {
        var count :int = 0;
        var size :int = players.length;
        for (var ii :int = 0; ii < size; ii++) {
            if (players[ii] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of active players in the game.
     */
    public function getActivePlayerCount () :int
    {
        var count :int = 0;
        var size :int = players.length;
        for (var ii :int = 0; ii < size; ii++) {
            if (isActivePlayer(ii)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns whether the given player is still an active player in
     * the game.  (Ie. whether or not they are still participating.)
     */
    public function isActivePlayer (pidx :int) :Boolean
    {
        return isOccupiedPlayer(pidx) &&
            (playerStatus == null || isActivePlayerStatus(playerStatus[pidx]));
    }

    /**
     * Returns the player index of the given user in the game, or 
     * <code>-1</code> if the player is not involved in the game.
     */
    public function getPlayerIndex (username :Name) :int
    {
        return ArrayUtil.indexOf(players, username);
    }

    /**
     * Returns whether the game is in play.  A game that is not in play
     * could either be awaiting players, ended, or cancelled.
     */
    public function isInPlay () :Boolean
    {
        return (state == IN_PLAY);
    }

    /**
     * Returns whether the given player index in the game is occupied.
     */
    public function isOccupiedPlayer (pidx :int) :Boolean
    {
        return (pidx >= 0 && pidx < players.length) && (players[pidx] != null);
    }

    /**
     * Returns whether the given player index is a winner, or false if the
     * winners are not yet assigned.
     */
    public function isWinner (pidx :int) :Boolean
    {
        return (winners != null) && winners[pidx];
    }

    /**
     * Returns the number of winners for this game, or <code>0</code> if
     * the winners array is not populated, e.g., the game is not yet over.
     */
    public function getWinnerCount () :int
    {
        var count :int = 0;
        if (winners != null) {
            for (var ii :int = 0; ii < winners.length; ii++) {
                if (winners[ii]) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Returns true if the game is ended in a draw.
     */
    public function isDraw () :Boolean
    {
        return getWinnerCount() == getPlayerCount();
    }

    /**
     * Returns the winner index of the first winning player for this game,
     * or <code>-1</code> if there are no winners or the winners array is
     * not yet assigned.  This is only likely to be useful for games that
     * are known to have a single winner.
     */
    public function getWinnerIndex () :int
    {
        return ArrayUtil.indexOf(winners, true);
    }

    /**
     * Returns the type of party game being played or NOT_PARTY_GAME.
     * {@link PartyGameConfig}.
     */
    public function getPartyGameType () :int
    {
        return PartyGameCodes.NOT_PARTY_GAME;
    }

    /**
     * Used by {@link #isActivePlayer} to determine if the supplied status is
     * associated with an active player (one that has not resigned from the
     * game and/or left the game room).
     */
    protected function isActivePlayerStatus (playerStatus :int) :Boolean
    {
        return playerStatus == PLAYER_IN_PLAY;
    }

    override protected function whichBuf (buf :StringBuilder) :void
    {
        super.whichBuf(buf);
        buf.append("(").append(players.join()).append(")");
        buf.append(":").append(state);
    }

//    // AUTO-GENERATED: METHODS START
//    /**
//     * Requests that the <code>gameService</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setGameService (value :GameMarshaller) :void
//    {
//        var ovalue :GameMarshaller = this.gameService;
//        requestAttributeChange(
//            GAME_SERVICE, value, ovalue);
//        this.gameService = value;
//    }
//
//    /**
//     * Requests that the <code>state</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setState (value :int) :void
//    {
//        var ovalue :int = this.state;
//        requestAttributeChange(
//            STATE, Integer.valueOf(value), Integer.valueOf(ovalue));
//        this.state = value;
//    }
//
//    /**
//     * Requests that the <code>isRated</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setIsRated (value :Boolean) :void
//    {
//        var ovalue :Boolean = this.isRated;
//        requestAttributeChange(
//            IS_RATED, langBoolean.valueOf(value), langBoolean.valueOf(ovalue));
//        this.isRated = value;
//    }
//
//    /**
//     * Requests that the <code>isPrivate</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setIsPrivate (value :Boolean) :void
//    {
//        var ovalue :Boolean = this.isPrivate;
//        requestAttributeChange(
//            IS_PRIVATE, langBoolean.valueOf(value),
//            langBoolean.valueOf(ovalue));
//        this.isPrivate = value;
//    }
//
//    /**
//     * Requests that the <code>players</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setPlayers (value :TypedArray) :void
//    {
//        var ovalue :TypedArray = this.players;
//        requestAttributeChange(
//            PLAYERS, value, ovalue);
//        this.players = (value == null) ? null : (value.clone() as TypedArray);
//    }
//
//    /**
//     * Requests that the <code>index</code>th element of
//     * <code>players</code> field be set to the specified value.
//     * The local value will be updated immediately and an event will be
//     * propagated through the system to notify all listeners that the
//     * attribute did change. Proxied copies of this object (on clients)
//     * will apply the value change when they received the attribute
//     * changed notification.
//     */
//    public function setPlayersAt (value :Name, index :int) :void
//    {
//        var ovalue :Name = (this.players[index] as Name);
//        requestElementUpdate(
//            PLAYERS, index, value, ovalue);
//        this.players[index] = value;
//    }
//
//    /**
//     * Requests that the <code>winners</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setWinners (value :TypedArray) :void
//    {
//        var ovalue :TypedArray = this.winners;
//        requestAttributeChange(
//            WINNERS, value, ovalue);
//        this.winners = (value == null) ? null : (value.clone() as TypedArray);
//    }
//
//    /**
//     * Requests that the <code>index</code>th element of
//     * <code>winners</code> field be set to the specified value.
//     * The local value will be updated immediately and an event will be
//     * propagated through the system to notify all listeners that the
//     * attribute did change. Proxied copies of this object (on clients)
//     * will apply the value change when they received the attribute
//     * changed notification.
//     */
//    public function setWinnersAt (value :Boolean, index :int) :void
//    {
//        var ovalue :Boolean = (this.winners[index] as Boolean);
//        requestElementUpdate(
//            WINNERS, index, langBoolean.valueOf(value),
//            langBoolean.valueOf(ovalue));
//        this.winners[index] = value;
//    }
//
//    /**
//     * Requests that the <code>roundId</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setRoundId (value :int) :void
//    {
//        var ovalue :int = this.roundId;
//        requestAttributeChange(
//            ROUND_ID, Integer.valueOf(value), Integer.valueOf(ovalue));
//        this.roundId = value;
//    }
//
//    /**
//     * Requests that the <code>playerStatus</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setPlayerStatus (value :TypedArray) :void
//    {
//        var ovalue :TypedArray = this.playerStatus;
//        requestAttributeChange(
//            PLAYER_STATUS, value, ovalue);
//        this.playerStatus = (value == null) ? null
//            : (value.clone() as TypedArray);
//    }
//
//    /**
//     * Requests that the <code>index</code>th element of
//     * <code>playerStatus</code> field be set to the specified value.
//     * The local value will be updated immediately and an event will be
//     * propagated through the system to notify all listeners that the
//     * attribute did change. Proxied copies of this object (on clients)
//     * will apply the value change when they received the attribute
//     * changed notification.
//     */
//    public function setPlayerStatusAt (value :int, index :int) :void
//    {
//        var ovalue :int = (this.playerStatus[index] as int);
//        requestElementUpdate(
//            PLAYER_STATUS, index, Integer.valueOf(value),
//            Integer.valueOf(ovalue));
//        this.playerStatus[index] = value;
//    }
//    // AUTO-GENERATED: METHODS END
//
//    override public function writeObject (out :ObjectOutputStream) :void
//    {
//        super.writeObject(out);
//
//        out.writeObject(gameService);
//        out.writeInt(state);
//        out.writeBoolean(isRated);
//        out.writeBoolean(isPrivate);
//        out.writeObject(players);
//        out.writeField(winners);
//        out.writeInt(roundId);
//        out.writeField(playerStatus);
//    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        gameService = (ins.readObject() as GameMarshaller);
        state = ins.readInt();
        isRated = ins.readBoolean();
        isPrivate = ins.readBoolean();
        players = (ins.readObject() as TypedArray);
        winners = (ins.readField(TypedArray.getJavaType(Boolean))
            as TypedArray);
        roundId = ins.readInt();
        playerStatus = (ins.readField(TypedArray.getJavaType(int))
            as TypedArray);
    }
}
}
