//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;
import java.io.EOFException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Extends the {@link IntStat} by maintaining the maximum value that the stat has ever been
 * assigned (unlike {@link MaxIntStat}, which tracks the maximum value that the stat has ever
 * been incremented by).
 */
public class MaxValueIntStat extends IntStat
{
    /**
     * Returns the maximum value that this integer statistic has ever been assigned.
     */
    public int getMaxValue ()
    {
        return _maxValue;
    }

    @Override // from IntStat
    public boolean setValue (int value)
    {
        _maxValue = Math.max(value, _maxValue);
        return super.setValue(value);
    }

    @Override // from IntStat
    public String valueToString ()
    {
        return super.valueToString() + " " + String.valueOf(_maxValue);
    }

    @Override // from IntStat
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        super.persistTo(out, aux);
        out.writeInt(_maxValue);
    }

    @Override // from IntStat
    public void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException
    {
        super.unpersistFrom(in, aux);
        _maxValue = in.readInt();
    }

    /** The largest value that this stat has been assigned. */
    protected int _maxValue;
}
