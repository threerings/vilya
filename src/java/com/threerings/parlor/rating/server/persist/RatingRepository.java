//
// $Id: PuzzleManagerDelegate.java 209 2007-02-24 00:37:33Z mdb $
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

package com.threerings.parlor.rating.server.persist;

import java.util.List;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.depot.DepotRepository;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.operator.Logic.*;
import com.samskivert.jdbc.depot.operator.Conditionals.*;

import com.threerings.util.Name;

/**
 * Handles the persistent storage of per-user per-game ratings.
 */
public abstract class RatingRepository extends DepotRepository
{
    /**
     * Users are indexed by integer. It is the responsibility of the subclasser to implement
     * an injective mapping of {@link Name} instance to integer here.
     */
    public abstract int mapNameToId (Name player);

    /**
     * Loads the rating for the given player for the given game and returns it as a
     * {@link RatingRecord} object, or null if the player has no previous rating for the game.
     */
    public RatingRecord getRating (int gameId, Name player)
        throws PersistenceException
    {
        return load(
            RatingRecord.class,
            RatingRecord.GAME_ID, gameId,
            RatingRecord.PLAYER_ID, mapNameToId(player));
    }

    /**
     * Fetch the ratings registered for any of the given players for the given game and return
     * them as a list of {@link RatingRecord} objects. The size of this list is no less than zero
     * and no greater than the number of given players.
     */
    public List<RatingRecord> getRatings (int gameId, Name... players)
        throws PersistenceException
    {
        Comparable[] idArr = new Comparable[players.length];
        for (int ii = 0; ii < idArr.length; ii ++) {
            idArr[ii] = mapNameToId(players[ii]);
        }
        return findAll(
            RatingRecord.class,
            new Where(new And(
                new Equals(RatingRecord.GAME_ID, gameId),
                new In(RatingRecord.PLAYER_ID, idArr)))); 
    }

    /**
     * Fetch and return all the registered {@link RatingRecord} rows for the given player. 
     */
    public List<RatingRecord> getRatings (Name player)
        throws PersistenceException
    {
        return findAll(RatingRecord.class, new Where(RatingRecord.PLAYER_ID, mapNameToId(player)));
    }

    /**
     * Set the rating and experience for a given player and game. This method will either update
     * or create a row.
     */
    public void setRating (int gameId, Name player, int rating, int experience)
        throws PersistenceException
    {
        store(new RatingRecord(gameId, mapNameToId(player), rating, experience));
    }

    /**
     * Initialize the {@link RatingRepository}.
     */
    protected RatingRepository(ConnectionProvider conprov)
    {
        super(conprov);
    }
}
