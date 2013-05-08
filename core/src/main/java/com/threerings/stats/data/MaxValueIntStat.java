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

import java.io.IOException;

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
