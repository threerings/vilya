//
// $Id$

package com.threerings.parlor.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

/**
 * Models a parameter that allows the selection of one of a list of choices (specified as strings).
 */
public class ChoiceParameter extends Parameter
{
    /** The set of choices available for this parameter. */
    public var choices :TypedArray;

    /** The starting selection. */
    public var start :String;

    public function ChoiceParameter ()
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
        choices = TypedArray(ins.readObject());
        start = (ins.readField(String) as String);
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(choices);
        out.writeField(start);
    }
}
}
