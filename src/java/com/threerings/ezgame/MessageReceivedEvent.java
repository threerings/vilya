//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

package com.threerings.ezgame;

public class MessageReceivedEvent extends EZEvent
{
    public MessageReceivedEvent (
        EZGame ezgame, String messageName, Object value)
    {
        super(ezgame);
        _name = messageName;
        _value = value;
    }

    /**
     * Access the message name.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Access the message value.
     */
    public Object getValue ()
    {
        return _value;
    }

    public String toString ()
    {
        return "[MessageReceivedEvent name=" + _name +
            ", value=" + _value + "]";
    }

    protected String _name;
    protected Object _value;
}
