package com.threerings.ezgame.client {

import flash.events.Event;

import flash.utils.ByteArray;

import com.threerings.util.Name;

import com.threerings.presents.dobj.MessageAdapter;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.presents.dobj.MessageEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;

import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;

import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.PropertySetEvent;
import com.threerings.ezgame.data.PropertySetListener;
import com.threerings.ezgame.util.EZObjectMarshaller;

import com.threerings.ezgame.MessageReceivedEvent;
import com.threerings.ezgame.PropertyChangedEvent;
import com.threerings.ezgame.StateChangedEvent;

/**
 * A controller for flash games.
 */
public class EZGameController extends GameController
    implements TurnGameController, PropertySetListener, MessageListener
{
    /** The implementation of the GameObject interface for users. */
    public var gameObjImpl :GameObjectImpl;

    /**
     */
    public function EZGameController ()
    {
        addDelegate(_turnDelegate = new TurnGameControllerDelegate(this));
    }

    override public function willEnterPlace (plobj :PlaceObject) :void
    {
        _ezObj = (plobj as EZGameObject);
        gameObjImpl = new GameObjectImpl(_ctx, _ezObj);

        _ctx.getClient().getClientObject().addListener(_userListener);

        super.willEnterPlace(plobj);
    }

    override public function didLeavePlace (plobj :PlaceObject) :void
    {
        super.didLeavePlace(plobj);

        _ctx.getClient().getClientObject().removeListener(_userListener);

        _ezObj = null;
    }

    // from TurnGameController
    public function turnDidChange (turnHolder :Name) :void
    {
        dispatchUserEvent(
            new StateChangedEvent(StateChangedEvent.TURN_CHANGED, gameObjImpl));
    }

    // from PropertySetListener
    public function propertyWasSet (event :PropertySetEvent) :void
    {
        // notify the user game
        dispatchUserEvent(new PropertyChangedEvent(
            gameObjImpl, event.getName(), event.getValue(),
            event.getOldValue(), event.getIndex()));
    }

    // from MessageListener
    public function messageReceived (event :MessageEvent) :void
    {
        var name :String = event.getName();
        if (EZGameObject.USER_MESSAGE == name) {
            dispatchUserMessage(event.getArgs());

        } else if (EZGameObject.GAME_CHAT == name) {
            // this is chat send by the game, let's route it like
            // localChat, which is also sent by the game
            gameObjImpl.localChat(String(event.getArgs()[0]));
        }
    }

    /**
     * Called by our user listener when we receive a message event
     * on the user object.
     */
    protected function messageReceivedOnUserObject (event :MessageEvent) :void
    {
        // see if it's a message about user games
        var msgName :String =
            EZGameObject.USER_MESSAGE + ":" + _ezObj.getOid();
        if (msgName == event.getName()) {
            dispatchUserMessage(event.getArgs());
        }
    }

    /**
     * Dispatch the user message.
     */
    protected function dispatchUserMessage (args :Array) :void
    {
        dispatchUserEvent(new MessageReceivedEvent(
            gameObjImpl, (args[0] as String),
            EZObjectMarshaller.decode(args[1])));
    }

    override protected function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        return new EZGamePanel(ctx, this);
    }

    override protected function gameDidStart () :void
    {
        super.gameDidStart();
        dispatchUserEvent(
            new StateChangedEvent(StateChangedEvent.GAME_STARTED, gameObjImpl));
    }

    override protected function gameDidEnd () :void
    {
        super.gameDidEnd();
        dispatchUserEvent(
            new StateChangedEvent(StateChangedEvent.GAME_ENDED, gameObjImpl));
    }

    protected function dispatchUserEvent (event :Event) :void
    {
        gameObjImpl.dispatch(event);
    }

    protected var _ezObj :EZGameObject;

    protected var _turnDelegate :TurnGameControllerDelegate;

    protected var _userListener :MessageAdapter =
        new MessageAdapter(messageReceivedOnUserObject);
}
}
