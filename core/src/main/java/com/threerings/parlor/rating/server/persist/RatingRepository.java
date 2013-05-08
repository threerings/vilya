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

package com.threerings.parlor.rating.server.persist;

import java.sql.Timestamp;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;

import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.Query;
import com.samskivert.depot.clause.OrderBy;
import com.samskivert.depot.expression.SQLExpression;

import com.threerings.parlor.rating.util.Percentiler;

/**
 * Handles the persistent storage of per-user per-game ratings.
 */
@Singleton
public class RatingRepository extends DepotRepository
{
    /**
     * Initialize the {@link RatingRepository}.
     */
    @Inject public RatingRepository (PersistenceContext ctx)
    {
        super(ctx);
    }

    /**
     * Loads the rating for the given player for the given game and returns it as a
     * {@link RatingRecord} object, or null if the player has no previous rating for the game.
     */
    public RatingRecord getRating (int gameId, int playerId)
    {
        return load(RatingRecord.getKey(gameId, playerId));
    }

    /**
     * Fetch the ratings registered for any of the given players for the given game and return
     * them as a list of {@link RatingRecord} objects. The size of this list is no less than zero
     * and no greater than the number of given players.
     */
    public List<RatingRecord> getRatings (int gameId, Integer... players)
    {
        if (players.length == 0) {
            return Collections.emptyList();
        }
        return from(RatingRecord.class).where(
            RatingRecord.GAME_ID.eq(gameId), RatingRecord.PLAYER_ID.in(players)).select();
    }

    /**
     * Fetch and return all the registered {@link RatingRecord} rows for the given player. Ratings
     * will be returned in order of most recently to least recently updated.
     *
     * @param since ratings last updated more than this number of milliseconds in the past will be
     * ommitted from the result list. Supplying -1 will return all ratings regardless of age.
     * @param count the maximum number of ratings to return or -1 for all ratings.
     */
    public List<RatingRecord> getRatings (int playerId, long since, int count)
    {
        Query<RatingRecord> query = from(RatingRecord.class);
        if (since > 0L) {
            Timestamp when = new Timestamp(System.currentTimeMillis() - since);
            query = query.where(RatingRecord.PLAYER_ID.eq(playerId),
                                RatingRecord.LAST_UPDATED.greaterThan(when));
        } else {
            query = query.where(RatingRecord.PLAYER_ID, playerId);
        }
        if (count > 0) {
            query = query.limit(count);
        }
        return query.descending(RatingRecord.LAST_UPDATED).select();
    }

    /**
     * Returns the top-ratings for the specified game. Players with equal rating will be sorted
     * most recently played first.
     *
     * @param since an absolute number of milliseconds (ie. 10*24*60*60*1000L). Players that have
     * not updated their rating within this many milliseconds in the past will be omitted from the
     * results. Supply zero to omit this filter.
     * @param playerIds an optional list of player ids to which to limit the top-rankings search.
     */
    public List<RatingRecord> getTopRatings (
        int gameId, int limit, long since, Set<Integer> playerIds)
    {
        List<SQLExpression<?>> where = Lists.newArrayList();
        where.add(RatingRecord.GAME_ID.eq(gameId));
        if (since > 0L) {
            where.add(RatingRecord.LAST_UPDATED.greaterThan(
                          new Timestamp(System.currentTimeMillis() - since)));
        }
        if (playerIds != null) {
            where.add(RatingRecord.PLAYER_ID.in(playerIds));
        }

        OrderBy ob = new OrderBy(
            new SQLExpression<?>[] { RatingRecord.RATING, RatingRecord.LAST_UPDATED },
            new OrderBy.Order[] { OrderBy.Order.DESC, OrderBy.Order.DESC });
        return from(RatingRecord.class).where(where).limit(limit).orderBy(ob).select();
    }

    /**
     * Set the rating and experience for a given player and game. This method will either update
     * or create a row.
     */
    public void setRating (int gameId, int playerId, int rating, int experience)
    {
        store(new RatingRecord(gameId, playerId, rating, experience));
    }

    /**
     * Deletes the specified rating record.
     */
    public void deleteRating (int gameId, int playerId)
    {
        delete(RatingRecord.getKey(gameId, playerId));
    }

