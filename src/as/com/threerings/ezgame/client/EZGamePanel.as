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

package com.threerings.ezgame.client {

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Loader;

import flash.events.Event;

import flash.utils.Dictionary;

import mx.containers.Canvas;

import mx.core.Container;
import mx.core.IChildList;

import com.threerings.flash.MediaContainer;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.ezgame.data.EZGameConfig;
import com.threerings.ezgame.data.EZGameObject;

public class EZGamePanel extends Canvas
    implements PlaceView
{
    /** The game object backend. */
    public var backend :GameControlBackend;

    public function EZGamePanel (ctx :CrowdContext, ctrl :EZGameController)
    {
        _ctx = ctx;
        _ctrl = ctrl;
    }

    // from PlaceView
    public function willEnterPlace (plobj :PlaceObject) :void
    {
        var cfg :EZGameConfig = (_ctrl.getPlaceConfig() as EZGameConfig);

        _ezObj = (plobj as EZGameObject);
        backend = createBackend();

        _gameView = new GameContainer(cfg.getGameDefinition().getMediaPath(cfg.getGameId()));
        configureGameView(_gameView);
        backend.setSharedEvents(
            Loader(_gameView.getMediaContainer().getMedia()).contentLoaderInfo.sharedEvents);
        backend.setContainer(_gameView);
        addChild(_gameView);
    }

    // from PlaceView
    public function didLeavePlace (plobj :PlaceObject) :void
    {
        _gameView.getMediaContainer().shutdown(true);
        removeChild(_gameView);

        backend.shutdown();
    }

    /**
     * Creates the backend object that will handle requests from user code.
     */
    protected function createBackend () :GameControlBackend
    {
        return new GameControlBackend(_ctx, _ezObj, _ctrl);
    }

    protected function configureGameView (view :GameContainer) :void
    {
        view.percentWidth = 100;
        view.percentHeight = 100;
    }
    
    protected var _ctx :CrowdContext;
    protected var _ctrl :EZGameController;
    protected var _gameView :GameContainer;
    protected var _ezObj :EZGameObject;
}
}
