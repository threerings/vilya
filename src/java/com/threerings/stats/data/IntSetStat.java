//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;

import com.samskivert.util.StringUtil;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.util.StreamableArrayIntSet;

/**
 * Used to track a statistic comprised of a bounded set of integers.
 */
public class IntSetStat extends Stat
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
     * @param the maximum number of ints to store in the IntSetStat. Must be <= 255.
     */
    public IntSetStat (int maxSize)
    {
        _maxSize = Math.min(maxSize, MAX_MAX_SIZE);
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
     * @return true if the int was newly added, false if it was already contained in the set, or
     * if the set is full.
     */
    public boolean add (int key)
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
        _maxSize = ((int)in.readByte()) + 128;
        int numValues = ((int)in.readByte()) + 128;
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
