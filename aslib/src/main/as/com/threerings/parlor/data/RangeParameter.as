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

package com.threerings.parlor.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Models a parameter that can contain an integer value in a specified range.
 */
public class RangeParameter extends Parameter
{
    /** The minimum value of this parameter. */
    public var minimum :int;

    /** The maximum value of this parameter. */
    public var maximum :int;

    /** The starting value for this parameter. */
    public var start :int;

    public function RangeParameter ()
    {
    }

    // documentation inherited
    override public function getDefaultValue () :Object
    {
        return start;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        minimum = ins.readInt();
        maximum = ins.readInt();
        start = ins.readInt();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(minimum);
        out.writeInt(maximum);
        out.writeInt(start);
    }
}
}
