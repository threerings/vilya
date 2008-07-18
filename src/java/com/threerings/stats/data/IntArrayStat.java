//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;

import java.util.Arrays;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Used to track an integer array statistic.
 */
public class IntArrayStat extends Stat
{
    /**
     * Returns the value of this statistic.
     */
    public int[] getValue ()
    {
        return _value;
    }

    /**
     * Sets this statistic's value to the specified value.
     *
     * @return true if the stat was modified, false if not.
     */
    public boolean setValue (int[] value)
    {
        if (!Arrays.equals(_value, value)) {
            _value = value;
            setModified(true);
            return true;
        }
        return false;
    }
    
    /**
     * Appends a value to this statistic.
     */
    public void appendValue (int value)
    {
        _value = ArrayUtil.append(_value, value);
        setModified(true);
    }
    
    @Override // documentation inherited
    public String valueToString ()
    {
        return StringUtil.toString(_value);
    }

    @Override // documentation inherited
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        out.writeObject(_value);
    }

    @Override // documentation inherited
    public void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException
    {
        _value = (int[])in.readObject();
    }

    /** Contains the integer list value of this statistic. */
    protected int[] _value = new int[0];
}
