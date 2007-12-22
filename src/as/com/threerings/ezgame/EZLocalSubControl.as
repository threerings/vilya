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

/**
 * Dispatched when a key is pressed when the game has focus.
 *
 * @eventType flash.events.KeyboardEvent.KEY_DOWN
 */
[Event(name="keyDown", type="flash.events.KeyboardEvent")]

/**
 * Dispatched when a key is released when the game has focus.
 *
 * @eventType flash.events.KeyboardEvent.KEY_UP
 */
[Event(name="keyUp", type="flash.events.KeyboardEvent")]

/**
 * Dispatched when the size of the game area changes.
 *
 * @eventType com.threerings.ezgame.SizeChangedEvent.TYPE
 */
[Event(name="SizeChanged", type="com.threerings.ezgame.SizeChangedEvent")]


import flash.events.KeyboardEvent;

import flash.geom.Point;

/**
 * Access local properties of the game. Do not instantiate this class yourself,
 * access it via GameControl.local.
 */
public class EZLocalSubControl extends AbstractSubControl
{
    public function EZLocalSubControl (parent :AbstractGameControl)
    {
        super(parent);
    }

    // documentation inherited
    override public function addEventListener (
        type :String, listener :Function, useCapture :Boolean = false,
        priority :int = 0, useWeakReference :Boolean = false) :void
    {
        super.addEventListener(type, listener, useCapture, priority, useWeakReference);
    
        switch (type) {
        case KeyboardEvent.KEY_UP:
        case KeyboardEvent.KEY_DOWN:
            if (hasEventListener(type)) { // ensure it was added
                callHostCode("alterKeyEvents_v1", type, true);
            }
            break;
        }
    }

    // documentation inherited
    override public function removeEventListener (
        type :String, listener :Function, useCapture :Boolean = false) :void
    {
        super.removeEventListener(type, listener, useCapture);
    
        switch (type) {
        case KeyboardEvent.KEY_UP:
        case KeyboardEvent.KEY_DOWN:
            if (!hasEventListener(type)) { // once it's no longer needed
                callHostCode("alterKeyEvents_v1", type, false);
            }
            break;
        }
    }

    /**
     * Get the size of the game area, expressed as a Point
     * (x = width, y = height).
     */
    public function getSize () :Point
    {
        return callHostCode("getSize_v1") as Point;
    }

    /**
     * Display a feedback chat message for the local player only, no other players
     * or observers will see it.
     */
    public function feedback (msg :String) :void
    {
        callHostCode("localChat_v1", msg);
    }

    /**
     * Run the specified text through the user's chat filter. This is optional, you can use
     * it to clean up user-entered text.
     *
     * @return the filtered text, or null if it was so bad it's gone.
     */
    public function filter (text :String) :String
    {
        return (callHostCode("filter_v1", text) as String);
    }

    override protected function populateProperties (o :Object) :void
    {
        super.populateProperties(o);

        o["dispatchEvent_v1"] = dispatch; // for re-dispatching key events
        o["sizeChanged_v1"] = sizeChanged_v1;
    }

    /**
     * Private method to generate a SizeChangedEvent.
     */
    private function sizeChanged_v1 (size :Point) :void
    {
        dispatch(new SizeChangedEvent(_parent, size));
    }
}
}
