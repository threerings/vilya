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

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

import com.threerings.parlor.rating.util.Percentiler;

/**
 * Contains data for a {@link Percentiler} record.
 */
public class PercentileRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<PercentileRecord> _R = PercentileRecord.class;
    public static final ColumnExp<Integer> GAME_ID = colexp(_R, "gameId");
    public static final ColumnExp<Integer> GAME_MODE = colexp(_R, "gameMode");
    public static final ColumnExp<byte[]> DATA = colexp(_R, "data");
    // AUTO-GENERATED: FIELDS END

    /** Increment this value to reflect changes to this object's schema. */
    public static final int SCHEMA_VERSION = 4;

    /** The id of the game for which we're tracking a percentile distribution. */
    @Id public int gameId;

    /** The mode of the game. Games can maintain distributions for many different modes. */
    @Id public int gameMode;

    /** The raw percentiler data. */
    @Column(length=500)
    public byte[] data;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link PercentileRecord}
     * with the supplied key values.
     */
    public static Key<PercentileRecord> getKey (int gameId, int gameMode)
    {
        return newKey(_R, gameId, gameMode);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(GAME_ID, GAME_MODE); }
    // AUTO-GENERATED: METHODS END
}
