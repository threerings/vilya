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

package com.threerings.stats.data;

import java.io.EOFException;
import java.io.IOException;

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
