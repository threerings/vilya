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

/**
 * Property change events are dispatched after the property change was
 * validated on the server.
 */
public class PropertyChangedEvent extends EZEvent
{
    /**
     * Constructor.
     */
    public PropertyChangedEvent (
        EZGame ezgame, String propName, Object newValue, Object oldValue,
        int index)
    {
        super(ezgame);
        _name = propName;
        _newValue = newValue;
        _oldValue = oldValue;
        _index = index;
    }

    /**
     * Get the name of the property that changed.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Get the property's new value.
     */
    public Object getNewValue ()
    {
        return _newValue;
    }

    /**
     * Get the property's previous value (handy!).
     */
    public Object getOldValue ()
    {
        return _oldValue;
    }
    
    /**
     * If an array element was updated, get the index, or -1 if not applicable.
     */
    public int getIndex ()
    {
        return _index;
    }

    @Override
    public String toString ()
    {
        return "[PropertyChangedEvent name=" + _name + ", value=" + _newValue +
            ((_index < 0) ? "" : (", index=" + _index)) + "]";
    }

    /** Our implementation details. */
    protected String _name;
    protected Object _newValue;
    protected Object _oldValue;
    protected int _index;
}
