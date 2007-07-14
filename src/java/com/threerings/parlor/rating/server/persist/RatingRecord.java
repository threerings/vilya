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

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.expression.ColumnExp;

@Entity
public class RatingRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #gameId} field. */
    public static final String GAME_ID = "gameId";

    /** The qualified column identifier for the {@link #gameId} field. */
    public static final ColumnExp GAME_ID_C =
        new ColumnExp(RatingRecord.class, GAME_ID);

    /** The column identifier for the {@link #playerId} field. */
    public static final String PLAYER_ID = "playerId";

    /** The qualified column identifier for the {@link #playerId} field. */
    public static final ColumnExp PLAYER_ID_C =
        new ColumnExp(RatingRecord.class, PLAYER_ID);

    /** The column identifier for the {@link #rating} field. */
    public static final String RATING = "rating";

    /** The qualified column identifier for the {@link #rating} field. */
    public static final ColumnExp RATING_C =
        new ColumnExp(RatingRecord.class, RATING);

    /** The column identifier for the {@link #experience} field. */
    public static final String EXPERIENCE = "experience";

    /** The qualified column identifier for the {@link #experience} field. */
    public static final ColumnExp EXPERIENCE_C =
        new ColumnExp(RatingRecord.class, EXPERIENCE);
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 1;

    /** The identifier of the game we're rating for. */
    @Id
    public int gameId;

    /** The identifier of the player we're rating. */ 
    @Id
    public int playerId;
    
    /** The player's current rating. */
    public int rating;
    
    /** The number of times the player has played this game. */
    public int experience;

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
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link #RatingRecord}
     * with the supplied key values.
     */
    public static Key<RatingRecord> getKey (int gameId, int playerId)
    {
        return new Key<RatingRecord>(
                RatingRecord.class,
                new String[] { GAME_ID, PLAYER_ID },
                new Comparable[] { gameId, playerId });
    }
    // AUTO-GENERATED: METHODS END
}
