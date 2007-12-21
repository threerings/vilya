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

public class OccupantChangedEvent extends EZEvent
{
    public static const OCCUPANT_ENTERED :String = "OccupantEntered";
    public static const OCCUPANT_LEFT :String = "OccupantLeft";

    /** The occupantId of the occupant that entered or left. */
    public function get occupantId () :int
    {
        return _occupantId;
    }

    /** Is/was the occupant a player? If false, they are/were a watcher. */
    public function get player () :Boolean
    {
        return _player;
    }

    public function OccupantChangedEvent (
        type :String, gameCtrl :Object, occupantId :int, player :Boolean)
    {
        super(type, gameCtrl);
        _occupantId = occupantId;
        _player = player;
    }

    override public function toString () :String
    {
        return "[OccupantChangedEvent type=" + type +
            ", occupantId=" + _occupantId +
            ", player=" + _player + "]";
    }

    override public function clone () :Event
    {
        return new OccupantChangedEvent(type, _gameCtrl, _occupantId, _player);
    }

    protected var _occupantId :int;
    protected var _player :Boolean;
}
}
