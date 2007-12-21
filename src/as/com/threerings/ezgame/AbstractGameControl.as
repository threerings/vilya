//
// $Id: EZGameControl.as 526 2007-12-13 01:42:10Z ray $
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

import flash.display.DisplayObject;

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;

import flash.geom.Point;

/**
 * Dispatched when the game client is unloaded and you should clean up any Timers or
 * other bits left hanging.
 *
 * @eventType flash.events.Event.UNLOAD
 */
[Event(name="unload", type="flash.events.Event")]

/**
 * The single point of control for each client in your multiplayer EZGame.
 */
public class AbstractGameControl extends AbstractControl
{
    /**
     * Create an EZGameControl object using some display object currently on the hierarchy.
     *
     * @param disp the display object that is the game's UI.
     * @param autoReady if true, the game will automatically be started when initialization is
     * complete, if false, the game will not start until all clients call {@link #playerReady}.
     */
    public function AbstractGameControl (disp :DisplayObject, autoReady :Boolean)
    {
        createSubControls();

        var event :DynEvent = new DynEvent();
        event.userProps = new Object();
        populateProperties(event.userProps);
        event.userProps["autoReady_v1"] = autoReady;
        disp.root.loaderInfo.sharedEvents.dispatchEvent(event);
        if ("ezProps" in event) {
            setHostProps(event.ezProps);
        }

        // set up our focusing click handler
        disp.root.addEventListener(MouseEvent.CLICK, handleRootClick);

        // set up the unload event to propagate
        disp.root.loaderInfo.addEventListener(Event.UNLOAD, dispatch);
    }

    override public function isConnected () :Boolean
    {
        return _connected;
    }

    /**
     * Create any subcontrols used by this game.
     */
    protected function createSubControls () :void
    {
        _subControls.push(
            _localCtrl = createLocalControl(),
            _netCtrl = createNetControl(),
            _playerCtrl = createPlayerControl(),
            _gameCtrl = createGameControl(),
            _servicesCtrl = createServicesControl()
        );
    }

    /**
     * Create the 'local' subcontrol.
     */
    protected function createLocalControl () :EZLocalSubControl
    {
        return new EZLocalSubControl(this);
    }

    /**
     * Create the 'net' subcontrol.
     */
    protected function createNetControl () :EZNetSubControl
    {
        return new EZNetSubControl(this);
    }

    /**
     * Create the 'player' subcontrol.
     */
    protected function createPlayerControl () :EZPlayerSubControl
    {
        return new EZPlayerSubControl(this);
    }

    /**
     * Create the 'game' subcontrol.
     */
    protected function createGameControl () :EZGameSubControl
    {
        return new EZGameSubControl(this);
    }

    /**
     * Create the 'services' subcontrol.
     */
    protected function createServicesControl () :EZServicesSubControl
    {
        return new EZServicesSubControl(this);
    }

    /**
     * Populate any properties or functions we want to expose to the other side of the ezgame
     * security boundary.
     */
    override protected function populateProperties (o :Object) :void
    {
        super.populateProperties(o);

        o["connectionClosed_v1"] = connectionClosed_v1;

        for each (var ctrl :AbstractSubControl in _subControls) {
            ctrl.populatePropertiesFriend(o);
        }
    }

    /**
     * Sets the properties we received from the host framework on the other side of the security
     * boundary.
     */
    override protected function setHostProps (o :Object) :void
    {
        super.setHostProps(o);

        // see if we're connected
        _connected = (o.gameData != null);

        for each (var ctrl :AbstractSubControl in _subControls) {
            ctrl.setHostPropsFriend(o);
        }

        // and assign our functions
        _funcs = o;
    }

    override protected function callHostCode (name :String, ... args) :*
    {
        if (_funcs != null) {
            try {
                var func :Function = (_funcs[name] as Function);
                if (func != null) {
                    return func.apply(null, args);
                }
            } catch (err :Error) {
                trace(err.getStackTrace());
                trace("--");
                throw new Error("Unable to call host code: " + err.message);
            }

        } else {
            // if _funcs is null, this will almost certainly throw an error..
            checkIsConnected();
        }
    }

    /**
     * Internal method that is called whenever the mouse clicks our root.
     */
    protected function handleRootClick (evt :MouseEvent) :void
    {
        if (!isConnected()) {
            return;
        }
        try {
            if (evt.target.stage == null || evt.target.stage.focus != null) {
                return;
            }
        } catch (err :SecurityError) {
        }
        callHostCode("focusContainer_v1");
    }

    /**
     * Private method called when the backend disconnects from us.
     */
    private function connectionClosed_v1 () :void
    {
        _connected = false;
    }

    /** Are we connected? */
    protected var _connected :Boolean;

    /** Contains functions exposed to us from the EZGame host. */
    protected var _funcs :Object;

    /** Holds all our sub-controls. */
    protected var _subControls :Array = [];

    /** Specific sub-controls. */
    protected var _localCtrl :EZLocalSubControl;
    protected var _netCtrl :EZNetSubControl;
    protected var _playerCtrl :EZPlayerSubControl;
    protected var _gameCtrl :EZGameSubControl;
    protected var _servicesCtrl :EZServicesSubControl;
}
}

import flash.events.Event;

dynamic class DynEvent extends Event
{
    public function DynEvent ()
    {
        super("ezgameQuery", true, false);
    }

    override public function clone () :Event
    {
        return new DynEvent();
    }
}
