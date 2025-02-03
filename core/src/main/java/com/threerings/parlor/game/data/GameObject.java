//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.game.data;

import javax.annotation.Generated;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.crowd.data.PlaceObject;

/**
 * A game object hosts the shared data associated with a game played by one or more players. The
 * game object extends the place object so that the game can act as a place where players actually
 * go when playing the game. Only very basic information is maintained in the base game object. It
 * serves as the base for a hierarchy of game object derivatives that handle basic gameplay for a
 * suite of different game types (ie. turn based games, party games, board games, card games,
 * etc.).
 */
public class GameObject extends PlaceObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>state</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String STATE = "state";

    /** The field name of the <code>isRated</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String IS_RATED = "isRated";

    /** The field name of the <code>isPrivate</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String IS_PRIVATE = "isPrivate";

    /** The field name of the <code>players</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String PLAYERS = "players";

    /** The field name of the <code>winners</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String WINNERS = "winners";

    /** The field name of the <code>sessionId</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String SESSION_ID = "sessionId";

    /** The field name of the <code>playerStatus</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String PLAYER_STATUS = "playerStatus";
    // AUTO-GENERATED: FIELDS END

    /** A game state constant indicating that the game has not yet started and is still awaiting
     * the arrival of all of the players. */
    public static final int PRE_GAME = 0;

    /** A game state constant indicating that the game is in play. */
    public static final int IN_PLAY = 1;

    /** A game state constant indicating that the game ended normally. */
    public static final int GAME_OVER = 2;

    /** A game state constant indicating that the game was cancelled. */
    public static final int CANCELLED = 3;

    /** The player status constant for a player whose game is in play. */
    public static final int PLAYER_IN_PLAY = 0;

    /** The player status constant for a player whose has been knocked out of the game. NOTE: This
     * can include a player choosing to leave a game prematurely. */
    public static final int PLAYER_LEFT_GAME = 1;

    /** The game state, one of {@link #PRE_GAME}, {@link #IN_PLAY}, {@link #GAME_OVER}, or
     * {@link #CANCELLED}. */
    public int state = PRE_GAME;

    /** Indicates whether or not this game is rated. */
    public boolean isRated;

    /** Indicates whether the game is "private". */
    public boolean isPrivate;

    /** The usernames of the players involved in this game. */
    public Name[] players;

    /** Whether each player in the game is a winner, or null if the game is not yet over. */
    public boolean[] winners;

    /** A unique identifier for each game session. Every time the game is started, this value will
     * be incremented to provide a unique identifier for that particular session. */
    public int sessionId;

    /**
     * If null, indicates that all present players are active, or for more complex games can be
     * non-null to indicate the current status of each player in the game. The status value is one
     * of {@link #PLAYER_LEFT_GAME} or {@link #PLAYER_IN_PLAY}.<p> Subclasses of GameObject may use
     * other means to determine a player's status in the game, so call {@link #isActivePlayer(int)}
     * to see if a player is still participating in a game.
     */
    public int[] playerStatus;

    /**
     * Returns the number of players in the game.
     */
    public int getPlayerCount ()
    {
        int count = 0;
        int size = players.length;
        for (int ii = 0; ii < size; ii++) {
            if (players[ii] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns the number of active players in the game.
     */
    public int getActivePlayerCount ()
    {
        int count = 0;
        int size = players.length;
        for (int ii = 0; ii < size; ii++) {
            if (isActivePlayer(ii)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns whether the given player is still an active player in the game.  (Ie. whether or not
     * they are still participating.)
     */
    public boolean isActivePlayer (int pidx)
    {
        return isOccupiedPlayer(pidx) &&
            (playerStatus == null || isActivePlayerStatus(playerStatus[pidx]));
    }

    /**
     * Returns the player index of the given user in the game, or <code>-1</code> if the player is
     * not involved in the game.
     */
    public int getPlayerIndex (Name username)
    {
        int size = (players == null) ? 0 : players.length;
        for (int ii = 0; ii < size; ii++) {
            if (players[ii] != null && players[ii].equals(username)) {
                return ii;
            }
        }
        return -1;
    }

    /**
     * Returns whether the game is in play.  A game that is not in play could either be awaiting
     * players, ended, or cancelled.
     */
    public boolean isInPlay ()
    {
        return (state == IN_PLAY);
    }

    /**
     * Returns whether the given player index in the game is occupied.
     */
    public boolean isOccupiedPlayer (int pidx)
    {
        return (pidx >= 0 && pidx < players.length) && (players[pidx] != null);
    }

    /**
     * Returns whether the given player index is a winner, or false if the winners are not yet
     * assigned.
     */
    public boolean isWinner (int pidx)
    {
        return (winners != null) && winners[pidx];
    }

    /**
     * Returns the number of winners for this game, or <code>0</code> if the winners array is not
     * populated, e.g., the game is not yet over.
     */
    public int getWinnerCount ()
    {
        int count = 0;
        int size = (winners == null) ? 0 : winners.length;
        for (int ii = 0; ii < size; ii++) {
            if (winners[ii]) {
                count++;
            }
        }
        return count;
    }

    /**
     * Returns true if the game is ended in a draw.
     */
    public boolean isDraw ()
    {
        return getWinnerCount() == getPlayerCount();
    }

    /**
     * Returns the winner index of the first winning player for this game, or <code>-1</code> if
     * there are no winners or the winners array is not yet assigned.  This is only likely to be
     * useful for games that are known to have a single winner.
     */
    public int getWinnerIndex ()
    {
        int size = (winners == null) ? 0 : winners.length;
        for (int ii = 0; ii < size; ii++) {
            if (winners[ii]) {
                return ii;
            }
        }
        return -1;
    }

    @SuppressWarnings("deprecation")
    public void sendPlayerReady  () {
        manager.invoke("playerReady");
    }

    /**
     * Used by {@link #isActivePlayer} to determine if the supplied status is associated with an
     * active player (one that has not resigned from the game and/or left the game room).
     */
    protected boolean isActivePlayerStatus (int playerStatus)
    {
        return playerStatus == PLAYER_IN_PLAY;
    }

    @Override
    protected void which (StringBuilder buf)
    {
        super.which(buf);
        StringUtil.toString(buf, players);
        buf.append(":").append(state);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>state</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setState (int value)
    {
        int ovalue = this.state;
        requestAttributeChange(
            STATE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.state = value;
    }

    /**
     * Requests that the <code>isRated</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setIsRated (boolean value)
    {
        boolean ovalue = this.isRated;
        requestAttributeChange(
            IS_RATED, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.isRated = value;
    }

    /**
     * Requests that the <code>isPrivate</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setIsPrivate (boolean value)
    {
        boolean ovalue = this.isPrivate;
        requestAttributeChange(
            IS_PRIVATE, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.isPrivate = value;
    }

    /**
     * Requests that the <code>players</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPlayers (Name[] value)
    {
        Name[] ovalue = this.players;
        requestAttributeChange(
            PLAYERS, value, ovalue);
        this.players = (value == null) ? null : value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>players</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPlayersAt (Name value, int index)
    {
        Name ovalue = this.players[index];
        requestElementUpdate(
            PLAYERS, index, value, ovalue);
        this.players[index] = value;
    }

    /**
     * Requests that the <code>winners</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setWinners (boolean[] value)
    {
        boolean[] ovalue = this.winners;
        requestAttributeChange(
            WINNERS, value, ovalue);
        this.winners = (value == null) ? null : value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>winners</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setWinnersAt (boolean value, int index)
    {
        boolean ovalue = this.winners[index];
        requestElementUpdate(
            WINNERS, index, Boolean.valueOf(value), Boolean.valueOf(ovalue));
        this.winners[index] = value;
    }

    /**
     * Requests that the <code>sessionId</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setSessionId (int value)
    {
        int ovalue = this.sessionId;
        requestAttributeChange(
            SESSION_ID, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.sessionId = value;
    }

    /**
     * Requests that the <code>playerStatus</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPlayerStatus (int[] value)
    {
        int[] ovalue = this.playerStatus;
        requestAttributeChange(
            PLAYER_STATUS, value, ovalue);
        this.playerStatus = (value == null) ? null : value.clone();
    }

    /**
     * Requests that the <code>index</code>th element of
     * <code>playerStatus</code> field be set to the specified value.
     * The local value will be updated immediately and an event will be
     * propagated through the system to notify all listeners that the
     * attribute did change. Proxied copies of this object (on clients)
     * will apply the value change when they received the attribute
     * changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setPlayerStatusAt (int value, int index)
    {
        int ovalue = this.playerStatus[index];
        requestElementUpdate(
            PLAYER_STATUS, index, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.playerStatus[index] = value;
    }
    // AUTO-GENERATED: METHODS END
}
