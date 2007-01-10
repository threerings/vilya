package com.threerings.ezgame.client {

import flash.display.DisplayObject;
import flash.display.DisplayObjectContainer;
import flash.display.Loader;

import flash.events.Event;

import flash.utils.Dictionary;

import mx.containers.Canvas;
import mx.containers.VBox;

import mx.core.Container;
import mx.core.IChildList;

import mx.utils.DisplayUtil;

import com.threerings.util.MediaContainer;

import com.threerings.mx.controls.ChatDisplayBox;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.ezgame.data.EZGameConfig;
import com.threerings.ezgame.data.EZGameObject;

public class EZGamePanel extends VBox
    implements PlaceView
{
    /** The game object backend. */
    public var backend :GameControlBackend;

    public function EZGamePanel (ctx :CrowdContext, ctrl :EZGameController)
    {
        _ctx = ctx;
        _ctrl = ctrl;

        //addChild(new ChatDisplayBox(ctx));
    }

    // from PlaceView
    public function willEnterPlace (plobj :PlaceObject) :void
    {
        var cfg :EZGameConfig = (_ctrl.getPlaceConfig() as EZGameConfig);

        _ezObj = (plobj as EZGameObject);
        backend = createBackend();

        _gameView = new GameContainer(cfg.configData); // TODO?
        backend.setSharedEvents(
            Loader(_gameView.getMedia()).contentLoaderInfo.sharedEvents);
        backend.setContainer(_gameView);
        addChild(_gameView);
    }

    // from PlaceView
    public function didLeavePlace (plobj :PlaceObject) :void
    {
        _gameView.shutdown(true);
        removeChild(_gameView);

        backend.shutdown();
    }

    /**
     * Creates the backend object that will handle requests from user code.
     */
    protected function createBackend () :GameControlBackend
    {
        return new GameControlBackend(_ctx, _ezObj);
    }
    
    protected var _ctx :CrowdContext;
    protected var _ctrl :EZGameController;

    protected var _gameView :GameContainer;

    protected var _ezObj :EZGameObject;
}
}
