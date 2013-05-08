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

package com.threerings.parlor.rating.server;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.Invoker;
import com.samskivert.jdbc.RepositoryUnit;

import com.threerings.util.Name;
import com.threerings.util.TimeUtil;

import com.threerings.presents.annotation.MainInvoker;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.game.server.GameManagerDelegate;
import com.threerings.parlor.rating.data.RatingCodes;
import com.threerings.parlor.rating.server.persist.RatingRecord;
import com.threerings.parlor.rating.server.persist.RatingRepository;

/**
 * Rates players after each game and handles persisting the results.
 */
public abstract class RatingDelegate extends GameManagerDelegate
    implements RatingCodes
{
    @Override
    public void didInit (PlaceConfig config)
    {
        super.didInit(config);
        _gmgr = (GameManager)_plmgr;
    }

    @Override
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);
        _gobj = (GameObject) plobj;
    }

    @Override
    public void bodyEntered (int bodyOid)
    {
        super.bodyEntered(bodyOid);

        // if the game is in play and this is a player, load their ratings
        BodyObject occupant = (BodyObject)_omgr.getObject(bodyOid);
        if (shouldRateGame() && _gobj.isInPlay() && isPlayer(occupant)) {
            PlayerRating rating = maybeCreateRating(occupant);
            if (rating != null) {
                loadRatings(Collections.singleton(rating));
            }
        }
    }

    @Override
    public void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // flush this player's rating if its modified
        for (PlayerRating rating : _ratings.values()) {
            if (rating.playerOid == bodyOid && rating.modified) {
                saveRatings(Collections.singleton(rating.cloneForSave()));
                break;
            }
        }
    }

    @Override
    public void gameWillStart ()
    {
        super.gameWillStart();

        // if this game is not to be rated, stop here
        if (!shouldRateGame()) {
            return;
        }

        // note the time at which we started
        _startStamp = System.currentTimeMillis();

        // this contains the persistent player id for each position in a seated table game
        _playerIds = new int[_gmgr.getPlayerSlots()];

        // load up the ratings for all players in this game; also make a note of the persistent
        // player id of each player position for seated table games
        List<PlayerRating> toLoad = Lists.newArrayList();
        for (int ii = 0, ll = _gobj.occupants.size(); ii < ll; ii++) {
            BodyObject bobj = (BodyObject)_omgr.getObject(_gobj.occupants.get(ii));
            int pidx = _gmgr.getPlayerIndex(bobj.getVisibleName());
            if (pidx != -1) {
                _playerIds[pidx] = _gmgr.getPlayerPersistentId(bobj);
            }
            PlayerRating rating = maybeCreateRating(bobj);
            if (rating != null) {
                toLoad.add(rating);
            }
        }
        loadRatings(toLoad);
    }

    @Override
    public void gameDidEnd ()
    {
        super.gameDidEnd();

        // if this game is not to be rated, stop here
        if (!shouldRateGame()) {
            return;
        }

        // don't update ratings if the game did not run long enough
        int gameSecs = TimeUtil.elapsedSeconds(_startStamp, System.currentTimeMillis());
        if (gameSecs < minimumRatedDuration()) {
            return;
        }

        // compute our updated ratings
        updateRatings();

        // any players who are no longer in the room need their ratings flushed immediately
        List<PlayerRating> flushes = Lists.newArrayList();
        for (PlayerRating rating : _ratings.values()) {
            if (rating.modified && !_gobj.occupants.contains(rating.playerOid)) {
                flushes.add(rating.cloneForSave());
            }
        }
        saveRatings(flushes);
    }

    protected PlayerRating maybeCreateRating (BodyObject bobj)
    {
        // if this occupant is not a player (or not ratable), skip 'em
        int playerId = _gmgr.getPlayerPersistentId(bobj);
        if (playerId == 0) {
            return null;
        }
        // if this player's ratings are already loaded and their oids match, no need to reload
        PlayerRating orating = _ratings.get(playerId);
        if (orating != null && orating.playerOid == bobj.getOid()) {
            return null;
        }
        return new PlayerRating(bobj, playerId);
    }

    /**
     * Loads up rating information for the specified set of player ids and stores them in the
     * {@link #_ratings} table.
     */
    protected void loadRatings (final Collection<PlayerRating> ratings)
    {
        if (ratings.size() == 0) {
            return;
        }

        final int gameId = getGameId();
        _invoker.postUnit(new RepositoryUnit("loadRatings(" + gameId + ")") {
            @Override public void invokePersist () throws Exception {
                // map the records by player id so that we can correlate with the db results
                IntMap<PlayerRating> map = IntMaps.newHashIntMap();
                for (PlayerRating rating : ratings) {
                    map.put(rating.playerId, rating);
                }

                // load up the ratings data from the database, update the records
                Integer[] playerIds = map.keySet().toArray(new Integer[map.size()]);
                for (RatingRecord record : _repo.getRatings(gameId, playerIds)) {
                    PlayerRating rating = map.get(record.playerId);
                    if (rating != null) {
                        rating.rating = record.rating;
                        rating.experience = record.experience;
                    } // else { hell frozen over, pigs flying }
                }
            }

            @Override public void handleSuccess () {
                // stuff our populated records into the _ratings mapping
                for (PlayerRating rating : ratings) {
                    _ratings.put(rating.playerId, rating);
                }
            }
        });
    }

    protected void saveRatings (final Collection<PlayerRating> ratings)
    {
        if (ratings.size() == 0) {
            return;
        }

        final int gameId = getGameId();
        _invoker.postUnit(new RepositoryUnit("saveRatings(" + gameId + ")") {
            @Override public void invokePersist () throws Exception {
                for (PlayerRating rating : ratings) {
                    _repo.setRating(gameId, rating.playerId, rating.rating, rating.experience);
                }
            }

            @Override public void handleSuccess () {
                // let subclasses publish the new ratings if they so desire
                for (PlayerRating rating : ratings) {
                    updateRatingInMemory(gameId, rating);
                }
            }
        });
    }

    /**
     * Computes updated ratings for the players of this game. The default implementation uses the
     * {@link GameObject#winners} field to determine winners and losers and uses a FIDE/ELO
     * algorithm to compute updated ratings.
     */
    protected void updateRatings ()
    {
        // if no one won (or we're not a seated game), no ratings need be computed
        if (_gobj.getWinnerCount() == 0 || _playerIds.length == 0) {
            return;
        }

        PlayerRating[] ratings = new PlayerRating[_ratings.size()];
        for (int ii = 0; ii < ratings.length; ii++) {
            ratings[ii] = _ratings.get(_playerIds[ii]);
        }

        // compute the update ratings for all players
        int[] nratings = new int[_playerIds.length];
        for (int ii = 0; ii < nratings.length; ii++) {
            float W = _gobj.isDraw() ? 0.5f : _gobj.isWinner(ii) ? 1f : 0f;
            nratings[ii] = Rating.computeRating(ratings, ii, W);
        }

        // and write them back to their rating records
        for (int ii = 0; ii < nratings.length; ii++) {
            PlayerRating rating = ratings[ii];
            if (rating != null && nratings[ii] > 0) {
                rating.rating = nratings[ii];
                rating.experience++;
                rating.modified = true;
            }
        }
    }

    /**
     * Returns the game id to use when reading and writing ratings.
     */
    protected int getGameId ()
    {
        return _gmgr.getGameConfig().getGameId();
    }

    /**
     * Returns true if this game should be rated, false otherwise.
     */
    protected boolean shouldRateGame ()
    {
        return _gmgr.getGameConfig().rated;
    }

    /**
     * Return the minimum time (in seconds) a game must've lasted for it to count towards rating.
     */
    protected abstract int minimumRatedDuration ();

    /**
     * Optionally store update ratings in memory e.g. in the user object.
     *
     * This method is called on the dobj thread.
     */
    protected abstract void updateRatingInMemory (int gameId, PlayerRating rating);

    /**
     * Encapsulates the rating/experience tuple representing a player's rating for a game.
     */
    protected static class PlayerRating extends Rating
        implements Cloneable
    {
        /** The oid of the rated player. */
        public int playerOid;

        /** The name of the rated player. */
        public Name playerName;

        /** The id of the rated player. */
        public int playerId;

        /** Whether or not this rating needs to be written back to the database. */
        public boolean modified;

        /**
         * Sets up a new {@link PlayerRating} object with default values.
         */
        public PlayerRating (BodyObject player, int playerId)
        {
            this.playerOid = player.getOid();
            this.playerName = player.getVisibleName();
            this.playerId = playerId;
        }

        /**
         * Duplicates this rating and clears its modified status.
         */
        public PlayerRating cloneForSave ()
        {
            try {
                PlayerRating rating = (PlayerRating)this.clone();
                modified = false;
                return rating;
            } catch (CloneNotSupportedException cnse) {
                throw new AssertionError(cnse);
            }
        }
    }

    /** An appropriately casted reference to our GameManager. */
    protected GameManager _gmgr;

    /** An appropriately casted reference to our GameObject. */
    protected GameObject _gobj;

    /** Contains the persistent id of the players in this game. */
    protected int[] _playerIds;

    /** The ratings for each player as they were at the beginning of the game. */
    protected IntMap<PlayerRating> _ratings = IntMaps.newHashIntMap();

    /** A timestamp set at the beginning of the game, used to calculate its duration. */
    protected long _startStamp;

    // our dependencies
    @Inject protected RatingRepository _repo;
    @Inject protected @MainInvoker Invoker _invoker;
}
