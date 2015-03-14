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

import com.samskivert.util.StringUtil;

import com.threerings.media.util.MathUtil;

import com.threerings.parlor.rating.data.RatingCodes;

/**
 * Encapsulates the rating/experience tuple representing a player's rating for a game, and logic
 * for calculating rating changes.
 */
public class Rating
    implements RatingCodes
{
    /** The player's rating for our game. */
    public int rating = DEFAULT_RATING;

    /** The number of times the player's played our game. */
    public int experience;

    /**
     * Computes a player's updated rating using a modified version of the
     * FIDE/ELO system. The rating adjustment is computed for the player versus
     * each opponent individually and these adjustments are summed and scaled
     * by one over the number of opponents to create the final rating
     * adjustment, which is then added to the player's previous rating and
     * bounded to the rating range. <em>Note:</em> provisional players (those
     * with experience of less than 20) will be treated as having, at most, the
     * default rating when used as an opponent in calculatons for a
     * non-provisional player.
     *
     * @param ratings the pre-match ratings of each of the opponents.
     * @param pidx the index of the player whose rating is to be calculated.
     * @param W the win value for the player whose rating is to be calculated,
     * (1.0 means the player won, 0.5 means they drew, 0 means they lost).
     *
     * @return the player's updated rating or -1 if none of the opponents could
     * be applicably rated against this player due to provisional/
     * non-provisional mismatch or lack of participation.
     */
    public static int computeRating (Rating[] ratings, int pidx, float W)
    {
        float dR = 0; // the total delta rating
        int opponents = 0;

        for (int ii = 0; ii < ratings.length; ii++) {
            Rating orating = ratings[ii];
            // we don't care how we did against ourselves, or against guests...
            if (pidx == ii || orating == null) {
                continue;
            }

            // if we are non-provisional, and the opponent is provisional, we
            // max the opponent out at the default rating to avoid potentially
            // inflating a real rating with one that has a lot of uncertainty
            int opprat = ratings[ii].rating;
            if (!ratings[pidx].isProvisional() && ratings[ii].isProvisional()) {
                opprat = Math.min(opprat, DEFAULT_RATING);
            }
            dR += computeAdjustment(W, opprat, ratings[pidx]);
            opponents++;
        }

        // if we have no valid opponents, we cannot compute a rating
        if (opponents == 0) {
            return -1;
        }

        int nrat = Math.round(ratings[pidx].rating + dR/opponents);
        return MathUtil.bound(MINIMUM_RATING, nrat, MAXIMUM_RATING);
    }

    /**
     * Computes a ratings adjustment for the given player, using a modified
     * version of the FIDE Chess rating system as:
     *
     * <pre>{@code
     * adjustment = K(W - We)
     *
     * where:
     *
     * K = if (experience < 20) then 64
     *     else if (rating < 2100 and experience >= 20) then 32
     *     else if (rating >= 2100 and rating < 2400 and experience >= 20)
     *          then 24
     *     else 16
     * W = score for the game just completed, as 1.0, 0.5, and 0.0 for a
     * win, draw, or loss, respectively.
     * dR = opponent's rating minus player's rating.
     * We = expected score (win expectancy) as determined by:
     *
     *     We = 1 / (10^(dR/400) + 1)
     * }</pre>
     *
     * @param W the win value the game in question (1.0 means the player won,
     * 0.5 means they drew, 0 means they lost).
     * @param opprat the opponent's current rating.
     * @param rating the player's current rating.
     *
     * @return the adjustment to the player's rating.
     */
    public static float computeAdjustment (float W, int opprat, Rating rating)
    {
        // calculate We, the win expectancy
        float dR = opprat - rating.rating;
        float We = 1.0f / (float)(Math.pow(10.0f, (dR / 400.0f)) + 1);

        // calculate K, the score multiplier constant
        int K;
        if (rating.isProvisional()) {
            K = 64;
        } else if (rating.rating < 2100) {
            K = 32; // experience >= 20
        } else if (rating.rating < 2400) {
            K = 24; // experience >= 20 && rating >= 2100
        } else {
            K = 16; // experience >= 20 && rating >= 2400
        }

        // compute and return the ratings adjustment
        return K * (W - We);
    }

    /**
     * Returns true if this rating is provisional ({@code experience < 20}).
     */
    public boolean isProvisional ()
    {
        return (experience < 20);
    }

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
