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
 * Dispatched when the state of the game has changed.
 */
public class StateChangedEvent extends EZEvent
{
    /** Indicates that the game has transitioned to a started state. */
    public static final String GAME_STARTED = "GameStarted";

    /** Indicates that the game has transitioned to a ended state. */
    public static final String GAME_ENDED = "GameEnded";

    /** Indicates that the turn has changed. */
    // TODO: move to own event?
    public static final String TURN_CHANGED = "TurnChanged";

    public StateChangedEvent (EZGame ezgame, String type)
    {
        super(ezgame);
        _type = type;
    }

    public String getType ()
    {
        return _type;
    }

    public String toString ()
    {
        return "[StateChangedEvent type=" + _type + "]";
    }

    protected String _type;
}
