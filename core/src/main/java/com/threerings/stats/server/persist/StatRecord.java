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

package com.threerings.stats.server.persist;

import com.samskivert.depot.Key;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.annotation.Id;
import com.samskivert.depot.expression.ColumnExp;

@Entity(name="STATS")
public class StatRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<StatRecord> _R = StatRecord.class;
    public static final ColumnExp<Integer> PLAYER_ID = colexp(_R, "playerId");
    public static final ColumnExp<Integer> STAT_CODE = colexp(_R, "statCode");
    public static final ColumnExp<byte[]> STAT_DATA = colexp(_R, "statData");
    public static final ColumnExp<Byte> MOD_COUNT = colexp(_R, "modCount");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 4;

    /** The identifier of the player this is a stat for. */
    @Id @Column(name="PLAYER_ID")
    public int playerId;

    /** The code of the stat. */
    @Id @Column(name="STAT_CODE")
    public int statCode;

    /** The data associated with the stat. */
    @Column(name="STAT_DATA", length=65535)
    public byte[] statData;

    /**
     * The number of times this stat has been updated, so that simultaneous attempts to update
     * the same stat in the repository can be caught and handled. Stored as a byte because
     * we don't expect to ever have more than a couple simultaneous attempts to update a stat.
     */
    public byte modCount;

    /**
     * An empty constructor for unmarshalling.
     */
    public StatRecord ()
    {
        super();
    }

    /**
     * A constructor that populates all our fields.
     */
    public StatRecord (int playerId, int statCode, byte[] data)
    {
        this(playerId, statCode, data, (byte)0);
    }

    public StatRecord (int playerId, int statCode, byte[] data, byte modCount)
    {
        super();
        this.playerId = playerId;
        this.statCode = statCode;
        this.statData = data;
        this.modCount = modCount;
    }

    @Override
    public String toString ()
    {
        return "playerId=" + playerId + " statCode=" + statCode + " modCount=" + modCount;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link StatRecord}
     * with the supplied key values.
     */
    public static Key<StatRecord> getKey (int playerId, int statCode)
    {
        return newKey(_R, playerId, statCode);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(PLAYER_ID, STAT_CODE); }
    // AUTO-GENERATED: METHODS END
}
