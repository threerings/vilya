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

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;

/**
 * The abstract base class for EZ controls.
 */
public class BaseControl extends EventDispatcher
{
    public function BaseControl ()
    {
        if (Object(this).constructor == BaseControl) {
            throw new IllegalOperationError("Abstract");
        }
    }

    /**
     * Your own events may not be dispatched here.
     */
    override public function dispatchEvent (event :Event) :Boolean
    {
        // Ideally we want to not be an EventDispatcher so that people
        // won't try to do this on us, but if we do that, then some other
        // object will be the target during dispatch, and that's confusing.
        throw new IllegalOperationError();
    }

    /**
     * Secret function to dispatch property changed events.
     */
    internal function dispatch (event :Event) :void
    {
        try {
            super.dispatchEvent(event);
        } catch (err :Error) {
            trace("Error dispatching event to user game.");
            trace(err.getStackTrace());
        }
    }
}
}
