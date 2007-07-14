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

import java.util.List;
import java.util.logging.Level;

import com.samskivert.io.PersistenceException;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.IntMap;
import com.samskivert.util.Invoker;

import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.media.util.MathUtil;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.game.server.GameManagerDelegate;

import com.threerings.parlor.rating.server.persist.RatingRecord;
import com.threerings.parlor.rating.server.persist.RatingRepository;
import com.threerings.util.Name;

/**
 * Rates players after each game and handles persisting the results.
 */
public abstract class RatingManagerDelegate
    extends GameManagerDelegate
{
    /**
     * The minimum rating value. 
     */
    public static final int MINIMUM_RATING = 1000;

    /**
     * The default rating value. 
     */
    public static final int DEFAULT_RATING = 1200;

    /**
     * The maximum rating value. 
     */
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
    
    @Override
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);
        
        _gobj = (GameObject) plobj;
    }

    @Override
    public void gameWillStart ()
    {
        super.gameWillStart();

        // note the time at which we started
        _startStamp = (int) (System.currentTimeMillis() / 1000);

        // read ratings from persistent store
        CrowdServer.invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                int gameId = _gmgr.getGameConfig().getGameId();

                // enumerate our non-guest players
                Integer[] allPlayers = new Integer[_gmgr.getPlayerCount()];
                Integer[] ratedPlayers = new Integer[allPlayers.length];
                int jj = 0;
                for (int ii = 0; ii < allPlayers.length; ii ++) {
                    allPlayers[ii] = _gmgr.getPlayerPersistentId(_gmgr.getPlayer(ii));
                    if (allPlayers[ii] != 0) {
                        // this is a player with a persistent id, a player we can rate
                        ratedPlayers[jj ++] = allPlayers[ii];
                    }
                }
                ratedPlayers = ArrayUtil.splice(ratedPlayers, jj);

                try {
                    // fetch the previous ratings of our persistent users, for this game
                    List<RatingRecord> records = _repo.getRatings(gameId, ratedPlayers);
                    
                    // and make it easy to look them up by their id
                    IntMap<RatingRecord> map = new HashIntMap<RatingRecord>();
                    for (RatingRecord record : records) {
                        map.put(record.playerId, record);
                    }

                    // now build the array we keep around until the end of the game
                    _ratings = new Rating[allPlayers.length];
                    for (int ii = 0; ii < _ratings.length; ii ++) {
                        if (allPlayers[ii] == 0) {
                            // for guests we let the slot remain null
                            continue;
                        }
                        RatingRecord record = map.get(allPlayers[ii]);
                        // if the player had no previous record, initiate them at default values
                        _ratings[ii] = record != null ?
                            new Rating(allPlayers[ii], record.rating, record.experience) :
                            new Rating(allPlayers[ii]);
                    }

                    return true;
                    
                } catch (PersistenceException pe) {
                    log.log(Level.WARNING,
                        "Failed to load ratings [where=" + where() + ", id=" + gameId + "].", pe);
                    return false;
                }
            };

            public void handleResult () {
                // nothing to do here?
            }
        });
    }
            
    @Override
    public void gameDidEnd ()
    {
        super.gameDidEnd();

        // note the duration of the game (in seconds)
        int gameSecs = (int) (System.currentTimeMillis()/1000 - _startStamp);

        // update ratings if the game ran long enough, and the persistence code finished reading
        if (_ratings == null || gameSecs < minimumRatedDuration()) {
            return;
        }

        // compute the updated ratings
        int[] nratings = new int[_ratings.length];
        for (int ii = 0; ii < _ratings.length; ii ++) {
            nratings[ii] = _ratings[ii] != null ? computeRating(ii) : -1;
        }

        // and store them
        boolean modified = false;
        for (int ii = 0; ii < _ratings.length; ii++) {
            // skip this rating if it's a guest slot or we weren't able to compute a value
            if (nratings[ii] < 0) {
                continue;
            }
            _ratings[ii].rating = nratings[ii];
            _ratings[ii].experience ++;
            modified = true;
        }

        // bail if nothing changed
        if (!modified) {
            return;
        }

        // else persist the result
        final int gameId = _gmgr.getGameConfig().getGameId();
        CrowdServer.invoker.postUnit(new Invoker.Unit() {
            public boolean invoke () {
                try {
                    for (int ii = 0; ii < _ratings.length; ii ++) {
                        if (_ratings[ii] != null) {
                            // for each rated player, update or create the rating record
                            // TODO: reorganize things so this can be a single db request?
                            _repo.setRating(gameId, _ratings[ii].playerId,
                                            _ratings[ii].rating, _ratings[ii].experience);
                        }
                    }
                    return true;

                } catch (PersistenceException pe) {
                    log.log(Level.WARNING,
                        "Failed to load ratings [where=" + where() + ", id=" + gameId + "].", pe);
                    return false;
                }
            };

            public void handleResult () {
                // let subclasses store away the new ratings if they so desire
                for (int ii = 0; ii < _ratings.length; ii ++) {
                    if (_ratings[ii] != null) {
                        updateRatingInMemory(gameId, _gmgr.getPlayerName(ii), _ratings[ii]);
                    }
                }
            }
        });
    }

    /**
     * Computes a player's updated rating using a modified version of the FIDE/ELO system.
     * The rating adjustment is computed for the player versus each opponent individually and
     * these adjustments are summed and scaled by one over the number of opponents to create
     * the final rating adjustment, which is then added to the player's previous rating and
     * bounded to the rating range. <em>Note:</em> provisional players (those with experience of
     * less than 20) will be treated as having, at most, the default rating when used as an
     * opponent in calculatons for a non-provisional player.
     *
     * This method may be overriden to change the rating algorithm.
     *
     * @return the player's updated rating or -1 if none of the opponents could be applicably
     * rated against this player.
     */
    protected int computeRating (int pidx)
    {
        float totDeltaR = 0; // the total delta rating
        int opponents = 0;
    
        for (int ii = 0; ii < _ratings.length; ii++) {
            // we don't care how we did against ourselves, or against guests...
            if (pidx == ii || _ratings[ii] == null) {
                continue;
            }
    
            // if we are non-provisional, and the opponent is provisional, we
            // max the opponent out at the default rating to avoid potentially
            // inflating a real rating with one that has a lot of uncertainty
            int opprat = _ratings[ii].rating;
            if (_ratings[pidx].experience >= 20 && _ratings[ii].experience < 20) {
                opprat = Math.min(opprat, DEFAULT_RATING);
            }

            // calculate K, the score multiplier constant
            int K;
            if (_ratings[pidx].experience < 20) {
                K = 64;
            } else if (_ratings[pidx].rating < 2100) {
                K = 32; // experience >= 20
            } else if (_ratings[pidx].rating < 2400) {
                K = 24; // experience >= 20 && rating >= 2100
            } else {
                K = 16; // experience >= 20 && rating >= 2400
            }
            
            // calculate W, the win value representing the actual result of the game
            float W = _gobj.isDraw() ? 0.5f : _gobj.isWinner(pidx) ? 1f : 0f;
            // calculate We, the expected win value given the players' respective ratings
            float dR = opprat - _ratings[pidx].rating;
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
        int nrat = Math.round(_ratings[pidx].rating + totDeltaR/opponents);
        return MathUtil.bound(MINIMUM_RATING, nrat, MAXIMUM_RATING);
    }

    /**
     * Simply encapsulates the rating/experience tuple representing a player's rating for a game.
     */
    protected static class Rating
    {
        /** The id of the rated player. */
        public int playerId;
        
        /** The player's rating for our game. */
        public int rating;
        
        /** The number of times the player's played our game. */
        public int experience;

        /**
         * Sets up a new {@link Rating} object with default values.
         */
        public Rating (int playerId)
        {
            this.playerId = playerId;
            this.rating = DEFAULT_RATING;
            this.experience = 0;
        }
        
        /**
         * Sets up a new {@link Rating} object with the given values.
         */
        public Rating (int playerId, int rating, int experience)
        {
            this.playerId = playerId;
            this.rating = rating;
            this.experience = experience;
        }
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
    protected abstract void updateRatingInMemory (int gameId, Name playerName, Rating rating);


    /** An appropriately casted reference to our GameManager. */
    protected GameManager _gmgr;
    
    /** An appropriately casted reference to our GameObject. */
    protected GameObject _gobj;
    
    /** The RatingRepository that holds our data. */
    protected RatingRepository _repo;

    /** The ratings for each player as they were at the beginning of the game. */
    protected Rating[] _ratings;

    /** A timestamp set at the beginning of the game, used to calculate its duration. */
    protected int _startStamp;
}
