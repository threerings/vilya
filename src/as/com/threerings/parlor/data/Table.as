//
// $Id: Table.java 4191 2006-06-13 22:42:20Z ray $
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

package com.threerings.parlor.data {

import com.threerings.util.ArrayUtil;
import com.threerings.util.ClassUtil;
import com.threerings.util.Hashable;
import com.threerings.util.Name;
import com.threerings.util.StringBuilder;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DSet_Entry;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.PartyGameCodes;
import com.threerings.parlor.game.data.PartyGameConfig;

/**
 * This class represents a table that is being used to matchmake a game by
 * the Parlor services.
 */
public class Table
    implements DSet_Entry, Hashable
{
    /** The unique identifier for this table. */
    public var tableId :int;

    /** The object id of the lobby object with which this table is
     * associated. */
    public var lobbyOid :int;

    /** The oid of the game that was created from this table or -1 if the
     * table is still in matchmaking mode. */
    public var gameOid :int = -1;

    /** An array of the usernames of the occupants of this table (some
     * slots may not be filled), or null if a party game. */
    public var occupants :TypedArray;

    /** The body oids of the occupants of this table, or null if a party game.
     * (This is not propagated to remote instances.) */
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
     * Count the number of players currently occupying this table.
     */
    public function getOccupiedCount () :int
    {
        var count :int = 0;
        for (var ii :int = 0; ii < occupants.length; ii++) {
            if (occupants[ii] != null) {
                count++;
            }
        }
        return count;
    }

    /**
     * Once a table is ready to play (see {@link #mayBeStarted} and {@link
     * #shouldBeStarted}), the players array can be fetched using this
     * method. It will return an array containing the usernames of all of
     * the players in the game, sized properly and with each player in the
     * appropriate position.
     */
    public function getPlayers () :Array
    {
        if (isPartyGame()) {
            return occupants;
        }

        // create and populate the players array
        var players :Array = new Array();
        for (var ii :int = 0; ii < occupants.length; ii++) {
            if (occupants[ii] != null) {
                players.push(occupants[ii]);
            }
        }

        return players;
    }

    /**
     * For a team game, get the team member indices of the compressed
     * players array returned by getPlayers().
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
                var occ :Name = (occupants[(subTeams[jj] as int)] as Name);
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
     * Return true if the game is a party game.
     */
    public function isPartyGame () :Boolean
    {
        return (PartyGameCodes.NOT_PARTY_GAME != getPartyGameType());
    }

    /**
     * Get the type of party game being played at this table, or
     * PartyGameCodes.NOT_PARTY_GAME.
     */
    public function getPartyGameType () :int
    {
        if (config is PartyGameConfig) {
            return (config as PartyGameConfig).getPartyGameType();

        } else {
            return PartyGameCodes.NOT_PARTY_GAME;
        }
    }

//    /**
//     * Requests to seat the specified user at the specified position in
//     * this table.
//     *
//     * @param position the position in which to seat the user.
//     * @param occupant the occupant to set.
//     *
//     * @return null if the user was successfully seated, a string error
//     * code explaining the failure if the user was not able to be seated
//     * at that position.
//     */
//    public function setOccupant (position :int, occupant :BodyObject) :String
//    {
//        // make sure the requested position is a valid one
//        if (position >= tconfig.desiredPlayerCount || position < 0) {
//            return ParlorCodes.INVALID_TABLE_POSITION;
//        }
//
//        // make sure the requested position is not already occupied
//        if (occupants[position] != null) {
//            return ParlorCodes.TABLE_POSITION_OCCUPIED;
//        }
//
//        // otherwise all is well, stick 'em in
//        setOccupantPos(position, occupant);
//        return null;
//    }
//
//    /**
//     * This method is used for party games, it does no bounds checking
//     * or verification of the player's ability to join, if you are unsure
//     * you should call 'setOccupant'.
//     */
//    public function setOccupantPos (position :int, occupant :BodyObject) :void
//    {
//        occupants[position] = occupant.getVisibleName();
//        bodyOids[position] = occupant.getOid();
//    }
//
//    /**
//     * Requests that the specified user be removed from their seat at this
//     * table.
//     *
//     * @return true if the user was seated at the table and has now been
//     * removed, false if the user was never seated at the table in the
//     * first place.
//     */
//    public function clearOccupant (username :Name) :Boolean
//    {
//        var dex :int = ArrayUtil.indexOf(occupants, username);
//        if (dex != -1) {
//            clearOccupantPos(dex);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Requests that the user identified by the specified body object id
//     * be removed from their seat at this table.
//     *
//     * @return true if the user was seated at the table and has now been
//     * removed, false if the user was never seated at the table in the
//     * first place.
//     */
//    public function clearOccupantByOid (bodyOid :int) :Boolean
//    {
//        var dex :int = ArrayUtil.indexOf(bodyOids, bodyOid);
//        if (dex != -1) {
//            clearOccupantPos(dex);
//            return true;
//        }
//        return false;
//    }
//
//    /**
//     * Called to clear an occupant at the specified position.
//     * Only call this method if you know what you're doing.
//     */
//    public function clearOccupantPos (position :int) :void
//    {
//        occupants[position] = null;
//        bodyOids[position] = 0;
//    }

    /**
     * Returns true if this table has a sufficient number of occupants
     * that the game can be started.
     */
    public function mayBeStarted () :Boolean
    {
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
                    if (occupants[members[jj]] != null) {
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
     * Returns true if sufficient seats are occupied that the game should
     * be automatically started.
     */
    public function shouldBeStarted () :Boolean
    {
        return tconfig.desiredPlayerCount <= getOccupiedCount();
    }

    /**
     * Returns true if this table is in play, false if it is still being
     * matchmade.
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
        return (other is Table) &&
            (tableId == (other as Table).tableId);
    }

    /**
     * Generates a string representation of this table instance.
     */
    public function toString () :String
    {
        var buf :StringBuilder = new StringBuilder();
        buf.append(ClassUtil.shortClassName(this));
        buf.append(" [");
        toStringBuilder(buf);
        buf.append("]");
        return buf.toString();
    }

    // documentation inherited
    public function getKey () :Object
    {
        return tableId;
    }

    /**
     * Returns true if there is no one sitting at this table.
     */
    public function isEmpty () :Boolean
    {
        for (var ii :int = 0; ii < bodyOids.length; ii++) {
            if ((bodyOids[ii] as int) !== 0) {
                return false;
            }
        }
        return true;
    }

    // from Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        tableId = ins.readInt();
        lobbyOid = ins.readInt();
        gameOid = ins.readInt();
        occupants = (ins.readObject() as TypedArray);
        config = (ins.readObject() as GameConfig);
        tconfig = (ins.readObject() as TableConfig);
    }

    // from Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        throw new Error();
//        out.writeInt(tableId);
//        out.writeInt(lobbyOid);
//        out.writeInt(gameOid);
//        out.writeObject(occupants);
//        out.writeObject(config);
//        out.writeObject(tconfig);
    }

    /**
     * Helper method for toString, ripe for overrideability.
     */
    protected function toStringBuilder (buf :StringBuilder) :void
    {
        buf.append("tableId=").append(tableId);
        buf.append(", lobbyOid=").append(lobbyOid);
        buf.append(", gameOid=").append(gameOid);
        buf.append(", occupants=").append(occupants.join());
        buf.append(", config=").append(config);
    }

    /** A counter for assigning table ids. */
    protected static var _tableIdCounter :int = 0;
}
}
