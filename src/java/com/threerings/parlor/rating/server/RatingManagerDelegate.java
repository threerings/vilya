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

package com.threerings.parlor.rating.server;

import static com.threerings.parlor.Log.log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.RepositoryUnit;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.util.Name;

import com.threerings.media.util.MathUtil;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.game.server.GameManagerDelegate;

import com.threerings.parlor.rating.server.persist.RatingRecord;
import com.threerings.parlor.rating.server.persist.RatingRepository;

/**
 * Rates players after each game and handles persisting the results.
 */
public abstract class RatingManagerDelegate extends GameManagerDelegate
{
    /** The minimum rating value. */
    public static final int MINIMUM_RATING = 1000;

    /** The default rating value. */
    public static final int DEFAULT_RATING = 1200;

    /** The maximum rating value. */
    public static final int MAXIMUM_RATING = 3000;

    /**
     * Constructs a rating manager delegate.
     */
    public RatingManagerDelegate (GameManager gmgr)
    {
        super(gmgr);
        _gmgr = gmgr;
        _repo = getRatingRepository();
    }

    @Override // from PlaceManagerDelegate
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);
        _gobj = (GameObject) plobj;
    }

    @Override // from PlaceManagerDelegate
    public void bodyEntered (int bodyOid)
    {
        super.bodyEntered(bodyOid);

        // if the game is in play and this is a player, load their ratings
        BodyObject occupant = (BodyObject)CrowdServer.omgr.getObject(bodyOid);
        if (_gobj.isInPlay() && isPlayer(occupant)) {
            Rating rating = maybeCreateRating(occupant);
            if (rating != null) {
                loadRatings(Collections.singleton(rating));
            }
        }
    }

    @Override // from PlaceManagerDelegate
    public void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // flush this player's rating if its modified
        for (Rating rating : _ratings.values()) {
            if (rating.playerOid == bodyOid && rating.modified) {
                saveRatings(Collections.singleton(rating.cloneForSave()));
                break;
            }
        }
    }

    @Override // from GameManagerDelegate
    public void gameWillStart ()
    {
        super.gameWillStart();

        // note the time at which we started
        _startStamp = (int) (System.currentTimeMillis() / 1000);

        // this contains the persistent player id for each position in a seated table game
        _playerIds = new int[_gmgr.getPlayerSlots()];

        // load up the ratings for all players in this game; also make a note of the persistent
        // player id of each player position for seated table games
        ArrayList<Rating> toLoad = new ArrayList<Rating>();
        for (int ii = 0, ll = _gobj.occupants.size(); ii < ll; ii++) {
            BodyObject bobj = (BodyObject)CrowdServer.omgr.getObject(_gobj.occupants.get(ii));
            int pidx = _gmgr.getPlayerIndex(bobj.getVisibleName());
            if (pidx != -1) {
                _playerIds[pidx] = _gmgr.getPlayerPersistentId(bobj);
            }
            Rating rating = maybeCreateRating(bobj);
            if (rating != null) {
                toLoad.add(rating);
            }
        }
        loadRatings(toLoad);
    }

    @Override // from GameManagerDelegate
    public void gameDidEnd ()
    {
        super.gameDidEnd();

        // don't update ratings if the game did not run long enough
        int gameSecs = (int) (System.currentTimeMillis()/1000 - _startStamp);
        if (gameSecs < minimumRatedDuration()) {
            return;
        }

        // compute our updated ratings
        updateRatings();

        // any players who are no longer in the room need their ratings flushed immediately
        ArrayList<Rating> flushes = new ArrayList<Rating>();
        for (Rating rating : _ratings.values()) {
            if (rating.modified && !_gobj.occupants.contains(rating.playerOid)) {
                flushes.add(rating.cloneForSave());
            }
        }
        saveRatings(flushes);
    }

    protected Rating maybeCreateRating (BodyObject bobj)
    {
        // if this occupant is not a player (or not ratable), skip 'em
        int playerId = _gmgr.getPlayerPersistentId(bobj);
        if (playerId == 0) {
            return null;
        }
        // if this player's ratings are already loaded and their oids match, no need to reload
        Rating orating = _ratings.get(playerId);
        if (orating != null && orating.playerOid == bobj.getOid()) {
            return null;
        }
        return new Rating(bobj, playerId);
    }

    /**
     * Returns true if the supplied occupant is a player, false if not.
     */
    protected boolean isPlayer (BodyObject occupant)
    {
        // avoid having to do this check all over the damned place
        if (occupant == null) {
            return false;
        }

        // if this is a seated table game, ask the game object
        if (_gobj.getPlayerIndex(occupant.getVisibleName()) != -1) {
            return true;
        }

        // if this is a party game, everyone's a player!
        if (_gmgr.getGameConfig().getMatchType() == GameConfig.PARTY) {
            return true;
        }

        // otherwise, sorry pardner
        return false;
    }

    /**
     * Loads up rating information for the specified set of player ids and stores them in the
     * {@link #_ratings} table.
     */
    protected void loadRatings (final Collection<Rating> ratings)
    {
        if (ratings.size() == 0) {
            return;
        }

        final int gameId = getGameId();
        CrowdServer.invoker.postUnit(new RepositoryUnit("loadRatings") {
            public void invokePersist () throws Exception {
                // map the records by player id so that we can correlate with the db results
                HashIntMap<Rating> map = new HashIntMap<Rating>();
                for (Rating rating : ratings) {
                    map.put(rating.playerId, rating);
                }

                // load up the ratings data from the database, update the records
                Integer[] playerIds = map.keySet().toArray(new Integer[map.size()]);
                for (RatingRecord record : _repo.getRatings(gameId, playerIds)) {
                    Rating rating = map.get(record.playerId);
                    if (rating != null) {
                        rating.rating = record.rating;
                        rating.experience = record.experience;
                    } // else { hell frozen over, pigs flying }
                }
            }

            public void handleSuccess () {
                // stuff our populated records into the _ratings mapping
                for (Rating rating : ratings) {
                    _ratings.put(rating.playerId, rating);
                }
            }

            public void handleFailure (Exception e) {
                log.log(Level.WARNING, "Failed to load ratings [where=" + where() +
                        ", id=" + gameId + "].", e);
            }
        });
    }

    protected void saveRatings (final Collection<Rating> ratings)
    {
        if (ratings.size() == 0) {
            return;
        }

        final int gameId = getGameId();
        CrowdServer.invoker.postUnit(new RepositoryUnit("saveRatings") {
            public void invokePersist () throws Exception {
                for (Rating rating : ratings) {
                    _repo.setRating(gameId, rating.playerId, rating.rating, rating.experience);
                }
            }

            public void handleSuccess () {
                // let subclasses publish the new ratings if they so desire
                for (Rating rating : ratings) {
                    updateRatingInMemory(gameId, rating);
                }
            }

            public void handleFailure (Exception e) {
                log.log(Level.WARNING, "Failed to update ratings [where=" + where() +
                        ", id=" + gameId + "].", e);
            }
        });
    }

    /**
     * Computes updated ratings for the players of this game. The default implementation uses the
     * {@link GameObject#winners} field to determine winners and losers and uses a FIDE/ELO
     * algorithm to comptue updated ratings.
     */
    protected void updateRatings ()
    {
        // if no one won (or we're not a seated game), no ratings need be computed
        if (_gobj.getWinnerCount() == 0 || _playerIds.length == 0) {
            return;
        }

        // compute the update ratings for all players
        int[] nratings = new int[_playerIds.length];
        for (int ii = 0; ii < nratings.length; ii ++) {
            nratings[ii] = computeRating(ii);
        }

        // and write them back to their rating records
        for (int ii = 0; ii < nratings.length; ii++) {
            Rating rating = _ratings.get(_playerIds[ii]);
            if (rating != null && nratings[ii] > 0) {
                rating.rating = nratings[ii];
                rating.experience++;
                rating.modified = true;
            }
        }
    }

    /**
     * Computes a player's updated rating using a modified version of the FIDE/ELO system.
     *
     * <p> The rating adjustment is computed for the player versus each opponent individually and
     * these adjustments are summed and scaled by one over the number of opponents to create the
     * final rating adjustment, which is then added to the player's previous rating and bounded to
     * the rating range. <em>Note:</em> provisional players (those with experience of less than 20)
     * will be treated as having, at most, the default rating when used as an opponent in
     * calculatons for a non-provisional player.
     *
     * @return the player's updated rating or -1 if none of the opponents could be applicably rated
     * against this player.
     */
    protected int computeRating (int pidx)
    {
        float totDeltaR = 0; // the total delta rating
        int opponents = 0;
        Rating prating = _ratings.get(_playerIds[pidx]);

        for (int ii = 0; ii < _playerIds.length; ii++) {
            Rating orating = _ratings.get(_playerIds[ii]);
            // we don't care how we did against ourselves, or against guests...
            if (pidx == ii || orating == null) {
                continue;
            }

            // if we are non-provisional, and the opponent is provisional, we max the opponent out
            // at the default rating to avoid potentially inflating a real rating with one that has
            // a lot of uncertainty
            int opprat = orating.rating;
            if (prating.experience >= 20 && orating.experience < 20) {
                opprat = Math.min(opprat, DEFAULT_RATING);
            }

            // calculate K, the score multiplier constant
            int K;
            if (prating.experience < 20) {
                K = 64;
            } else if (prating.rating < 2100) {
                K = 32; // experience >= 20
            } else if (prating.rating < 2400) {
                K = 24; // experience >= 20 && rating >= 2100
            } else {
                K = 16; // experience >= 20 && rating >= 2400
            }

            // calculate W, the win value representing the actual result of the game
            float W = _gobj.isDraw() ? 0.5f : _gobj.isWinner(pidx) ? 1f : 0f;
            // calculate We, the expected win value given the players' respective ratings
            float dR = opprat - prating.rating;
            float We = 1.0f / (float)(Math.pow(10.0f, (dR / 400.0f)) + 1);

            // update the total rating adjustment with the value for this opponent
            totDeltaR += K * (W - We);
            opponents++;
        }

        // if we have no valid opponents, we cannot compute a rating;
        if (opponents == 0) {
            return -1;
        }

        // return the updated and clamped rating
        int nrat = Math.round(prating.rating + totDeltaR/opponents);
        return MathUtil.bound(MINIMUM_RATING, nrat, MAXIMUM_RATING);
    }

    /**
     * Returns the game id to use when reading and writing ratings.
     */
    protected int getGameId ()
    {
        return _gmgr.getGameConfig().getGameId();
    }

    /**
     * Return the minimum time (in seconds) a game must've lasted for it to count towards rating.
     */
    protected abstract int minimumRatedDuration ();

    /**
     * Return a reference to the {@link RatingRepository} instance we should use to persist.
     */
    protected abstract RatingRepository getRatingRepository ();

    /**
     * Optionally store update ratings in memory e.g. in the user object.
     *
     * This method is called on the dobj thread.
     */
    protected abstract void updateRatingInMemory (int gameId, Rating rating);

    /**
     * Encapsulates the rating/experience tuple representing a player's rating for a game.
     */
    protected static class Rating
        implements Cloneable
    {
        /** The oid of the rated player. */
        public int playerOid;

        /** The name of the rated player. */
        public Name playerName;

        /** The id of the rated player. */
        public int playerId;

        /** The player's rating for our game. */
        public int rating;

        /** The number of times the player's played our game. */
        public int experience;

        /** Whether or not this rating needs to be written back to the database. */
        public boolean modified;

        /**
         * Sets up a new {@link Rating} object with default values.
         */
        public Rating (BodyObject player, int playerId)
        {
            this.playerOid = player.getOid();
            this.playerName = player.getVisibleName();
            this.playerId = playerId;
            this.rating = DEFAULT_RATING;
            this.experience = 0;
        }

        /**
         * Duplicates this rating and clears its modified status.
         */
        public Rating cloneForSave ()
        {
            try {
                Rating rating = (Rating)this.clone();
                modified = false;
                return rating;
            } catch (CloneNotSupportedException cnse) {
                throw new RuntimeException(cnse);
            }
        }

        @Override // from Object
        public String toString ()
        {
            return StringUtil.fieldsToString(this);
        }
    }

    /** An appropriately casted reference to our GameManager. */
    protected GameManager _gmgr;

    /** An appropriately casted reference to our GameObject. */
    protected GameObject _gobj;

    /** The RatingRepository that holds our data. */
    protected RatingRepository _repo;

    /** Contains the persistent id of the players in this game. */
    protected int[] _playerIds;

    /** The ratings for each player as they were at the beginning of the game. */
    protected HashIntMap<Rating> _ratings = new HashIntMap<Rating>();

    /** A timestamp set at the beginning of the game, used to calculate its duration. */
    protected int _startStamp;
}
