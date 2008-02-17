//
// $Id$

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
