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

import flash.geom.Point;

/**
 * Dispatched when the size of the game area changes, for example as a result of the user
 * resizing their browser window.
 */
public class SizeChangedEvent extends Event
{
    /**
     * The type of this event.
     *
     * @eventType SizeChanged
     */
    public static const SIZE_CHANGED :String = "SizeChanged";

    /**
     * Get the size of the game area, expressed as a Point
     * (The width is the x value, the height is the y value).
     */
    public function get size () :Point
    {
        return _size;
    }

    /**
     * Constructor.
     */
    public function SizeChangedEvent (size :Point)
    {
        super(SIZE_CHANGED);
        _size = size;
    }

    override public function toString () :String
    {
        return "[SizeChangedEvent size=" + _size + "]";
    }

    override public function clone () :Event
    {
        return new SizeChangedEvent(_size.clone()); // since _size is mutable
    }

    /** Our implementation details. @private */
    protected var _size: Point;
}
}
