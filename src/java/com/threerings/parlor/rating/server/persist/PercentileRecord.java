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
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.expression.ColumnExp;

import com.threerings.parlor.rating.util.Percentiler;

/**
 * Contains data for a {@link Percentiler} record.
 */
public class PercentileRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #gameId} field. */
    public static final String GAME_ID = "gameId";

    /** The qualified column identifier for the {@link #gameId} field. */
    public static final ColumnExp GAME_ID_C =
        new ColumnExp(PercentileRecord.class, GAME_ID);

    /** The column identifier for the {@link #type} field. */
    public static final String TYPE = "type";

    /** The qualified column identifier for the {@link #type} field. */
    public static final ColumnExp TYPE_C =
        new ColumnExp(PercentileRecord.class, TYPE);

    /** The column identifier for the {@link #data} field. */
    public static final String DATA = "data";

    /** The qualified column identifier for the {@link #data} field. */
    public static final ColumnExp DATA_C =
        new ColumnExp(PercentileRecord.class, DATA);
    // AUTO-GENERATED: FIELDS END

    /** Increment this value to reflect changes to this object's schema. */
    public static final int SCHEMA_VERSION = 2;

    /** The id of the game for which we're tracking a percentile distribution. */
    @Id
    public int gameId;

    /** The type of percentile distribution (games can maintain multiple distributions). */
    @Id
    public int type;

    /** The raw percentiler data. */
    @Column(length=500)
    public byte[] data;

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link #PercentileRecord}
     * with the supplied key values.
     */
    public static Key<PercentileRecord> getKey (int gameId, int type)
    {
        return new Key<PercentileRecord>(
                PercentileRecord.class,
                new String[] { GAME_ID, TYPE },
                new Comparable[] { gameId, type });
    }
    // AUTO-GENERATED: METHODS END
}
