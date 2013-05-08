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

import com.samskivert.util.StringUtil;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.annotation.Index;
import com.samskivert.depot.expression.ColumnExp;

@Entity
public class RatingRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<RatingRecord> _R = RatingRecord.class;
    public static final ColumnExp<Integer> GAME_ID = colexp(_R, "gameId");
    public static final ColumnExp<Integer> PLAYER_ID = colexp(_R, "playerId");
    public static final ColumnExp<Integer> RATING = colexp(_R, "rating");
    public static final ColumnExp<Integer> EXPERIENCE = colexp(_R, "experience");
    public static final ColumnExp<Timestamp> LAST_UPDATED = colexp(_R, "lastUpdated");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 4;

    /** The identifier of the game we're rating for. */
    @Id public int gameId;

    /** The identifier of the player we're rating. */
    @Id @Index public int playerId;

    /** The player's current rating. */
    public int rating;

    /** The number of times the player has played this game. */
    public int experience;

    /** The last time this rating was updated. */
    public Timestamp lastUpdated;

    /**
     * An empty constructor for unmarshalling.
     */
    public RatingRecord ()
    {
        super();
    }

    /**
     * A constructor that populates all our fields.
     */
    public RatingRecord (int gameId, int playerId, int rating, int experience)
    {
        super();

        this.gameId = gameId;
        this.playerId = playerId;
        this.rating = rating;
        this.experience = experience;
        this.lastUpdated = new Timestamp(System.currentTimeMillis());
    }

    @Override // from Object
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link RatingRecord}
     * with the supplied key values.
     */
    public static Key<RatingRecord> getKey (int gameId, int playerId)
    {
        return newKey(_R, gameId, playerId);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(GAME_ID, PLAYER_ID); }
    // AUTO-GENERATED: METHODS END
}
