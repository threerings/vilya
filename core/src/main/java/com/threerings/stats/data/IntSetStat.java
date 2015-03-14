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

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.StreamableArrayIntSet;

/**
 * Used to track a statistic comprised of a bounded set of integers.
 */
public class IntSetStat extends SetStat<Integer>
{
    /**
     * Constructs a new IntSetStat that will store up to 255 ints.
     */
    public IntSetStat ()
    {
        _maxSize = MAX_MAX_SIZE;
    }

    /**
     * Constructs a new IntSetStat that will store up to maxSize ints.
     *
     * @param maxSize the maximum number of ints to store in the IntSetStat. Must be {@code <= 255}.
     */
    public IntSetStat (int maxSize)
    {
        _maxSize = Math.min(maxSize, MAX_MAX_SIZE);
    }

    /**
     * Returns the number of values stored in the set.
     */
    @Override // from SetStat
    public int size ()
    {
        return _intSet.size();
    }

    /**
     * Returns true if the specified int is contained in this set.
     */
    @Override // from SetStat
    public boolean contains (Integer key)
    {
        return _intSet.contains(key);
    }

    /**
     * Adds the specified int to this set.
     *
     * @return true if the int was newly added, false if it was already contained in the set, or
     * if the set is full.
     */
    @Override // from SetStat
    public boolean add (Integer key)
    {
        boolean modified = (_intSet.size() < _maxSize) && _intSet.add(key);
        setModified(_modified || modified);
        return modified;
    }

    @Override
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        out.writeByte(_maxSize - 128);
        out.writeByte(_intSet.size() - 128);
        for (int key : _intSet) {
            out.writeInt(key);
        }
    }

    @Override
    public void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException
    {
        _maxSize = in.readByte() + 128;
        int numValues = in.readByte() + 128;
        _intSet = new StreamableArrayIntSet(numValues);
        for (int ii = 0; ii < numValues; ii++) {
            _intSet.add(in.readInt());
        }
    }

    @Override
    public String valueToString ()
    {
        return StringUtil.toString(_intSet);
    }

    protected int _maxSize;
    protected StreamableArrayIntSet _intSet = new StreamableArrayIntSet();

    protected static final int MAX_MAX_SIZE = 255;
}
