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
 * Dispatched on the 'net' subcontrol when a message is sent by any client.
 */
public class MessageReceivedEvent extends EZEvent
{
    /** The type of all MessageReceivedEvents. */
    public static const TYPE :String = "msgReceived";

    /**
     * Access the message name.
     */
    public function get name () :String
    {
        return _name;
    }

    /**
     * Access the message value.
     */
    public function get value () :Object
    {
        return _value;
    }

    public function MessageReceivedEvent (
        gameCtrl :Object, messageName :String, value :Object)
    {
        super(TYPE, gameCtrl);
        _name = messageName;
        _value = value;
    }

    override public function toString () :String
    {
        return "[MessageReceivedEvent name=" + _name +
            ", value=" + _value + "]";
    }

    override public function clone () :Event
    {
        return new MessageReceivedEvent(_gameCtrl, _name, _value);
    }

    protected var _name :String;
    protected var _value :Object;
}
}
