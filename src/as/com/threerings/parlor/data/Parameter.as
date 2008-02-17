//
// $Id$

package com.threerings.parlor.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Defines a configuration parameter for a game. Various derived classes exist that define
 * particular types of configuration parameters including choices, toggles, ranges, etc.
 */
public /*abstract*/ class Parameter
    implements Streamable
{
    /** A string identifier that names this parameter. */
    public var ident :String;

    /** A human readable name for this configuration parameter. */
    public var name :String;

    /** A human readable tooltip to display when the mouse is hovered over this configuration
     * parameter. */
    public var tip :String;

    public function Parameter ()
    {
    }

    public function toString () :String
    {
        return ident;
    }

    public function getDefaultValue () :Object
    {
        throw new Error("Abstract");
    }

    /** Returns the translation key for this parameter's label. */
    public function getLabel () :String
    {
        return "m." + ident;
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        ident = (ins.readField(String) as String);
        name = (ins.readField(String) as String);
        tip = (ins.readField(String) as String);
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeField(ident);
        out.writeField(name);
        out.writeField(tip);
    }
}
}
