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

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

/**
 * Used to track a statistic comprised of a set of strings.
 */
public abstract class StringSetStat extends SetStat<String>
{
    /**
     * Returns the number of Strings in this set.
     */
    @Override
    public int size ()
    {
        return _values.length;
    }

    /**
     * Returns true if the specified string is contained in this set.
     */
    @Override
    public boolean contains (String key)
    {
        if (key == null) {
            throw new IllegalArgumentException("StringSetStat cannot contain null");
        }
        return ArrayUtil.binarySearch(_values, 0, _values.length, key) >= 0;
    }

    /**
     * Adds the specified string to this set.
     *
     * @return true if the string was newly added, false if it was already
     * contained in the set.
     */
    @Override
    public boolean add (String key)
    {
        if (key == null) {
            throw new IllegalArgumentException("Cannot add null to StringSetStat");
        }

        int iidx = ArrayUtil.binarySearch(_values, 0, _values.length, key);
        if (iidx >= 0) {
            return false;
        }

        iidx = -iidx - 1;
        String[] values = new String[_values.length+1];
        System.arraycopy(_values, 0, values, 0, iidx);
        System.arraycopy(_values, iidx, values, iidx+1, (_values.length-iidx));
        values[iidx] = key;
        _values = values;
        setModified(true);
        return true;
    }

    /**
     * Returns the values in this set. <em>Do not</em> modify the returned array.
     */
    public String[] values ()
    {
        return _values;
    }

    @Override
    public String valueToString ()
    {
        return StringUtil.toString(_values);
    }

    /** Contains the strings in this set. */
    protected String[] _values = new String[0];
}
