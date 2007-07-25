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

package com.threerings.parlor.rating.server.persist;

import java.util.Collections;
import java.util.List;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.depot.DepotRepository;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.operator.Conditionals.*;
import com.samskivert.jdbc.depot.operator.Logic.And;

/**
 * Handles the persistent storage of per-user per-game ratings.
 */
public class RatingRepository extends DepotRepository
{
    /**
     * Initialize the {@link RatingRepository}.
     */
    public RatingRepository(ConnectionProvider conprov)
    {
        super(conprov);
    }

    /**
     * Loads the rating for the given player for the given game and returns it as a
     * {@link RatingRecord} object, or null if the player has no previous rating for the game.
     */
    public RatingRecord getRating (int gameId, int playerId)
        throws PersistenceException
    {
        return load(RatingRecord.class,
                    RatingRecord.GAME_ID, gameId,
                    RatingRecord.PLAYER_ID, playerId);
    }

    /**
     * Fetch the ratings registered for any of the given players for the given game and return
     * them as a list of {@link RatingRecord} objects. The size of this list is no less than zero
     * and no greater than the number of given players.
     */
    public List<RatingRecord> getRatings (int gameId, Integer... players)
        throws PersistenceException
    {
        if (players.length == 0) {
            return Collections.emptyList();
        }
        return findAll(RatingRecord.class,
                       new Where(new And(new Equals(RatingRecord.GAME_ID_C, gameId),
                                         new In(RatingRecord.PLAYER_ID_C, players))));
    }

    /**
     * Fetch and return all the registered {@link RatingRecord} rows for the given player. 
     */
    public List<RatingRecord> getRatings (int playerId)
        throws PersistenceException
    {
        return findAll(RatingRecord.class, new Where(RatingRecord.PLAYER_ID_C, playerId));
    }

    /**
     * Set the rating and experience for a given player and game. This method will either update
     * or create a row.
     */
    public void setRating (int gameId, int playerId, int rating, int experience)
        throws PersistenceException
    {
        store(new RatingRecord(gameId, playerId, rating, experience));
    }
}
