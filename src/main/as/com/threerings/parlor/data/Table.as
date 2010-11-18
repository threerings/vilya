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

package com.threerings.parlor.data {

import com.threerings.util.ArrayUtil;
import com.threerings.util.ClassUtil;
import com.threerings.util.Hashable;
import com.threerings.util.Joiner;
import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DSet_Entry;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.game.data.GameConfig;

/**
 * This class represents a table that is being used to matchmake a game by the Parlor services.
 */
public class Table
    implements DSet_Entry, Hashable
{
    /** Used to request any position at a table. */
    public static const ANY_POSITION :int = -1;

    /** The unique identifier for this table. */
    public var tableId :int;

    /** The object id of the lobby object with which this table is associated. */
    public var lobbyOid :int;

    /** The oid of the game that was created from this table or -1 if the table is still in
     * matchmaking mode. */
    public var gameOid :int = -1;

    /** An array of the usernames of the players of this table (some slots may not be filled), or
     * null if a party game. */
    public var players :TypedArray;

    /** An array of the usernames of the non-player occupants of this game. For FFA party games
     * this is all of a room's occupants and they are in fact players. */
    public var watchers :TypedArray;

    /** The body oids of the players of this table, or null if a party game.  (This is not
     * propagated to remote instances.) */
    public var bodyOids :TypedArray;

    /** The game config for the game that is being matchmade. */
    public var config :GameConfig;

    /** The table configuration object. */
    public var tconfig :TableConfig;

    /** Suitable for unserialization. */
    public function Table ()
    {
    }

    /**
     * Once a table is ready to play (see {@link #mayBeStarted} and {@link #shouldBeStarted}), the
     * players array can be fetched using this method. It will return an array containing the
     * usernames of all of the players in the game, sized properly and with each player in the
     * appropriate position.
     */
    public function getPlayers () :Array
    {
        var parray :Array = new Array();
        for (var ii :int = 0; ii < players.length; ii++) {
            if (players[ii] != null) {
                parray.push(players[ii]);
            }
        }
        return parray;
    }

    /**
     * Count the number of players currently occupying this table.
     */
    public function getOccupiedCount () :int
    {
        var count :int = 0;
        if (players != null) {
            for (var ii :int = 0; ii < players.length; ii++) {
                if (players[ii] != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * For a team game, get the team member indices of the compressed players array returned by
     * getPlayers().
     */
    public function getTeamMemberIndices () :TypedArray /* of Array of int */
    {
        var teams :Array = tconfig.teamMemberIndices;
        if (teams == null) {
            return null;
        }

        // compress the team indexes down
        var newTeams :TypedArray = new TypedArray("[[I");
        var players :Array = getPlayers();
        for (var ii :int = 0; ii < teams.length; ii++) {
            var subTeams :Array = (teams[ii] as Array);
            var newSubTeams :TypedArray = TypedArray.create(int);
            for (var jj :int = 0; jj < subTeams.length; jj++) {
                var occ :Name = (players[(subTeams[jj] as int)] as Name);
                if (occ != null) {
                    newSubTeams.push(ArrayUtil.indexOf(players, occ));
                }
            }
            newSubTeams.sort(null, Array.NUMERIC);
            newTeams[ii] = newSubTeams;
        }

        return newTeams;
    }

    /**
     * Returns true if this table has a sufficient number of players that the game can be
     * started.
     */
    public function mayBeStarted () :Boolean
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
            var teams :Array = tconfig.teamMemberIndices;
            for (var ii :int = 0; ii < teams.length; ii++) {
                var teamCount :int = 0;
                var members :Array = (teams[ii] as Array);
                for (var jj :int = 0; jj < members.length; jj++) {
                    if (players[members[jj]] != null) {
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
    public function shouldBeStarted () :Boolean
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
    public function inPlay () :Boolean
    {
        return gameOid != -1;
    }

    // from Hashable
    public function hashCode () :int
    {
        return tableId;
    }

    // from Hashable
    public function equals (other :Object) :Boolean
    {
        return (other is Table) && (tableId == (other as Table).tableId);
    }

    /**
     * Generates a string representation of this table instance.
     */
    public function toString () :String
    {
        var j :Joiner = Joiner.createFor(this);
        toStringJoiner(j);
        return j.toString();
    }

    // documentation inherited
    public function getKey () :Object
    {
        return tableId;
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        tableId = ins.readInt();
        lobbyOid = ins.readInt();
        gameOid = ins.readInt();
        players = TypedArray(ins.readObject());
        watchers = TypedArray(ins.readObject());
        config = GameConfig(ins.readObject());
        tconfig = TableConfig(ins.readObject());
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(tableId);
        out.writeInt(lobbyOid);
        out.writeInt(gameOid);
        out.writeObject(players);
        out.writeObject(watchers);
        out.writeObject(config);
        out.writeObject(tconfig);
    }

    /**
     * Helper method for toString, ripe for overrideability.
     */
    protected function toStringJoiner (j :Joiner) :void
    {
        j.add("tableId", tableId, "lobbyOid", lobbyOid, "gameOid", gameOid,
            "players", players, "watchers", watchers, "config", config);
    }

    /** A counter for assigning table ids. */
    protected static var _tableIdCounter :int = 0;
}
}
