//
// $Id: PuzzleManagerDelegate.java 209 2007-02-24 00:37:33Z mdb $
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

package com.threerings.stats.server.persist;

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.annotation.Table;
import com.samskivert.jdbc.depot.annotation.UniqueConstraint;
import com.samskivert.jdbc.depot.expression.ColumnExp;

@Entity(name="STRING_CODES")
@Table(uniqueConstraints={
    @UniqueConstraint(columnNames={StringCodeRecord.STAT_CODE, StringCodeRecord.VALUE}),
    @UniqueConstraint(columnNames={StringCodeRecord.STAT_CODE, StringCodeRecord.CODE})})
public class StringCodeRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #statCode} field. */
    public static final String STAT_CODE = "statCode";

    /** The qualified column identifier for the {@link #statCode} field. */
    public static final ColumnExp STAT_CODE_C =
        new ColumnExp(StringCodeRecord.class, STAT_CODE);

    /** The column identifier for the {@link #value} field. */
    public static final String VALUE = "value";

    /** The qualified column identifier for the {@link #value} field. */
    public static final ColumnExp VALUE_C =
        new ColumnExp(StringCodeRecord.class, VALUE);

    /** The column identifier for the {@link #code} field. */
    public static final String CODE = "code";

    /** The qualified column identifier for the {@link #code} field. */
    public static final ColumnExp CODE_C =
        new ColumnExp(StringCodeRecord.class, CODE);
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 2;

    /** The code of the stat. */
    @Id
    @Column(name="STAT_CODE")
    public int statCode;

    @Id
    @Column(name="VALUE")
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
     * Create and return a primary {@link Key} to identify a {@link #StringCodeRecord}
     * with the supplied key values.
     */
    public static Key<StringCodeRecord> getKey (int statCode, String value)
    {
        return new Key<StringCodeRecord>(
                StringCodeRecord.class,
                new String[] { STAT_CODE, VALUE },
                new Comparable[] { statCode, value });
    }
    // AUTO-GENERATED: METHODS END
}
