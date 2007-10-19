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

package com.threerings.parlor.data;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.util.ActionScript;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.data.GameConfig;

/**
 * This class represents a table that is being used to matchmake a game by the Parlor services.
 */
public class Table
    implements DSet.Entry, ParlorCodes
{
    /** The unique identifier for this table. */
    public int tableId;

    /** The object id of the lobby object with which this table is associated. */
    public int lobbyOid;

    /** The oid of the game that was created from this table or -1 if the table is still in
     * matchmaking mode. */
    public int gameOid = -1;

    /** An array of the usernames of the occupants of this table (some slots may not be filled), or
     * null if a party game. */
    public Name[] occupants;

    /** The body oids of the occupants of this table, or null if a party game.  (This is not
     * propagated to remote instances.) */
    public transient int[] bodyOids;

    /** For a running game, the total number of players. For FFA party games, this is everyone. */
    public short playerCount;

    /** For a running game, the total number of watchers. For FFA party games, this is always 0. */
    public short watcherCount;

    /** The game config for the game that is being matchmade. */
    public GameConfig config;

    /** The table configuration object. */
    public TableConfig tconfig;

    /**
     * Constructs a blank table instance, suitable for unserialization.
     */
    public Table ()
    {
    }

    /**
     * Initializes a new table instance, and assigns it the next monotonically increasing table id.
     *
     * @param lobbyOid the object id of the lobby in which this table is to live.
     * @param tconfig the table configuration for this table.
     * @param config the configuration of the game being matchmade by this table.
     */
    @ActionScript(omit=true)
    public void init (int lobbyOid, TableConfig tconfig, GameConfig config)
    {
        // assign a unique table id
        tableId = ++_tableIdCounter;

        // keep track of our lobby oid
        this.lobbyOid = lobbyOid;

        // keep a casted reference around
        this.tconfig = tconfig;
        this.config = config;

        // make room for the maximum number of players
        if (config.getMatchType() != GameConfig.PARTY) {
            occupants = new Name[tconfig.desiredPlayerCount];
            bodyOids = new int[occupants.length];

            // fill in information on the AIs
            int acount = (config.ais == null) ? 0 : config.ais.length;
            for (int ii = 0; ii < acount; ii++) {
                // TODO: handle this naming business better
                occupants[ii] = new Name("AI " + (ii+1));
            }

        } else {
            occupants = new Name[0];
            bodyOids = new int[0];
        }
    }

    /**
     * Returns true if there is no one sitting at this table.
     */
    @ActionScript(omit=true)
    public boolean isEmpty ()
    {
        for (int i = 0; i < bodyOids.length; i++) {
            if (bodyOids[i] != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Count the number of players currently occupying this table.
     */
    public int getOccupiedCount ()
    {
        int count = 0;
        if (occupants != null) {
            for (int ii = 0; ii < occupants.length; ii++) {
                if (occupants[ii] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Once a table is ready to play (see {@link #mayBeStarted} and {@link #shouldBeStarted}), the
     * players array can be fetched using this method. It will return an array containing the
     * usernames of all of the players in the game, sized properly and with each player in the
     * appropriate position.
     */
    public Name[] getPlayers ()
    {
        // seated party games need a spot for every seat
        if (GameConfig.SEATED_CONTINUOUS == config.getMatchType()) {
            return new Name[tconfig.desiredPlayerCount];
        }

        // FFA party games have 0-length players array, and non-party games will have the players
        // who are ready-to-go for the game start.
        Name[] players = new Name[getOccupiedCount()];
        if (occupants != null) {
            for (int ii = 0, dex = 0; ii < occupants.length; ii++) {
                if (occupants[ii] != null) {
                    players[dex++] = occupants[ii];
                }
            }
        }

        return players;
    }

    /**
     * For a team game, get the team member indices of the compressed players array returned by
     * getPlayers().
     */
    public int[][] getTeamMemberIndices ()
    {
        int[][] teams = tconfig.teamMemberIndices;
        if (teams == null) {
            return null;
        }

        // compress the team indexes down
        ArrayIntSet set = new ArrayIntSet();
        int[][] newTeams = new int[teams.length][];
        Name[] players = getPlayers();
        for (int ii=0; ii < teams.length; ii++) {
            set.clear();
            for (int jj=0; jj < teams[ii].length; jj++) {
                Name occ = occupants[teams[ii][jj]];
                if (occ != null) {
                    set.add(ListUtil.indexOf(players, occ));
                }
            }
            newTeams[ii] = set.toIntArray();
        }

        return newTeams;
    }

    /**
     * Requests to seat the specified user at the specified position in this table.
     *
     * @param position the position in which to seat the user.
     * @param occupant the occupant to set.
     *
     * @return null if the user was successfully seated, a string error code explaining the failure
     * if the user was not able to be seated at that position.
     */
    @ActionScript(omit=true)
    public String setOccupant (int position, BodyObject occupant)
    {
        // make sure the requested position is a valid one
        if (position >= tconfig.desiredPlayerCount || position < 0) {
            return INVALID_TABLE_POSITION;
        }

        // make sure the requested position is not already occupied
        if (occupants[position] != null) {
            return TABLE_POSITION_OCCUPIED;
        }

        // otherwise all is well, stick 'em in
        setOccupantPos(position, occupant);
        return null;
    }

    /**
     * This method is used for party games, it does no bounds checking or verification of the
     * player's ability to join, if you are unsure you should call 'setOccupant'.
     */
    @ActionScript(omit=true)
    public void setOccupantPos (int position, BodyObject occupant)
    {
        occupants[position] = occupant.getVisibleName();
        bodyOids[position] = occupant.getOid();
    }

    /**
     * Requests that the specified user be removed from their seat at this table.
     *
     * @return true if the user was seated at the table and has now been removed, false if the user
     * was never seated at the table in the first place.
     */
    @ActionScript(omit=true)
    public boolean clearOccupant (Name username)
    {
        if (occupants != null) {
            for (int i = 0; i < occupants.length; i++) {
                if (username.equals(occupants[i])) {
                    clearOccupantPos(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Requests that the user identified by the specified body object id be removed from their seat
     * at this table.
     *
     * @return true if the user was seated at the table and has now been removed, false if the user
     * was never seated at the table in the first place.
     */
    @ActionScript(omit=true)
    public boolean clearOccupantByOid (int bodyOid)
    {
        if (bodyOids != null) {
            for (int i = 0; i < bodyOids.length; i++) {
                if (bodyOid == bodyOids[i]) {
                    clearOccupantPos(i);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called to clear an occupant at the specified position.  Only call this method if you know
     * what you're doing.
     */
    @ActionScript(omit=true)
    public void clearOccupantPos (int position)
    {
        occupants[position] = null;
        bodyOids[position] = 0;
    }

    /**
     * Returns true if this table has a sufficient number of occupants that the game can be
     * started.
     */
    public boolean mayBeStarted ()
    {
        switch (config.getMatchType()) {
        case GameConfig.SEATED_CONTINUOUS:
        case GameConfig.PARTY:
            return true;
        }

        if (tconfig.teamMemberIndices == null) {
            // for a normal game, just check to see if we're past the minimum
            return tconfig.minimumPlayerCount <= getOccupiedCount();

        } else {
            // for a team game, make sure each team has the minimum players
            int[][] teams = tconfig.teamMemberIndices;
            for (int ii=0; ii < teams.length; ii++) {
                int teamCount = 0;
                for (int jj=0; jj < teams[ii].length; jj++) {
                    if (occupants[teams[ii][jj]] != null) {
                        teamCount++;
                    }
                }
                if (teamCount < tconfig.minimumPlayerCount) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * Returns true if sufficient seats are occupied that the game should be automatically started.
     */
    public boolean shouldBeStarted ()
    {
        switch (config.getMatchType()) {
        case GameConfig.SEATED_CONTINUOUS:
        case GameConfig.PARTY:
            return true;

        default:
            return (tconfig.desiredPlayerCount <= getOccupiedCount());
        }
    }

    /**
     * Returns true if this table is in play, false if it is still being matchmade.
     */
    public boolean inPlay ()
    {
        return gameOid != -1;
    }

    // documentation inherited
    public Comparable getKey ()
    {
        return tableId;
    }

    // documentation inherited
    public boolean equals (Object other)
    {
        return (other instanceof Table) && (tableId == ((Table) other).tableId);
    }

    // documentation inherited
    public int hashCode ()
    {
        return tableId;
    }

    /**
     * Generates a string representation of this table instance.
     */
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append(StringUtil.shortClassName(this));
        buf.append(" [");
        toString(buf);
        buf.append("]");
        return buf.toString();
    }

    /**
     * Helper method for toString, ripe for overrideability.
     */
    @ActionScript(name="toStringBuilder")
    protected void toString (StringBuilder buf)
    {
        buf.append("tableId=").append(tableId);
        buf.append(", lobbyOid=").append(lobbyOid);
        buf.append(", gameOid=").append(gameOid);
        buf.append(", occupants=").append(StringUtil.toString(occupants));
        buf.append(", config=").append(config);
    }

    /** A counter for assigning table ids. */
    protected static int _tableIdCounter = 0;
}
