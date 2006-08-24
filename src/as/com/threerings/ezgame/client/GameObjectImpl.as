package com.threerings.ezgame.client {

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;

import flash.utils.IExternalizable;
import flash.utils.ByteArray;

import com.threerings.io.TypedArray;

import com.threerings.util.ClassUtil;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.StringUtil;

import com.threerings.presents.client.ConfirmAdapter;
import com.threerings.presents.client.InvocationService_ConfirmListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.PropertySetEvent;
import com.threerings.ezgame.util.EZObjectMarshaller;

import com.threerings.ezgame.EZGame;
import com.threerings.ezgame.MessageReceivedEvent;
import com.threerings.ezgame.MessageReceivedListener;
import com.threerings.ezgame.PropertyChangedEvent;
import com.threerings.ezgame.PropertyChangedListener;
import com.threerings.ezgame.StateChangedEvent;
import com.threerings.ezgame.StateChangedListener;

public class GameObjectImpl extends EventDispatcher
    implements EZGame
{
    public function GameObjectImpl (ctx :CrowdContext, ezObj :EZGameObject)
    {
        _ctx = ctx;
        _ezObj = ezObj;
        _gameData = new GameData(this, _ezObj.getUserProps());
    }

    // from EZGame
    public function get data () :Object
    {
        return _gameData;
    }

    // from EZGame
    public function get (propName :String, index :int = -1) :Object
    {
        var value :Object = data[propName];
        if (index >= 0) {
            if (value is Array) {
                return (value as Array)[index];

            } else {
                throw new ArgumentError("Property " + propName +
                    " is not an array.");
            }
        }
        return value;
    }

    // from EZGame
    public function set (propName :String, value :Object, index :int = -1) :void
    {
        validatePropertyChange(propName, value, index);

        var encoded :Object = EZObjectMarshaller.encode(value);
        _ezObj.ezGameService.setProperty(
            _ctx.getClient(), propName, encoded, index,
            createLoggingListener("setProperty"));

        // set it immediately in the game object
        _ezObj.applyPropertySet(propName, value, index);
    }

    // from EZGame
    public function registerListener (obj :Object) :void
    {
        if (obj is MessageReceivedListener) {
            var mrl :MessageReceivedListener = (obj as MessageReceivedListener);
            addEventListener(
                MessageReceivedEvent.TYPE, mrl.messageReceived,
                false, 0, true);
        }
        if (obj is PropertyChangedListener) {
            var pcl :PropertyChangedListener = (obj as PropertyChangedListener);
            addEventListener(
                PropertyChangedEvent.TYPE, pcl.propertyChanged,
                false, 0, true);
        }
        if (obj is StateChangedListener) {
            var scl :StateChangedListener = (obj as StateChangedListener);
            addEventListener(
                StateChangedEvent.GAME_STARTED, scl.stateChanged,
                false, 0, true);
            addEventListener(
                StateChangedEvent.TURN_CHANGED, scl.stateChanged,
                false, 0, true);
            addEventListener(
                StateChangedEvent.GAME_ENDED, scl.stateChanged,
                false, 0, true);
        }
    }

    // from EZGame
    public function unregisterListener (obj :Object) :void
    {
        if (obj is MessageReceivedListener) {
            var mrl :MessageReceivedListener = (obj as MessageReceivedListener);
            removeEventListener(
                MessageReceivedEvent.TYPE, mrl.messageReceived);
        }
        if (obj is PropertyChangedListener) {
            var pcl :PropertyChangedListener = (obj as PropertyChangedListener);
            removeEventListener(
                PropertyChangedEvent.TYPE, pcl.propertyChanged);
        }
        if (obj is StateChangedListener) {
            var scl :StateChangedListener = (obj as StateChangedListener);
            removeEventListener(
                StateChangedEvent.GAME_STARTED, scl.stateChanged);
            removeEventListener(
                StateChangedEvent.TURN_CHANGED, scl.stateChanged);
            removeEventListener(
                StateChangedEvent.GAME_ENDED, scl.stateChanged);
        }
    }

    // from EZGame
    public function setCollection (collName :String, values :Array) :void
    {
        populateCollection(collName, values, true);
    }

    // from EZGame
    public function addToCollection (collName :String, values :Array) :void
    {
        populateCollection(collName, values, false);
    }

    // from EZGame
    public function pickFromCollection (
        collName :String, count :int, msgOrPropName :String,
        playerIndex :int = -1) :void
    {
        getFromCollection(collName, count, msgOrPropName, playerIndex,
            false, null);
    }

    // from EZGame
    public function dealFromCollection (
        collName :String, count :int, msgOrPropName :String,
        callback :Function = null, playerIndex :int = -1) :void
    {
        getFromCollection(collName, count, msgOrPropName, playerIndex,
            true, callback);
    }

    // from EZGame
    public function mergeCollection (srcColl :String, intoColl :String) :void
    {
        validateName(srcColl);
        validateName(intoColl);
        _ezObj.ezGameService.mergeCollection(_ctx.getClient(),
            srcColl, intoColl, createLoggingListener("mergeCollection"));
    }

    // from EZGame
    public function sendMessage (
        messageName :String, value :Object, playerIndex :int = -1) :void
    {
        validateName(messageName);
        validateValue(value);

        var encoded :Object = EZObjectMarshaller.encode(value);
        _ezObj.ezGameService.sendMessage(_ctx.getClient(),
            messageName, encoded, playerIndex,
            createLoggingListener("sendMessage"));
    }

    // from EZGame
    public function sendChat (msg :String) :void
    {
        validateChat(msg);
        // Post a message to the game object, the controller
        // will listen and call localChat().
        _ezObj.postMessage(EZGameObject.GAME_CHAT, [ msg ]);
    }

    // from EZGame
    public function localChat (msg :String) :void
    {
        validateChat(msg);
        // The sendChat() messages will end up being routed
        // through this method on each client.
        // TODO: make this look distinct from other system chat
        _ctx.getChatDirector().displayInfo(null, MessageBundle.taint(msg));
    }

    // from EZGame
    public function getPlayerNames () :Array
    {
        var names :Array = new Array();
        for each (var name :Name in _ezObj.players) {
            names.push((name == null) ? null : name.toString());
        }
        return names;
    }

    // from EZGame
    public function getMyIndex () :int
    {
        return _ezObj.getPlayerIndex(getUsername());
    }

    // from EZGame
    public function getTurnHolderIndex () :int
    {
        return _ezObj.getPlayerIndex(_ezObj.turnHolder);
    }

    // from EZGame
    public function getWinnerIndexes () :Array /* of int */
    {
        var arr :Array = new Array();
        if (_ezObj.winners != null) {
            for (var ii :int = 0; ii < _ezObj.winners.length; ii++) {
                if (_ezObj.winners[ii]) {
                    arr.push(ii);
                }
            }
        }
        return arr;
    }

    // from EZGame
    public function isMyTurn () :Boolean
    {
        return getUsername().equals(_ezObj.turnHolder);
    }

    // from EZGame
    public function isInPlay () :Boolean
    {
        return _ezObj.isInPlay();
    }

    // from EZGame
    public function endTurn (nextPlayerIndex :int = -1) :void
    {
        _ezObj.ezGameService.endTurn(_ctx.getClient(), nextPlayerIndex,
            createLoggingListener("endTurn"));
    }

    // from EZGame
    public function endGame (winnerIndex :int, ... rest) :void
    {
        var winners :TypedArray = TypedArray.create(int);
        winners.push(winnerIndex);
        while (rest.length > 0) {
            winners.push(int(rest.shift()));
        }
        _ezObj.ezGameService.endGame(_ctx.getClient(), winners,
            createLoggingListener("endGame"));
    }

    override public function willTrigger (type :String) :Boolean
    {
        throw new IllegalOperationError();
    }

    override public function dispatchEvent (event :Event) :Boolean
    {
        // Ideally we want to not be an IEventDispatcher so that people
        // won't try to do this on us, but if we do that, then some other
        // object will be the target during dispatch, and that's confusing.
        // It's really nice to be able to 
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
            var log :Log = Log.getLog(this);
            log.warning("Error dispatching event to user game.");
            log.logStackTrace(err);
        }
    }

    /**
     * Convenience function to get our name.
     */
    private function getUsername () :Name
    {
        var body :BodyObject = 
            (_ctx.getClient().getClientObject() as BodyObject);
        return body.getVisibleName();
    }

    /**
     * Create a listener for service requests.
     */
    private function createLoggingListener (
        service :String) :InvocationService_ConfirmListener
    {
        return new ConfirmAdapter(function (cause :String) :void {
            Log.getLog(this).warning("Service failure " +
                "[service=" + service + ", cause=" + cause + "].");
        });
    }

    /**
     * Helper method for setCollection and addToCollection.
     */
    private function populateCollection (
        collName :String, values :Array, clearExisting :Boolean) :void
    {
        validateName(collName);
        if (values == null) {
            throw new ArgumentError("Collection values may not be null.");
        }
        validateValue(values);

        var encodedValues :TypedArray =
            (EZObjectMarshaller.encode(values) as TypedArray);

        _ezObj.ezGameService.addToCollection(
            _ctx.getClient(), collName, encodedValues, clearExisting,
            createLoggingListener("populateCollection"));
    }

    /**
     * Helper method for pickFromCollection and dealFromCollection.
     */
    private function getFromCollection(
        collName :String, count :int, msgOrPropName :String, playerIndex :int,
        consume :Boolean, callback :Function) :void
    {
        validateName(collName);
        validateName(msgOrPropName);
        if (count < 1) {
            throw new ArgumentError("Must retrieve at least one element!");
        }

        var listener :InvocationService_ConfirmListener;
        if (callback != null) {
            // TODO: Figure out the method sig of the callback, and what it
            // means
            var fn :Function = function (cause :String = null) :void {
                if (cause == null) {
                    callback(count);
                } else {
                    callback(parseInt(cause));
                }
            };
            listener = new ConfirmAdapter(fn, fn);

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
    private function validatePropertyChange (
        propName :String, value :Object, index :int) :void
    {
        validateName(propName);

        // check that we're setting an array element on an array
        if (index >= 0) {
            if (!(_gameData[propName] is Array)) {
                throw new ArgumentError("Property " + propName +
                    " is not an Array.");
            }
        }

        // validate the value too
        validateValue(value);
    }

    /**
     * Verify that the specified name is valid.
     */
    private function validateName (name :String) :void
    {
        if (name == null) {
            throw new ArgumentError(
                "Property, message, and collection names must not be null.");
        }
    }

    private function validateChat (msg :String) :void
    {
        if (StringUtil.isBlank(msg)) {
            throw new ArgumentError(
                "Empty chat may not be displayed.");
        }
    }

    /**
     * Verify that the value is legal to be streamed to other clients.
     */
    private function validateValue (value :Object) :void
    {
        if (value == null) {
            return;

        } else if (value is IExternalizable) {
            throw new ArgumentError(
                "IExternalizable is not yet supported");

        } else if (value is Array) {
            if (ClassUtil.getClass(value) != Array) {
                // We can't allow arrays to be serialized as IExternalizables
                // because we need to know element values (opaquely) on the
                // server. Also, we don't allow other types because we wouldn't
                // create the right class on the other side.
                throw new ArgumentError(
                    "Custom array subclasses are not supported");
            }
            // then, continue on with the sub-properties check (below)

        } else {
            var type :String = typeof(value);
            if (type == "number" || type == "string" || type == "boolean" ) {
                // kosher!
                return;
            }
            if (ClassUtil.getClass(value) != Object) {
                throw new ArgumentError(
                    "Non-simple properties may not be set.");
            }
            // fall through and verify the object's sub-properties
        }

        // check sub-properties (of arrays and objects)
        for each (var arrValue :Object in (value as Array)) {
            validateValue(arrValue);
        }
    }

    protected var _ctx :CrowdContext;

    protected var _ezObj :EZGameObject;

    protected var _gameData :GameData;
}
}
