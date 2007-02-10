package com.threerings.ezgame.client {

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.IEventDispatcher;
import flash.events.KeyboardEvent;

import flash.events.MouseEvent;

import flash.display.DisplayObject;
import flash.display.InteractiveObject;

import flash.utils.IExternalizable;
import flash.utils.ByteArray;
import flash.utils.Dictionary;
import flash.utils.getQualifiedSuperclassName;

import com.threerings.io.TypedArray;

import com.threerings.util.ClassUtil;
import com.threerings.util.Integer;
import com.threerings.util.Iterator;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.StringUtil;

import com.threerings.presents.client.ConfirmAdapter;
import com.threerings.presents.client.InvocationService_ConfirmListener;

import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;
import com.threerings.presents.dobj.MessageAdapter;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.PropertySetEvent;
import com.threerings.ezgame.data.PropertySetListener;
import com.threerings.ezgame.data.UserCookie;
import com.threerings.ezgame.util.EZObjectMarshaller;

/**
 * Manages the backend of the game.
 */
public class GameControlBackend
    implements MessageListener, SetListener, PropertySetListener
{
    public var log :Log = Log.getLog(this);

    public function GameControlBackend (
        ctx :CrowdContext, ezObj :EZGameObject)
    {
        _ctx = ctx;
        _ezObj = ezObj;
        _gameData = new GameData(setProperty_v1, _ezObj.getUserProps());

        _ezObj.addListener(this);
        _ctx.getClient().getClientObject().addListener(_userListener);
    }

    public function setSharedEvents (disp :IEventDispatcher) :void
    {
        disp.addEventListener("ezgameQuery", handleEZQuery);
    }

    public function setContainer (container :GameContainer) :void
    {
        _container = container;
    }

    public function shutdown () :void
    {
        _ezObj.removeListener(this);
        _ctx.getClient().getClientObject().removeListener(_userListener);
    }

    protected function handleEZQuery (evt :Object) :void
    {
        setUserCodeProperties(evt.userProps);
        evt.ezProps = new Object();
        populateProperties(evt.ezProps);
    }

    protected function setUserCodeProperties (o :Object) :void
    {
        // here we would handle adapting old functions to a new version

        _ezDispatcher = (o["dispatchEvent_v1"] as Function);

        _userFuncs = o;
    }

    protected function callUserCode (name :String, ... args) :*
    {
        if (_userFuncs != null) {
            try {
                var func :Function = (_userFuncs[name] as Function);
                if (func != null) {
                    return func.apply(null, args);
                }

            } catch (err :Error) {
                log.warning("Error in user-code: " + err);
                log.logStackTrace(err);
            }
        }
        return undefined;
    }

    protected function populateProperties (o :Object) :void
    {
        // straight data
        o["gameData"] = _gameData;

        // functions
        o["setProperty_v1"] = setProperty_v1;
        o["mergeCollection_v1"] = mergeCollection_v1;
        o["sendMessage_v1"] = sendMessage_v1;
        o["setTicker_v1"] = setTicker_v1;
        o["sendChat_v1"] = sendChat_v1;
        o["localChat_v1"] = localChat_v1;
        o["getPlayerCount_v1"] = getPlayerCount_v1;
        o["getPlayerNames_v1"] = getPlayerNames_v1;
        o["getMyIndex_v1"] = getMyIndex_v1;
        o["getTurnHolderIndex_v1"] = getTurnHolderIndex_v1;
        o["getWinnerIndexes_v1"] = getWinnerIndexes_v1;
        o["getUserCookie_v1"] = getUserCookie_v1;
        o["setUserCookie_v1"] = setUserCookie_v1;
        o["isMyTurn_v1"] = isMyTurn_v1;
        o["isInPlay_v1"] = isInPlay_v1;
        o["endTurn_v1"] = endTurn_v1;
        o["endGame_v1"] = endGame_v1;
        o["populateCollection_v1"] = populateCollection_v1;
        o["getFromCollection_v1"] = getFromCollection_v1;
        o["alterKeyEvents_v1"] = alterKeyEvents_v1;
        o["focusContainer_v1"] = focusContainer_v1;
    }

    public function setProperty_v1 (
        propName :String, value :Object, index :int) :void
    {
        validatePropertyChange(propName, value, index);

        var encoded :Object = EZObjectMarshaller.encode(value, (index == -1));
        _ezObj.ezGameService.setProperty(
            _ctx.getClient(), propName, encoded, index,
            createLoggingListener("setProperty"));

        // set it immediately in the game object
        _ezObj.applyPropertySet(propName, value, index);
    }

    public function mergeCollection_v1 (
        srcColl :String, intoColl :String) :void
    {
        validateName(srcColl);
        validateName(intoColl);
        _ezObj.ezGameService.mergeCollection(_ctx.getClient(),
            srcColl, intoColl, createLoggingListener("mergeCollection"));
    }

    public function sendMessage_v1 (
        messageName :String, value :Object, playerIndex :int) :void
    {
        validateName(messageName);
        validateValue(value);

        var encoded :Object = EZObjectMarshaller.encode(value, false);
        _ezObj.ezGameService.sendMessage(_ctx.getClient(),
            messageName, encoded, playerIndex,
            createLoggingListener("sendMessage"));
    }

    public function setTicker_v1 (tickerName :String, msOfDelay :int) :void
    {
        validateName(tickerName);
        _ezObj.ezGameService.setTicker(_ctx.getClient(),
            tickerName, msOfDelay, createLoggingListener("setTicker"));
    }

    public function sendChat_v1 (msg :String) :void
    {
        validateChat(msg);
        // Post a message to the game object, the controller
        // will listen and call localChat().
        _ezObj.postMessage(EZGameObject.GAME_CHAT, [ msg ]);
    }

    public function localChat_v1 (msg :String) :void
    {
        validateChat(msg);
        // The sendChat() messages will end up being routed
        // through this method on each client.
        // TODO: make this look distinct from other system chat
        _ctx.getChatDirector().displayInfo(null, MessageBundle.taint(msg));
    }

    public function getPlayerCount_v1 () :int
    {
        if (_ezObj.players.length == 0) {
            // party game
            return _ezObj.occupants.size();

        } else {
            return _ezObj.getPlayerCount();
        }
    }

    public function getPlayerNames_v1 () :Array
    {
        var names :Array = new Array();
        if (_ezObj.players.length == 0) {
            // party game, count all occupants
            var itr :Iterator = _ezObj.occupantInfo.iterator();
            while (itr.hasNext()) {
                var occInfo :OccupantInfo = (itr.next() as OccupantInfo);
                names.push(occInfo.username.toString());
            }

        } else {
            for each (var name :Name in _ezObj.players) {
                names.push((name == null) ? null : name.toString());
            }
        }
        return names;
    }

    public function getMyIndex_v1 () :int
    {
        if (_ezObj.players.length == 0) {
            // party game
            // TODO: this shouldn't be based off of the String form of the name.
            var array :Array = getPlayerNames_v1();
            return array.indexOf(getUsername().toString());

        } else {
            return _ezObj.getPlayerIndex(getUsername());
        }
    }

    public function getTurnHolderIndex_v1 () :int
    {
        return _ezObj.getPlayerIndex(_ezObj.turnHolder);
    }

    public function getWinnerIndexes_v1 () :Array /* of int */
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

    public function getUserCookie_v1 (
        playerIndex :int, callback :Function) :void
    {
        // see if that cookie is already published
        if (_ezObj.userCookies != null) {
            var uc :UserCookie =
                (_ezObj.userCookies.get(playerIndex) as UserCookie);
            if (uc != null) {
                callback(uc.cookie);
                return;
            }
        }

        if (_cookieCallbacks == null) {
            _cookieCallbacks = new Dictionary();
        }
        var arr :Array = (_cookieCallbacks[playerIndex] as Array);
        if (arr == null) {
            arr = [];
            _cookieCallbacks[playerIndex] = arr;
        }
        arr.push(callback);

        // request it to be made so by the server
        _ezObj.ezGameService.getCookie(_ctx.getClient(), playerIndex,
            createLoggingListener("getUserCookie"));
    }

    public function setUserCookie_v1 (cookie :Object) :Boolean
    {
        var ba :ByteArray =
            (EZObjectMarshaller.encode(cookie, false) as ByteArray);
        if (ba.length > MAX_USER_COOKIE) {
            // not saved!
            return false;
        }

        _ezObj.ezGameService.setCookie(_ctx.getClient(), ba,
            createLoggingListener("setUserCookie"));
        return true;
    }

    public function isMyTurn_v1 () :Boolean
    {
        return getUsername().equals(_ezObj.turnHolder);
    }

    public function isInPlay_v1 () :Boolean
    {
        return _ezObj.isInPlay();
    }

    public function endTurn_v1 (nextPlayerIndex :int = -1) :void
    {
        _ezObj.ezGameService.endTurn(_ctx.getClient(), nextPlayerIndex,
            createLoggingListener("endTurn"));
    }

    public function endGame_v1 (... winnerDexes) :void
    {
        var winners :TypedArray = TypedArray.create(int);
        while (winnerDexes.length > 0) {
            winners.push(int(winnerDexes.shift()));
        }
        _ezObj.ezGameService.endGame(_ctx.getClient(), winners,
            createLoggingListener("endGame"));
    }

    /**
     * Helper method for setCollection and addToCollection.
     */
    public function populateCollection_v1 (
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
    public function getFromCollection_v1 (
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

    public function alterKeyEvents_v1 (
        keyEventType :String, add :Boolean) :void
    {
        if (add) {
            _container.addEventListener(keyEventType, handleKeyEvent);
        } else {
            _container.removeEventListener(keyEventType, handleKeyEvent);
        }
    }

    public function focusContainer_v1 () :void
    {
        _container.setFocus();
    }
    
    /**
     * Handle key events on our container and pass them into the game.
     */
    protected function handleKeyEvent (evt :KeyboardEvent) :void
    {
        // dispatch a cloned copy of the event, so that it's safe
        _ezDispatcher(evt.clone());
    }

    /**
     * Convenience function to get our name.
     */
    protected function getUsername () :Name
    {
        var body :BodyObject = 
            (_ctx.getClient().getClientObject() as BodyObject);
        return body.getVisibleName();
    }

    /**
     * Create a listener for service requests.
     */
    protected function createLoggingListener (
        service :String) :InvocationService_ConfirmListener
    {
        return new ConfirmAdapter(function (cause :String) :void {
            Log.getLog(this).warning("Service failure " +
                "[service=" + service + ", cause=" + cause + "].");
        });
    }

    /**
     * Verify that the property name / value are valid.
     */
    protected function validatePropertyChange (
        propName :String, value :Object, index :int) :void
    {
        validateName(propName);

        // check that we're setting an array element on an array
        if (index >= 0) {
            if (!(_gameData[propName] is Array)) {
                throw new ArgumentError("Property " + propName +
                    " is not an Array.");
            }

        } else if (index != -1) {
            throw new ArgumentError("Invalid index specified: " + index);
        }

        // validate the value too
        validateValue(value);
    }

    /**
     * Verify that the specified name is valid.
     */
    protected function validateName (name :String) :void
    {
        if (name == null) {
            throw new ArgumentError(
                "Property, message, and collection names must not be null.");
        }
    }

    protected function validateChat (msg :String) :void
    {
        if (StringUtil.isBlank(msg)) {
            throw new ArgumentError(
                "Empty chat may not be displayed.");
        }
    }

    /**
     * Verify that the value is legal to be streamed to other clients.
     */
    protected function validateValue (value :Object) :void
    {
        if (value == null) {
            return;

        } else if (value is IExternalizable) {
            throw new ArgumentError(
                "IExternalizable is not yet supported");

        } else if (value is Array) {
            if (ClassUtil.getClassName(value) != "Array") {
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
            var clazz :Class = ClassUtil.getClass(value);
            if (clazz == ByteArray) {
                return; // kosher
            }
            var clazzparentname :String = getQualifiedSuperclassName (clazz);
            var rootclass :Boolean = (clazzparentname == null);
            if (! rootclass) {
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

    /**
     * Called by the EZGameController when the turn changes.
     */
    public function turnDidChange () :void
    {
        callUserCode("turnDidChange_v1");
    }

    /**
     * Called by the EZGameController when the game starts.
     */
    public function gameDidStart () :void
    {
        callUserCode("gameDidStart_v1");
    }

    /**
     * Called by the EZGameController when the game ends.
     */
    public function gameDidEnd () :void
    {
        callUserCode("gameDidEnd_v1");
    }

    // from SetListener
    public function entryAdded (event :EntryAddedEvent) :void
    {
        if (EZGameObject.USER_COOKIES == event.getName()) {
            receivedUserCookie(event.getEntry() as UserCookie);
        }
    }

    // from SetListener
    public function entryUpdated (event :EntryUpdatedEvent) :void
    {
        if (EZGameObject.USER_COOKIES == event.getName()) {
            receivedUserCookie(event.getEntry() as UserCookie);
        }
    }

    // from SetListener
    public function entryRemoved (event :EntryRemovedEvent) :void
    {
        // nada
    }

    // from PropertySetListener
    public function propertyWasSet (event :PropertySetEvent) :void
    {
        callUserCode("propertyWasSet_v1", event.getName(), event.getValue(),
            event.getOldValue(), event.getIndex());
    }

    public function messageReceived (event :MessageEvent) :void
    {
        var name :String = event.getName();
        if (EZGameObject.USER_MESSAGE == name) {
            var args :Array = event.getArgs();
            callUserCode("messageReceived_v1", (args[0] as String),
                EZObjectMarshaller.decode(args[1]));

        } else if (EZGameObject.GAME_CHAT == name) {
            // this is chat send by the game, let's route it like
            // localChat, which is also sent by the game
            localChat_v1(String(event.getArgs()[0]));

        } else if (EZGameObject.TICKER == name) {
            var targs :Array = event.getArgs();
            callUserCode("messageReceived_v1", (targs[0] as String),
                (targs[1] as Integer).value);
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
            var args :Array = event.getArgs();
            callUserCode("messageReceived_v1", (args[0] as String),
                EZObjectMarshaller.decode(args[1]));
        }
    }


    /**
     * Handle the arrival of a new UserCookie.
     */
    protected function receivedUserCookie (cookie :UserCookie) :void
    {
        if (_cookieCallbacks != null) {
            var arr :Array = (_cookieCallbacks[cookie.playerIndex] as Array);
            if (arr != null) {
                delete _cookieCallbacks[cookie.playerIndex];
                for each (var fn :Function in arr) {
                    try {
                        fn(cookie.cookie);
                    } catch (err :Error) {
                        // cope
                    }
                }
            }
        }
    }

    protected var _ctx :CrowdContext;

    protected var _userListener :MessageAdapter =
        new MessageAdapter(messageReceivedOnUserObject);

    protected var _container :GameContainer;

    protected var _ezObj :EZGameObject;

    protected var _userFuncs :Object;

    /** The function on the EZGameControl which we can use to directly
     * dispatch events to the user's game. */
    protected var _ezDispatcher :Function;

    protected var _gameData :GameData;

    /** playerIndex -> callback functions waiting for the cookie. */
    protected var _cookieCallbacks :Dictionary;

    protected static const MAX_USER_COOKIE :int = 4096;
}
}
