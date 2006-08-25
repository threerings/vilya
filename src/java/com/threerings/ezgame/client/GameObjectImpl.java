package com.threerings.ezgame.client;

import java.io.Externalizable;
import java.io.Serializable;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.CompactIntListUtil;
import com.samskivert.util.ObserverList;
import com.samskivert.util.StringUtil;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.Log; // well, fine

import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.PropertySetEvent;
import com.threerings.ezgame.util.EZObjectMarshaller;

import com.threerings.ezgame.EZGame;
import com.threerings.ezgame.EZEvent;
import com.threerings.ezgame.DealListener;
import com.threerings.ezgame.MessageReceivedEvent;
import com.threerings.ezgame.MessageReceivedListener;
import com.threerings.ezgame.PropertyChangedEvent;
import com.threerings.ezgame.PropertyChangedListener;
import com.threerings.ezgame.StateChangedEvent;
import com.threerings.ezgame.StateChangedListener;

public class GameObjectImpl
    implements EZGame
{
    public GameObjectImpl (CrowdContext ctx, EZGameObject ezObj)
    {
        _ctx = ctx;
        _ezObj = ezObj;
        _props = _ezObj.getUserProps();
    }

    // from EZGame
    public Object get (String propName)
    {
        return _props.get(propName);
    }

    // from EZGame
    public Object get (String propName, int index)
    {
        return ((Object[]) get(propName))[index];
    }

    // from EZGame
    public void set (String propName, Object value)
    {
        set(propName, value, -1);
    }

    // from EZGame
    public void set (String propName, Object value, int index)
    {
        validatePropertyChange(propName, value, -1);

        Object encoded = EZObjectMarshaller.encode(value);
        Object reconstituted = EZObjectMarshaller.decode(encoded);
        _ezObj.ezGameService.setProperty(
            _ctx.getClient(), propName, encoded, index,
            createLoggingListener("setProperty"));

        // set it immediately in the game object
        _ezObj.applyPropertySet(propName, reconstituted, index);
    }

    // from EZGame
    public void registerListener (Object obj)
    {
        if ((obj instanceof MessageReceivedListener) ||
            (obj instanceof PropertyChangedListener) ||
            (obj instanceof StateChangedListener)) {

            // silently ignore requests to listen twice
            if (!_listeners.contains(obj)) {
                _listeners.add(obj);
            }
        }
    }

    // from EZGame
    public void unregisterListener (Object obj)
    {
        _listeners.remove(obj);
    }

    // from EZGame
    public void setCollection (String collName, Object values)
    {
        populateCollection(collName, values, true);
    }

    // from EZGame
    public void addToCollection (String collName, Object values)
    {
        populateCollection(collName, values, false);
    }

    // from EZGame
    public void pickFromCollection (
        String collName, int count, String propName)
    {
        getFromCollection(collName, count, propName, -1, false, null);
    }

    // from EZGame
    public void pickFromCollection (
        String collName, int count, String msgName, int playerIndex)
    {
        getFromCollection(collName, count, msgName, playerIndex, false, null);
    }

    // from EZGame
    public void dealFromCollection (
        String collName, int count, String propName,
        DealListener listener)
    {
        getFromCollection(collName, count, propName, -1, true, listener);
    }

    // from EZGame
    public void dealFromCollection (
        String collName, int count, String msgName,
        DealListener listener, int playerIndex)
    {
        getFromCollection(collName, count, msgName, playerIndex, true, listener);
    }

    // from EZGame
    public void mergeCollection (String srcColl, String intoColl)
    {
        validateName(srcColl);
        validateName(intoColl);
        _ezObj.ezGameService.mergeCollection(_ctx.getClient(),
            srcColl, intoColl, createLoggingListener("mergeCollection"));
    }

    // from EZGame
    public void sendMessage (String messageName, Object value)
    {
        sendMessage(messageName, value, -1);
    }

    // from EZGame
    public void sendMessage (String messageName, Object value, int playerIndex)
    {
        validateName(messageName);
        validateValue(value);

        Object encoded = EZObjectMarshaller.encode(value);
        _ezObj.ezGameService.sendMessage(_ctx.getClient(),
            messageName, encoded, playerIndex,
            createLoggingListener("sendMessage"));
    }

    // from EZGame
    public void sendChat (String msg)
    {
        validateChat(msg);
        // Post a message to the game object, the controller
        // will listen and call localChat().
        _ezObj.postMessage(EZGameObject.GAME_CHAT, new Object[] { msg });
    }

    // from EZGame
    public void localChat (String msg)
    {
        validateChat(msg);
        // The sendChat() messages will end up being routed
        // through this method on each client.
        // TODO: make this look distinct from other system chat
        _ctx.getChatDirector().displayInfo(null, MessageBundle.taint(msg));
    }

    // from EZGame
    public String[] getPlayerNames ()
    {
        String[] names = new String[_ezObj.players.length];
        int index = 0;
        for (Name name : _ezObj.players) {
            names[index++] = (name == null) ? null : name.toString();
        }
        return names;
    }

    // from EZGame
    public int getMyIndex ()
    {
        return _ezObj.getPlayerIndex(getUsername());
    }

    // from EZGame
    public int getTurnHolderIndex ()
    {
        return _ezObj.getPlayerIndex(_ezObj.turnHolder);
    }

    // from EZGame
    public int[] getWinnerIndexes ()
    {
        int[] winners = new int[0];
        if (_ezObj.winners != null) {
            for (int ii = 0; ii < _ezObj.winners.length; ii++) {
                if (_ezObj.winners[ii]) {
                    winners = CompactIntListUtil.add(winners, ii);
                }
            }
        }
        return winners;
    }

    // from EZGame
    public boolean isMyTurn ()
    {
        return getUsername().equals(_ezObj.turnHolder);
    }

    // from EZGame
    public boolean isInPlay ()
    {
        return _ezObj.isInPlay();
    }

    // from EZGame
    public void endTurn ()
    {
        endTurn(-1);
    }

    // from EZGame
    public void endTurn (int nextPlayerIndex)
    {
        _ezObj.ezGameService.endTurn(_ctx.getClient(), nextPlayerIndex,
            createLoggingListener("endTurn"));
    }

    // from EZGame
    public void endGame (int... winners)
    {
        _ezObj.ezGameService.endGame(_ctx.getClient(), winners,
            createLoggingListener("endGame"));
    }

    /**
     * Secret function to dispatch property changed events.
     */
    void dispatch (EZEvent event)
    {
        ObserverList.ObserverOp<Object> op;

        if (event instanceof PropertyChangedEvent) {
            final PropertyChangedEvent pce = (PropertyChangedEvent) event;
            op = new ObserverList.ObserverOp<Object>() {
                public boolean apply (Object obs) {
                    if (obs instanceof PropertyChangedListener) {
                        ((PropertyChangedListener) obs).propertyChanged(pce);
                    }
                    return true;
                }
            };

        } else if (event instanceof StateChangedEvent) {
            final StateChangedEvent sce = (StateChangedEvent) event;
            op = new ObserverList.ObserverOp<Object>() {
                public boolean apply (Object obs) {
                    if (obs instanceof StateChangedListener) {
                        ((StateChangedListener) obs).stateChanged(sce);
                    }
                    return true;
                }
            };

        } else if (event instanceof MessageReceivedEvent) {
            final MessageReceivedEvent mre = (MessageReceivedEvent) event;
            op = new ObserverList.ObserverOp<Object>() {
                public boolean apply (Object obs) {
                    if (obs instanceof MessageReceivedListener) {
                        ((MessageReceivedListener) obs).messageReceived(mre);
                    }
                    return true;
                }
            };

        } else {
            throw new IllegalArgumentException("Please implement");
        }

        // and apply the operation
        _listeners.apply(op);
    }

    /**
     * Convenience function to get our name.
     */
    private Name getUsername ()
    {
        BodyObject body = (BodyObject) _ctx.getClient().getClientObject();
        return body.getVisibleName();
    }

    /**
     * Create a listener for service requests.
     */
    private InvocationService.ConfirmListener createLoggingListener (
        final String service)
    {
        return new InvocationService.ConfirmListener() {
            public void requestFailed (String cause)
            {
                Log.warning("Service failure " +
                    "[service=" + service + ", cause=" + cause + "].");
            }

            public void requestProcessed ()
            {
                // nada
            }
        };
    }

    /**
     * Helper method for setCollection and addToCollection.
     */
    private void populateCollection (
        String collName, Object values, boolean clearExisting)
    {
        validateName(collName);
        if (values == null) {
            throw new IllegalArgumentException(
                "Collection values may not be null.");
        }
        validateValue(values);

        byte[][] encodedValues = (byte[][]) EZObjectMarshaller.encode(values);

        _ezObj.ezGameService.addToCollection(
            _ctx.getClient(), collName, encodedValues, clearExisting,
            createLoggingListener("populateCollection"));
    }

    /**
     * Helper method for pickFromCollection and dealFromCollection.
     */
    private void getFromCollection(
        String collName, final int count, String msgOrPropName, int playerIndex,
        boolean consume, final DealListener dealy)
    {
        validateName(collName);
        validateName(msgOrPropName);
        if (count < 1) {
            throw new IllegalArgumentException(
                "Must retrieve at least one element!");
        }

        InvocationService.ConfirmListener listener;
        if (dealy != null) {
            // TODO: Figure out the method sig of the callback, and what it
            // means
            listener = new InvocationService.ConfirmListener() {
                public void requestFailed (String cause) {
                    try {
                        dealy.dealt(Integer.parseInt(cause));
                    } catch (NumberFormatException nfe) {
                        // nada
                    }
                }

                public void requestProcessed () {
                    dealy.dealt(count);
                }
            };

        } else {
            listener = createLoggingListener("getFromCollection");
        }

        _ezObj.ezGameService.getFromCollection(
            _ctx.getClient(), collName, consume, count, msgOrPropName,
            playerIndex, listener);
    }

    /**
     * Verify that the property name / value are valid.
     */
    private void validatePropertyChange (
        String propName, Object value, int index)
    {
        validateName(propName);

        // check that we're setting an array element on an array
        if (index >= 0) {
            if (!(get(propName) instanceof Object[])) {
                throw new IllegalArgumentException("Property " + propName +
                    " is not an Array.");
            }
        }

        // validate the value too
        validateValue(value);
    }

    /**
     * Verify that the specified name is valid.
     */
    private void validateName (String name)
    {
        if (name == null) {
            throw new IllegalArgumentException(
                "Property, message, and collection names must not be null.");
        }
    }

    private void validateChat (String msg)
    {
        if (StringUtil.isBlank(msg)) {
            throw new IllegalArgumentException(
                "Empty chat may not be displayed.");
        }
    }

    /**
     * Verify that the value is legal to be streamed to other clients.
     */
    private void validateValue (Object value)
    {
        if (value == null) {
            return;

        } else if (value instanceof Externalizable) {
            throw new IllegalArgumentException(
                "IExternalizable is not yet supported");

        } else if (value.getClass().isArray()) {
            int length = Array.getLength(value);
            for (int ii=0; ii < length; ii++) {
                validateValue(Array.get(value, ii));
            }

        } else if (value instanceof Iterable) {
            for (Object o : (Iterable) value) {
                validateValue(o);
            }

        } else if (!(value instanceof Serializable)) {
            throw new IllegalArgumentException(
                "Non-serializable properties may not be set.");
        }
    }

    protected CrowdContext _ctx;

    protected EZGameObject _ezObj;

    protected HashMap<String, Object> _props;

    protected ObserverList<Object> _listeners =
        new ObserverList<Object>(ObserverList.SAFE_IN_ORDER_NOTIFY);
}
