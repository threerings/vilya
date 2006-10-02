package com.threerings.ezgame.client;

import com.threerings.util.Name;

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

import com.threerings.ezgame.EZEvent;
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
    public GameObjectImpl gameObjImpl;

    /**
     */
    public EZGameController ()
    {
        addDelegate(_turnDelegate = new TurnGameControllerDelegate(this));
    }

    @Override
    public void willEnterPlace (PlaceObject plobj)
    {
        _ezObj = (EZGameObject) plobj;
        gameObjImpl = new GameObjectImpl(_ctx, _ezObj);

        _ctx.getClient().getClientObject().addListener(_userListener);

        super.willEnterPlace(plobj);
    }

    @Override
    public void didLeavePlace (PlaceObject plobj)
    {
        super.didLeavePlace(plobj);

        _ctx.getClient().getClientObject().removeListener(_userListener);

        _ezObj = null;
    }

    // from TurnGameController
    public void turnDidChange (Name turnHolder)
    {
        dispatchUserEvent(
            new StateChangedEvent(gameObjImpl, StateChangedEvent.TURN_CHANGED));
    }

    // from PropertySetListener
    public void propertyWasSet (PropertySetEvent event)
    {
        // notify the user game
        dispatchUserEvent(new PropertyChangedEvent(
            gameObjImpl, event.getName(), event.getValue(),
            event.getOldValue(), event.getIndex()));
    }

    // from MessageListener
    public void messageReceived (MessageEvent event)
    {
        String name = event.getName();
        if (EZGameObject.USER_MESSAGE.equals(name)) {
            dispatchUserMessage(event.getArgs());

        } else if (EZGameObject.GAME_CHAT.equals(name)) {
            // this is chat send by the game, let's route it like
            // localChat, which is also sent by the game
            gameObjImpl.localChat((String) event.getArgs()[0]);

        } else if (EZGameObject.TICKER.equals(name)) {
            Object[] args = event.getArgs();
            dispatchUserEvent(new MessageReceivedEvent(
                gameObjImpl, (String) args[0], (Integer) args[1]));
        }
    }

    /**
     * Dispatch the user message.
     */
    protected void dispatchUserMessage (Object[] args)
    {
        dispatchUserEvent(new MessageReceivedEvent(
            gameObjImpl, (String) args[0],
            EZObjectMarshaller.decode(args[1])));
    }

    @Override
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new EZGamePanel(ctx, this);
    }

    @Override
    protected void gameDidStart ()
    {
        super.gameDidStart();
        dispatchUserEvent(
            new StateChangedEvent(gameObjImpl, StateChangedEvent.GAME_STARTED));
    }

    @Override
    protected void gameDidEnd ()
    {
        super.gameDidEnd();
        dispatchUserEvent(
            new StateChangedEvent(gameObjImpl, StateChangedEvent.GAME_ENDED));
    }

    protected void dispatchUserEvent (EZEvent event)
    {
        gameObjImpl.dispatch(event);
    }

    protected EZGameObject _ezObj;

    protected TurnGameControllerDelegate _turnDelegate;

    /** Listens for message events on the user object. */
    protected MessageListener _userListener = new MessageListener() {
        public void messageReceived (MessageEvent event) {
            // see if it's a message about user games
            String msgName = EZGameObject.USER_MESSAGE + ":" + _ezObj.getOid();
            if (msgName.equals(event.getName())) {
                dispatchUserMessage(event.getArgs());
            }
        }
    };
}
