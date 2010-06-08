// $Id: StageScenePanel.as 887 2010-01-05 22:12:02Z dhoover $
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.stage.client {

import flash.events.Event;
import flash.events.EventDispatcher;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.stage.util.StageContext;
import com.threerings.util.Controller;

/**
 * Eventually responsible for rendering a stage scene - but for now it's a stub.
 */
public class StageScenePanel
    implements PlaceView
{
    public function StageScenePanel (ctx :StageContext, ctrl :Controller)
    {
        _dispatcher = new EventDispatcher(this);
    }

    public function willEnterPlace (plobj :PlaceObject) :void
    {
    }

    public function didLeavePlace (plobj :PlaceObject) :void
    {
    }

    public function addEventListener (type :String, listener :Function, useCapture :Boolean = false,
        priority:int = 0, useWeakReference :Boolean = false) :void
    {
        _dispatcher.addEventListener(type, listener, useCapture, priority, useWeakReference);
    }

    public function dispatchEvent (event :Event) :Boolean
    {
        return _dispatcher.dispatchEvent(event);
    }

    public function hasEventListener (type :String) :Boolean
    {
        return _dispatcher.hasEventListener(type);
    }

    public function removeEventListener (type :String, listener :Function,
        useCapture :Boolean = false) :void
    {
        _dispatcher.removeEventListener(type, listener, useCapture);
    }

    public function willTrigger (type :String) :Boolean
    {
        return _dispatcher.willTrigger(type);
    }

    public function sceneUpdated (update :SceneUpdate) :void
    {
        // TODO
    }

    protected var _dispatcher :EventDispatcher;
}
}
