//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;
import java.util.HashSet;

import com.samskivert.util.StringUtil;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

public class IntSetStat extends Stat
{
    /**
     * Constructs a new IntSetStat that will store an unbounded number of ints.
     */
    public IntSetStat ()
    {
        _maxSize = -1;
    }

    /**
     * Constructs a new IntSetStat that will store up to maxSize ints.
     */
    public IntSetStat (int maxSize)
    {
        _maxSize = maxSize;
    }

    /**
     * Returns the number of values stored in the set.
     */
    public int size ()
    {
        return _intSet.size();
    }

    /**
     * Returns true if the specified int is contained in this set.
     */
    public boolean contains (int key)
    {
        return _intSet.contains(key);
    }

    /**
     * Adds the specified int to this set.
     *
     * @return true if the int was newly added, false if it was already contained in the set.
     */
    public boolean add (int key)
    {
        return (_maxSize < 0 || _intSet.size() < _maxSize ? _intSet.add(key) : false);
    }

    @Override
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        out.writeByte(_intSet.size());
        for (int key : _intSet) {
            out.writeInt(key);
        }
    }

    @Override
    public void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException
    {
        int numValues = in.readByte();
        _intSet = new HashSet<Integer>(numValues);
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
    protected HashSet<Integer> _intSet = new HashSet<Integer>();

}