    /**
     * Loads the percentile distribution associated with the specified game and mode. null will
     * never be returned, rather a blank percentiler will be created and returned.
     */
    public Percentiler loadPercentile (int gameId, int gameMode)
    {
        PercentileRecord record = load(PercentileRecord.getKey(gameId, gameMode));
        return (record == null) ? new Percentiler() : new Percentiler(record.data);
    }

    /**
     * Loads all the percentile distributions associated with the specified game. The percentilers
     * will be mapped by their game mode. The map may be zero size if the game has recorded no
     * distributions.
     */
    public Map<Integer, Percentiler> loadPercentiles (int gameId)
    {
        Map<Integer, Percentiler> tilers = Maps.newHashMap();
        for (PercentileRecord record : from(PercentileRecord.class).
                 where(PercentileRecord.GAME_ID, gameId).select()) {
            tilers.put(record.gameMode, new Percentiler(record.data));
        }
        return tilers;
    }

    /**
     * Writes the supplied percentiler's data out to the database.
     */
    public void updatePercentile (int gameId, int gameMode, Percentiler tiler)
    {
        PercentileRecord record = new PercentileRecord();
        record.gameId = gameId;
        record.gameMode = gameMode;
        record.data = tiler.toBytes();
        store(record);
    }

    /**
     * Deletes the percentile record for the specified game and game mode.
     */
    public void deletePercentile (int gameId, int gameMode)
    {
        delete(PercentileRecord.getKey(gameId, gameMode));
    }

    /**
     * Deletes all rating and percentile records for the specified game.
     */
    public void purgeGame (int gameId)
    {
        from(RatingRecord.class).where(RatingRecord.GAME_ID, gameId).delete(null);
        from(PercentileRecord.class).where(PercentileRecord.GAME_ID, gameId).delete(null);
    }

    /**
     * Deletes all rating records for the specified players.
     */
    public void purgePlayers (Collection<Integer> playerIds)
    {
        from(RatingRecord.class).where(RatingRecord.PLAYER_ID.in(playerIds)).delete(null);
    }

    /**
     * Load the most recently entered rating for each of a collection of players. The search may
     * be limited to only negative or positive id's since applications may use the sign to indicate
     * game mode.
     * @param gameIdSign if non-zero, limits the search to game ids of matching sign
     */
    public Collection<RatingRecord> getMostRecentRatings (
        Collection<Integer> playerIds, int gameIdSign)
    {
        return getMostRecentRatings(playerIds, null, gameIdSign);
    }

    /**
     * Load the most recently entered rating for each of a collection of players. The search may
     * be limited to a specific collection of games.
     * @param gameIds if not null, limits the search to the specific game ids
     * @param gameIdSign if non-zero, limits the search to game ids of matching sign
     */
    public Collection<RatingRecord> getMostRecentRatings (
        Collection<Integer> playerIds, Collection<Integer> gameIds, int gameIdSign)
    {
        // TODO: Implement "distinct" in depot. Here's the query I'd like to do:
        //
        //     select distinct on ("playerId") * from "RatingRecord" where "playerId" in (...)
        //     and "gameId" < 0 order by "playerId" desc, "lastUpdated" desc;
        //
        // (The ordering by playerId seems to only be required to satisfy the distinct request).
        // Without distinct, I must load all ratings and throw out all but the first. They're not
        // that big, but still.

        List<SQLExpression<?>> conditions = Lists.newArrayList();
        conditions.add(RatingRecord.PLAYER_ID.in(playerIds));
        if (gameIds != null) {
            conditions.add(RatingRecord.GAME_ID.in(gameIds));
        }
        if (gameIdSign != 0) {
            conditions.add(gameIdSign < 0 ?
                RatingRecord.GAME_ID.lessThan(0) : RatingRecord.GAME_ID.greaterThan(0));
        }
        IntMap<RatingRecord> ratings = IntMaps.newHashIntMap();
        for (RatingRecord record : from(RatingRecord.class).where(conditions).
                 descending(RatingRecord.LAST_UPDATED).select()) {
            if (ratings.containsKey(record.playerId)) {
                continue;
            }
            ratings.put(record.playerId, record);
        }
        return ratings.values();
    }

    @Override
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(RatingRecord.class);
        classes.add(PercentileRecord.class);
    }
}
