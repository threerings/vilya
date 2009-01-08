//
// $Id$

package com.threerings.stats.server.persist;

import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.annotation.Column;
import com.samskivert.depot.annotation.Computed;
import com.samskivert.depot.annotation.Entity;
import com.samskivert.depot.expression.ColumnExp;

@Computed @Entity
public class MaxStatCodeRecord extends PersistentRecord {

    // AUTO-GENERATED: FIELDS START
    public static final Class<MaxStatCodeRecord> _R = MaxStatCodeRecord.class;
    public static final ColumnExp MAX_CODE = colexp(_R, "maxCode");
    // AUTO-GENERATED: FIELDS END

    @Column
    public int maxCode;
}
