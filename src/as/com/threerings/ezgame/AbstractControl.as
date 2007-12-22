//
// $Id: BaseControl.as 414 2007-08-23 00:32:36Z mdb $
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
 * The abstract base class for Game controls and subcontrols.
 */
public class AbstractControl extends EventDispatcher
{
    public function AbstractControl ()
    {
        if (Object(this).constructor == AbstractControl) {
            throw new IllegalOperationError("Abstract");
        }
    }

    /**
     * Are we connected and running inside the game environment, or has someone just
     * loaded up our SWF by itself?
     */
    public function isConnected () :Boolean
    {
        return false;
    }

    /**
     * Execute the specified function as a batch of commands that will be sent to the server
     * together. This is no different from executing the commands outside of a batch, but
     * may result in better use of the network and should be used if setting a number of things
     * at once.
     *
     * Example:
     * _ctrl.doBatch(function () :void {
     *     _ctrl.net.set("board", new Array());
     *     _ctrl.net.set("scores", new Array());
     *     _ctrl.net.set("captures", 0);
     * });
     */
    public function doBatch (fn :Function) :void
    {
        callHostCode("startTransaction");
        try {
            fn();
        } finally {
            callHostCode("commitTransaction");
        }
    }

    /**
     * Populate any properties or functions we want to expose to the host code.
     */
    protected function populateProperties (o :Object) :void
    {
        // nothing by default
    }

    /**
     * Grab any properties needed from our host code.
     */
    protected function setHostProps (o :Object) :void
    {
        // nothing by default
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
    protected function dispatch (event :Event) :void
    {
        try {
            super.dispatchEvent(event);
        } catch (err :Error) {
            trace("Error dispatching event to user game.");
            trace(err.getStackTrace());
        }
    }

    /**
     * Call a method exposed by the host code.
     */
    protected function callHostCode (name :String, ... args) :*
    {
        return undefined; // no-op by default
    }

    /**
     * Exposed to sub controls.
     */
    internal function callHostCodeFriend (name :String, args :Array) :*
    {
        args.unshift(name);
        return callHostCode.apply(this, args);
    }

    /**
     * Helper method to throw an error if we're not connected.
     */
    protected function checkIsConnected () :void
    {
        if (!isConnected()) {
            throw new IllegalOperationError(
                "The game is not connected to the host framework, please check isConnected(). " +
                "If false, your game is being viewed standalone and should adjust.");
        }
    }
}
}
