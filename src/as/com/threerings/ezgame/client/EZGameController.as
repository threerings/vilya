package com.threerings.ezgame.client {

import flash.events.Event;

import com.threerings.util.Name;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;

import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;

import com.threerings.ezgame.data.EZGameObject;

/**
 * A controller for flash games.
 */
public class EZGameController extends GameController
    implements TurnGameController
{
    /**
     */
    public function EZGameController ()
    {
        addDelegate(_turnDelegate = new TurnGameControllerDelegate(this));
    }

    override public function willEnterPlace (plobj :PlaceObject) :void
    {
        _ezObj = (plobj as EZGameObject);

        super.willEnterPlace(plobj);
    }

    override public function didLeavePlace (plobj :PlaceObject) :void
    {
        super.didLeavePlace(plobj);

        _ezObj = null;
    }

    // from TurnGameController
    public function turnDidChange (turnHolder :Name) :void
    {
        _panel.backend.turnDidChange();
    }

    override protected function gameDidStart () :void
    {
        super.gameDidStart();

        _panel.backend.gameDidStart();
    }

    override protected function gameDidEnd () :void
    {
        super.gameDidEnd();

        _panel.backend.gameDidEnd();
    }

    override protected function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        _panel = new EZGamePanel(ctx, this);
        return _panel;
    }

    protected var _ezObj :EZGameObject;

    protected var _turnDelegate :TurnGameControllerDelegate;

    protected var _panel :EZGamePanel;
}
}
