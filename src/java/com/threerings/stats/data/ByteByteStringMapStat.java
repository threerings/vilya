//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Maps up to 127 string values to integers in the range 0 - 127.
 */
public class ByteByteStringMapStat extends StringMapStat
{
    @Override // documentation inherited
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        out.writeByte(_keys.length);
        for (int ii = 0; ii < _keys.length; ii++) {
            out.writeByte((byte)aux.getStringCode(_type, _keys[ii]));
            out.writeByte((byte)_values[ii]);
        }
    }

    @Override // documentation inherited
    public void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException
    {
        _keys = new String[in.readByte()];
        _values = new int[_keys.length];
        for (int ii = 0; ii < _keys.length; ii++) {
            _keys[ii] = aux.getCodeString(_type, in.readByte());
            _values[ii] = in.readByte();
        }
    }

    @Override // from StringMapStat
    protected int getMaxValue ()
    {
        return Byte.MAX_VALUE;
    }
}
