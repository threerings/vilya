//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * A string set that maps its values to bytes.
 */
public class ByteStringSetStat extends StringSetStat
{
    @Override // documentation inherited
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        out.writeByte(_values.length);
        for (int ii = 0; ii < _values.length; ii++) {
            out.writeByte((byte)aux.getStringCode(_type, _values[ii]));
        }
    }

    @Override // documentation inherited
    public void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException
    {
        _values = new String[in.readByte()];
        for (int ii = 0; ii < _values.length; ii++) {
            _values[ii] = aux.getCodeString(_type, in.readByte());
        }
    }
}
