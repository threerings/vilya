//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;
import java.io.EOFException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Extends the {@link IntStat} by maintaining a maximum value which is updated every time a value
 * is accumulated to the stat. Thus we track an accumulating value as well as the largest amount by
 * which it has ever accumulated.
 */
public class MaxIntStat extends IntStat
{
    /**
     * Returns the maximum value every accumulated to this integer statistic.
     */
    public int getMaxValue ()
    {
        return _maxValue;
    }

    @Override // from IntStat
    public boolean increment (int delta)
    {
        if (super.increment(delta)) {
            _maxValue = Math.max(delta, _maxValue);
            return true;
        }
        return false;
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
        try {
            _maxValue = in.readInt();
        } catch (EOFException eofe) {
            // hack to deal with old style IntStat converted to MaxIntStat
        }
    }

    /** The largest value ever accumulated to this stat. */
    protected int _maxValue;
}
