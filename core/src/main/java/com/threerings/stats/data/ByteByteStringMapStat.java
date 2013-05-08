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
 * Maps up to 127 string values to integers in the range 0 - 127.
 */
public class ByteByteStringMapStat extends StringMapStat
{
    @Override
    public void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException
    {
        out.writeByte(_keys.length);
        for (int ii = 0; ii < _keys.length; ii++) {
            out.writeByte((byte)aux.getStringCode(_type, _keys[ii]));
            out.writeByte((byte)_values[ii]);
        }
    }

    @Override
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

    @Override
    protected int getMaxValue ()
    {
        return Byte.MAX_VALUE;
    }
}
