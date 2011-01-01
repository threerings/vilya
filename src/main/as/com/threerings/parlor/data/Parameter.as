//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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
