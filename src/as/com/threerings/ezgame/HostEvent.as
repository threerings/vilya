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
 * Dispatched by the host coordinator, to signal a change
 * in host status.
 */
public class HostEvent extends EZEvent
{
    /** Indicates that the current host changed. */
    public static const CHANGED :String = "Host Changed";

    /** Indicates that the host used to be unknown, but it became claimed
        by one of the clients. */
    public static const CLAIMED :String = "Host Claimed";

    public function HostEvent (type :String, ezgame :EZGameControl)
    {
        super (type, ezgame);
    }

    override public function toString () :String
    {
        return "[HostEvent type=" + type + "]";
    }

    override public function clone () :Event
    {
        return new HostEvent (type, _ezgame);
    }
}
}
