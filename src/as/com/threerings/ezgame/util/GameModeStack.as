//
// $Id: SubControl.as 271 2007-04-07 00:25:58Z dhoover $
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
    
/**
 * GameModeStack implements a stack of user-specific <i>game modes</i>. Modes can be pushed on to
 * or popped off of the stack. Whenever a new mode is pushed on the stack, the old top is cleaned
 * up, and the new one is initialized; vice versa for popping a mode off the stack.
 *
 * Usage example:
 * <pre>
 *   var mgr :GameModeStack = new GameModeStack(switchDisplayFn);
 *   mgr.push(new MainMenu());
 *   ...
 *   // in the main menu, we decide to pick a level
 *   mgr.push(new LevelSelectorScreen());
 *   ...
 *   // inside the level selector, we start the game:
 *   mgr.push(new GameScreen());
 *   ...
 *   // inside the game screen, once it was won or lost:
 *   mgr.pop();
 * </pre>
 */
public class GameModeStack 
{
    /**
     * Constructor, receives a function to be called whenever the top of the stack changes.
     *
     * @param callback Called whenever the top of the stack changes. Should be a function like:
     *   function (previousTop :GameMode, newTop :GameMode) :void { }
     *   - where previousTop is the mode previously selected, and newTop is the mode currently
     *   selected (either can be null).
     */
    public function GameModeStack (callback :Function)
    {
        _callback = callback;
    }

    /** Returns the top of the mode stack. If the stack is empty, returns null. */
    public function top () :GameMode
    {
        return (_stack.length > 0) ? _stack[0] : null; 
    }

    /** Pops all items off the stack. */
    public function clear () :void
    {
        while (top() != null) {
            pop();
        }
    }

    /**
     * Pushes a new game mode on top of the stack. 
     */
    public function push (newMode :GameMode) :void
    {
        var oldMode :GameMode = top();
        if (oldMode != null) {
            oldMode.pushedOnto(newMode);
        }
        
        _stack.unshift(newMode);
        newMode.pushed();

        _callback(oldMode, newMode);
    }

    /**
     * Pops and returns the current top game mode from the stack. Popping an empty stack is safe,
     * it simply returns null.
     */
    public function pop () :GameMode
    {
        var oldMode :GameMode = top();
        if (oldMode == null) {
            return null;
        }

        _stack.shift();
        oldMode.popped();

        var newMode :GameMode = top();
        if (newMode != null) {
            newMode.poppedFrom(oldMode);
        }

        _callback(oldMode, newMode);
        return oldMode;
    }

    /** Function that will be called every time the stack changes. */
    protected var _callback :Function;
    
    /** Internal storage for the stack. */
    protected var _stack :Array = new Array(); // of GameModes
}
}
