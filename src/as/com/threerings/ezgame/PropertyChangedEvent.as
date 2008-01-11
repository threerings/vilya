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

package com.threerings.ezgame {

import flash.events.Event;

/**
 * Property change events are dispatched after the property change was
 * validated on the server.
 */
public class PropertyChangedEvent extends EZEvent
{
    /**
     * The type of a property change event.
     *
     * @eventType PropChanged
     */
    public static const PROPERTY_CHANGED :String = "PropChanged";

    /**
     * Get the name of the property that changed.
     */
    public function get name () :String
    {
        return _name;
    }

    /**
     * Get the property's new value.
     */
    public function get newValue () :Object
    {
        return _newValue;
    }

    /**
     * Get the property's previous value (handy!).
     */
    public function get oldValue () :Object
    {
        return _oldValue;
    }
    
    /**
     * If an array element was updated, get the index, or -1 if not applicable.
     */
    public function get index () :int
    {
        return _index;
    }

    /**
     * Constructor.
     */
    public function PropertyChangedEvent (
        gameCtrl :Object, propName :String, newValue :Object,
        oldValue :Object, index :int = -1)
    {
        super(PROPERTY_CHANGED, gameCtrl);
        _name = propName;
        _newValue = newValue;
        _oldValue = oldValue;
        _index = index;
    }

    override public function toString () :String
    {
        return "[PropertyChangedEvent name=" + _name + ", value=" + _newValue +
            ((_index < 0) ? "" : (", index=" + _index)) + "]";
    }

    override public function clone () :Event
    {
        return new PropertyChangedEvent(_gameCtrl, _name, _newValue, _oldValue, _index);
    }

    /** @private */
    protected var _name :String;

    /** @private */
    protected var _newValue :Object;

    /** @private */
    protected var _oldValue :Object;

    /** @private */
    protected var _index :int;
}
}
