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
import com.samskivert.depot.annotation.Index;
import com.samskivert.depot.expression.ColumnExp;

@Entity(name="STRING_CODES", indices={
    @Index(name="statCodeValue", unique=true),
    @Index(name="statCodeCode", unique=true)
})
public class StringCodeRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    public static final Class<StringCodeRecord> _R = StringCodeRecord.class;
    public static final ColumnExp<Integer> STAT_CODE = colexp(_R, "statCode");
    public static final ColumnExp<String> VALUE = colexp(_R, "value");
    public static final ColumnExp<Integer> CODE = colexp(_R, "code");
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 2;

    /** Defines the statCodeValue unique index. */
    public static ColumnExp<?>[] statCodeValue ()
    {
        return new ColumnExp<?>[] { STAT_CODE, VALUE };
    }

    /** Defines the statCodeCode unique index. */
    public static ColumnExp<?>[] statCodeCode ()
    {
        return new ColumnExp<?>[] { STAT_CODE, CODE };
    }

    /** The code of the stat. */
    @Id @Column(name="STAT_CODE")
    public int statCode;

    @Id @Column(name="VALUE")
    public String value;

    @Column(name="CODE")
    public int code;

    /**
     * An empty constructor for unmarshalling.
     */
    public StringCodeRecord ()
    {
        super();
    }

    /**
     * A constructor that populates all our fields.
     */
    public StringCodeRecord (int statCode, String value, int code)
    {
        super();
        this.statCode = statCode;
        this.value = value;
        this.code = code;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link StringCodeRecord}
     * with the supplied key values.
     */
    public static Key<StringCodeRecord> getKey (int statCode, String value)
    {
        return newKey(_R, statCode, value);
    }

    /** Register the key fields in an order matching the getKey() factory. */
    static { registerKeyFields(STAT_CODE, VALUE); }
    // AUTO-GENERATED: METHODS END
}
