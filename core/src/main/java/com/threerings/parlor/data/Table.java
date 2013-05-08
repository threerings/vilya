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

package com.threerings.parlor.data;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ListUtil;
import com.samskivert.util.StringUtil;

import com.threerings.util.ActionScript;
import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;

/**
 * This class represents a table that is being used to matchmake a game by the Parlor services.
 */
public class Table
    implements DSet.Entry, ParlorCodes
{
    /** Used to request any position at a table. */
    public static final int ANY_POSITION = -1;

    /** The unique identifier for this table. */
    public int tableId;

    /** The object id of the lobby object with which this table is associated. */
    public int lobbyOid;

    /** The oid of the game that was created from this table or -1 if the table is still in
     * matchmaking mode. */
    public int gameOid = -1;

    /** An array of the usernames of the players of this table (some slots may not be filled), or
     * null if a party game. */
    public Name[] players;

    /** An array of the usernames of the non-player occupants of this game. For FFA party games
     * this is all of a room's occupants and they are in fact players. */
    public Name[] watchers = createPlayerNamesArray(0);

    /** The body oids of the players of this table, or null if a party game.  (This is not
     * propagated to remote instances.) */
    public transient int[] bodyOids;

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
            players = createPlayerNamesArray(tconfig.desiredPlayerCount);
            bodyOids = new int[players.length];

            // fill in information on the AIs
            int acount = (config.ais == null) ? 0 : config.ais.length;
            for (int ii = 0; ii < acount; ii++) {
                // TODO: handle this naming business better
                players[ii] = new Name("AI " + (ii+1));
            }

        } else {
            players = createPlayerNamesArray(0);
            bodyOids = new int[0];
        }
    }

    /**
     * Returns true if there is no one sitting at this table.
     */
    @ActionScript(omit=true)
    public boolean isEmpty ()
    {
        for (int bodyOid : bodyOids) {
            if (bodyOid != 0) {
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
        if (players != null) {
            for (Name player : players) {
                if (player != null) {
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
            return createPlayerNamesArray(tconfig.desiredPlayerCount);
        }

        // FFA party games have 0-length players array, and non-party games will have the players
        // who are ready-to-go for the game start.
        Name[] plist = createPlayerNamesArray(getOccupiedCount());
        if (players != null) {
            for (int ii = 0, dex = 0; ii < players.length; ii++) {
                if (players[ii] != null) {
                    plist[dex++] = players[ii];
                }
            }
        }

        return plist;
    }

    /**
     * For a team game, get the team member indices of the compressed players array returned by
     * {@link #getPlayers}.
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
        Name[] plist = getPlayers();
        for (int ii=0; ii < teams.length; ii++) {
            set.clear();
            for (int jj=0; jj < teams[ii].length; jj++) {
                Name occ = players[teams[ii][jj]];
                if (occ != null) {
                    set.add(ListUtil.indexOf(plist, occ));
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
     * @param player the player to set.
     *
     * @return null if the user was successfully seated, a string error code explaining the failure
     * if the user was not able to be seated at that position.
     */
    @ActionScript(omit=true)
    public String setPlayer (int position, BodyObject player)
    {
        // check whether this player has been banned
        if (_bannedUsers != null && _bannedUsers.contains(player.getVisibleName())) {
            return BANNED_FROM_TABLE;
        }

        // if they just want any position, see if we have one available
        if (position == ANY_POSITION) {
            for (int ii = 0; ii < tconfig.desiredPlayerCount; ii++) {
                if (players[ii] == null) {
                    setPlayerPos(ii, player);
                    return null;
                }
            }
            return TABLE_POSITION_OCCUPIED;
        }

        // make sure the requested position is a valid one
        if (position >= tconfig.desiredPlayerCount || position < 0) {
            return INVALID_TABLE_POSITION;
        }

        // make sure the requested position is not already occupied
        if (players[position] != null) {
            return TABLE_POSITION_OCCUPIED;
        }

        // otherwise all is well, stick 'em in
        setPlayerPos(position, player);
        return null;
    }

    /**
     * This method is used for party games, it does no bounds checking or verification of the
     * player's ability to join, if you are unsure you should call 'setPlayer'.
     */
    @ActionScript(omit=true)
    public void setPlayerPos (int position, BodyObject player)
    {
        players[position] = player.getVisibleName();
        bodyOids[position] = player.getOid();
    }

    /**
     * Indicate to this table that a user was booted and should
     * be prevented from rejoining.
     */
    public void addBannedUser (Name player)
    {
        if (_bannedUsers == null) {
            _bannedUsers = Sets.newHashSet();
        }

        _bannedUsers.add(player);
    }

    /**
     * Requests that the specified user be removed from their seat at this table.
     *
     * @return true if the user was seated at the table and has now been removed, false if the user
     * was never seated at the table in the first place.
     */
    @ActionScript(omit=true)
    public boolean clearPlayer (Name username)
    {
        if (players != null) {
            for (int ii = 0; ii < players.length; ii++) {
                if (username.equals(players[ii])) {
                    clearPlayerPos(ii);
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
    public boolean clearPlayerByOid (int bodyOid)
    {
        if (bodyOids != null) {
            for (int ii = 0; ii < bodyOids.length; ii++) {
                if (bodyOid == bodyOids[ii]) {
                    clearPlayerPos(ii);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Called to clear a player at the specified position.  Only call this method if you know what
     * you're doing.
     */
    @ActionScript(omit=true)
    public void clearPlayerPos (int position)
    {
        players[position] = null;
        bodyOids[position] = 0;
    }

    /**
     * Returns true if this table contains the specified player.
     */
    @ActionScript(omit=true)
    public boolean containsPlayer (Name player)
    {
        return (players != null && ListUtil.indexOf(players, player) != -1);
    }

    /**
     * Called by the table manager when the game object's players have changed. Regenerates the
     * {@link #watchers} array.
     */
    @ActionScript(omit=true)
    public void updateOccupants (GameObject gameobj)
    {
        List<Name> wlist = Lists.newArrayList();
        for (OccupantInfo info : gameobj.occupantInfo) {
            if (containsPlayer(info.username)) { // skip players
                continue;
            }
            wlist.add(info.username);
        }
        watchers = wlist.toArray(createPlayerNamesArray(wlist.size()));
    }

    /**
     * Returns true if this table has a sufficient number of players that the game can be
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
            for (int[] team : teams) {
                int teamCount = 0;
                for (int element : team) {
                    if (players[element] != null) {
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
    public Comparable<?> getKey ()
    {
        return tableId;
    }

    @Override
    public boolean equals (Object other)
    {
        return (other instanceof Table) && (tableId == ((Table) other).tableId);
    }

    @Override
    public int hashCode ()
    {
        return tableId;
    }

    @Override
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
        buf.append(", players=").append(StringUtil.toString(players));
        buf.append(", config=").append(config);
    }

    /**
     * Creates a names array of the appropriate type.
     */
    protected Name[] createPlayerNamesArray (int length)
    {
        return new Name[length];
    }

    /** A counter for assigning table ids. */
    protected static int _tableIdCounter = 0;

    /** On the server, the usernames that have been banned from this table. */
    protected transient HashSet<Name> _bannedUsers;
}
