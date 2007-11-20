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

package com.threerings.ezgame.util {

/**
 * Interface for game modes, handled by the GameModeStack. Game modes are notified when they
 * become activated and deactivated, so that they can adjust themselves accordingly.
 */
public interface GameMode
{
    /**
     * Called when this instance of GameMode is added to the top of the stack.
     */
    function pushed () :void;

    /**
     * Called when this instance of GameMode is removed from the top of the stack.
     */
    function popped () :void;

    /**
     * Called when this instance of GameMode was the top of the stack, but another instance
     * is being pushed on top of it.
     */
    function pushedOnto (mode :GameMode) :void;

    /**
     * Called when another instance is being removed from the top of the stack,
     * making this instance the new top.
     */
    function poppedFrom (mode :GameMode) :void;
}
}
