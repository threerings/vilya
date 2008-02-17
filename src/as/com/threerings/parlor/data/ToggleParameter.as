//
// $Id$

package com.threerings.parlor.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

/**
 * Models a parameter that allows the toggling of a single value.
 */
public class ToggleParameter extends Parameter
{
    /** The starting state for this parameter. */
    public var start :Boolean;

    public function ToggleParameter ()
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
        start = ins.readBoolean();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeBoolean(start);
    }
}
}
