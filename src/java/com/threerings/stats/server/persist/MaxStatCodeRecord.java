//
// $Id$

package com.threerings.stats.server.persist;

import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Computed;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.expression.ColumnExp;

@Computed @Entity
public class MaxStatCodeRecord extends PersistentRecord {

    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #maxCode} field. */
    public static final String MAX_CODE = "maxCode";

    /** The qualified column identifier for the {@link #maxCode} field. */
    public static final ColumnExp MAX_CODE_C =
        new ColumnExp(MaxStatCodeRecord.class, MAX_CODE);
    // AUTO-GENERATED: FIELDS END

    @Column
    public int maxCode;
}
