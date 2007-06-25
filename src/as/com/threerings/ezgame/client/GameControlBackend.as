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

import com.threerings.util.ArrayUtil;
import com.threerings.util.ClassUtil;
import com.threerings.util.Integer;
import com.threerings.util.Iterator;
import com.threerings.util.MessageBundle;
import com.threerings.util.Name;
import com.threerings.util.StringUtil;
import com.threerings.util.Wrapped;

import com.threerings.presents.client.ConfirmAdapter;
import com.threerings.presents.client.ResultWrapper;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.client.InvocationService_ResultListener;

import com.threerings.presents.dobj.ElementUpdateListener;
import com.threerings.presents.dobj.ElementUpdatedEvent;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.MessageAdapter;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.dobj.MessageListener;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;
import com.threerings.presents.dobj.OidListListener;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.chat.client.ChatDisplay;
import com.threerings.crowd.chat.data.ChatCodes;
import com.threerings.crowd.chat.data.ChatMessage;
import com.threerings.crowd.chat.data.UserMessage;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.data.GameObject;

import com.threerings.ezgame.data.EZGameConfig;
import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.PropertySetEvent;
import com.threerings.ezgame.data.PropertySetListener;
import com.threerings.ezgame.data.UserCookie;
import com.threerings.ezgame.util.EZObjectMarshaller;

/**
 * Manages the backend of the game.
 */
public class GameControlBackend
    implements MessageListener, SetListener, ElementUpdateListener,
               PropertySetListener, ChatDisplay
{
    public var log :Log = Log.getLog(this);

    public function GameControlBackend (
        ctx :CrowdContext, ezObj :EZGameObject, ctrl :EZGameController)
    {
        _ctx = ctx;
        _ezObj = ezObj;
        _ctrl = ctrl;
        _gameData = _ezObj.getUserProps();

        _ezObj.addListener(this);
        _ctx.getClient().getClientObject().addListener(_userListener);
        _ctx.getChatDirector().addChatDisplay(this);
    }

    public function setSharedEvents (disp :IEventDispatcher) :void
    {
        disp.addEventListener("ezgameQuery", handleEZQuery);
    }

    /**
     * Are we connected to the usercode on the front-end?
     */
    public function isConnected () :Boolean
    {
        return (_userFuncs != null);
    }

    public function setContainer (container :GameContainer) :void
    {
        _container = container;
    }

    public function shutdown () :void
    {
        _ezObj.removeListener(this);
        _ctx.getClient().getClientObject().removeListener(_userListener);

        _userFuncs = null; // disconnect
    }

    protected function handleEZQuery (evt :Object) :void
    {
        setUserCodeProperties(evt.userProps);
        evt.ezProps = new Object();
        populateProperties(evt.ezProps);

        // ok, we're now hooked-up with the game code
        _ctrl.userCodeIsConnected(evt.userProps["autoReady_v1"]);
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
        // add in any backwards-compatible functions
        new GameControlCompatibility(_ezObj, this).populateProperties(o);

        // straight data
        o["gameData"] = _gameData;

        // convert our game config from a HashMap to a Dictionary
        var gameConfig :Object = {};
        var cfg :EZGameConfig = (_ctrl.getPlaceConfig() as EZGameConfig);
        cfg.params.forEach(function (key :Object, value :Object) :void {
            gameConfig[key] = (value is Wrapped) ? Wrapped(value).unwrap() : value;
        });
        o["gameConfig"] = gameConfig;

        // functions
        o["playerReady_v1"] = playerReady_v1;
        o["setProperty_v1"] = setProperty_v1;
        o["testAndSetProperty_v1"] = testAndSetProperty_v1;
        o["mergeCollection_v1"] = mergeCollection_v1;
        o["setTicker_v1"] = setTicker_v1;
        o["sendChat_v1"] = sendChat_v1;
        o["localChat_v1"] = localChat_v1;
        o["setUserCookie_v1"] = setUserCookie_v1;
        o["isMyTurn_v1"] = isMyTurn_v1;
        o["isInPlay_v1"] = isInPlay_v1;
        o["getDictionaryLetterSet_v1"] = getDictionaryLetterSet_v1;
        o["checkDictionaryWord_v1"] = checkDictionaryWord_v1;
        o["populateCollection_v1"] = populateCollection_v1;
        o["alterKeyEvents_v1"] = alterKeyEvents_v1;
        o["focusContainer_v1"] = focusContainer_v1;

        // newest
        o["getFromCollection_v2"] = getFromCollection_v2;
        o["sendMessage_v2"] = sendMessage_v2;
        o["getOccupants_v1"] = getOccupants_v1;
        o["getMyId_v1"] = getMyId_v1;
        o["getControllerId_v1"] = getControllerId_v1;
        o["getUserCookie_v2"] = getUserCookie_v2;
        o["endTurn_v2"] = endTurn_v2;
        o["endRound_v1"] = endRound_v1;
        o["endGame_v2"] = endGame_v2;
        o["getTurnHolder_v1"] = getTurnHolder_v1;
        o["getRound_v1"] = getRound_v1;
        o["getOccupantName_v1"] = getOccupantName_v1;
        o["getPlayers_v1"] = getPlayers_v1;
        o["getPlayerPosition_v1"] = getPlayerPosition_v1;
        o["getMyPosition_v1"] = getMyPosition_v1;

        o["startTransaction"] = startTransaction_v1;
        o["commitTransaction"] = commitTransaction_v1;
    }

    /**
     * Called by the client code when it is ready for the game to be started.
     */
    public function playerReady_v1 () :void
    {
        _ctrl.playerIsReady();
    }

    /**
     * Sets a property.
     *
     * Note: immediate defaults to true, even though immediate=false is the general case. We are
     * providing some backwards compatibility to old versions of setProperty_v1() that assumed
     * immediate and did not pass a 4th value.  All callers should now specify that value
     * explicitly.
     */
    public function setProperty_v1 (
        propName :String, value :Object, index :int, immediate :Boolean = true) :void
    {
        validateConnected();
        validatePropertyChange(propName, value, index);

        var encoded :Object = EZObjectMarshaller.encode(value, (index == -1));
        _ezObj.ezGameService.setProperty(
            _ctx.getClient(), propName, encoded, index,
            false, null, createLoggingConfirmListener("setProperty"));
        if (immediate) {
            _ezObj.applyPropertySet(propName, value, index);
        }
    }

    public function testAndSetProperty_v1 (
        propName :String, value :Object, testValue :Object, index :int) :void
    {
        validateConnected();
        validatePropertyChange(propName, value, index);

        var encodedValue :Object = EZObjectMarshaller.encode(value, (index == -1));
        var encodedTestValue :Object = EZObjectMarshaller.encode(testValue, (index == -1));
        _ezObj.ezGameService.setProperty(
            _ctx.getClient(), propName, encodedValue, index,
            true, encodedTestValue, createLoggingConfirmListener("setProperty"));
    }
        

    public function mergeCollection_v1 (
        srcColl :String, intoColl :String) :void
    {
        validateConnected();
        validateName(srcColl);
        validateName(intoColl);
        _ezObj.ezGameService.mergeCollection(_ctx.getClient(),
            srcColl, intoColl, createLoggingConfirmListener("mergeCollection"));
    }

    public function sendMessage_v2 (
        messageName :String, value :Object, playerId :int) :void
    {
        validateConnected();
        validateName(messageName);
        validateValue(value);

        var encoded :Object = EZObjectMarshaller.encode(value, false);
        _ezObj.ezGameService.sendMessage(_ctx.getClient(),
            messageName, encoded, playerId,
            createLoggingConfirmListener("sendMessage"));
    }

    public function setTicker_v1 (tickerName :String, msOfDelay :int) :void
    {
        validateConnected();
        validateName(tickerName);
        _ezObj.ezGameService.setTicker(_ctx.getClient(),
            tickerName, msOfDelay, createLoggingConfirmListener("setTicker"));
    }

    public function sendChat_v1 (msg :String) :void
    {
        validateConnected();
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

    public function getOccupants_v1 () :Array
    {
        validateConnected();
        var occs :Array = [];
        for (var ii :int = _ezObj.occupants.size() - 1; ii >= 0; ii--) {
            occs.push(_ezObj.occupants.get(ii));
        }

        return occs;
    }

    public function getPlayers_v1 () :Array
    {
        validateConnected();
        if (_ezObj.players.length == 0) {
            // party game
            return getOccupants_v1();
        }
        var playerIds :Array = [];
        for (var ii :int = 0; ii < _ezObj.players.length; ii++) {
            var occInfo :OccupantInfo = _ezObj.getOccupantInfo(_ezObj.players[ii] as Name);
            playerIds.push((occInfo == null) ? 0 : occInfo.bodyOid);
        }
        return playerIds;
    }

    public function getOccupantName_v1 (playerId :int) :String
    {
        validateConnected();
        var occInfo :OccupantInfo =
            (_ezObj.occupantInfo.get(playerId) as OccupantInfo);
        return (occInfo == null) ? null : occInfo.username.toString();
    }

    public function getMyId_v1 () :int
    {
        validateConnected();
        return _ctx.getClient().getClientObject().getOid();
    }

    public function getControllerId_v1 () :int
    {
        validateConnected();
        return _ezObj.controllerOid;
    }

    // TODO: table games only
    public function getPlayerPosition_v1 (playerId :int) :int
    {
        validateConnected();
        var occInfo :OccupantInfo =
            (_ezObj.occupantInfo.get(playerId) as OccupantInfo);
        if (occInfo == null) {
            return -1;
        }
        return _ezObj.getPlayerIndex(occInfo.username);
    }

    // TODO: table only
    public function getMyPosition_v1 () :int
    {
        validateConnected();
        return _ezObj.getPlayerIndex(
            (_ctx.getClient().getClientObject() as BodyObject).getVisibleName());
    }

    // TODO: table only
    public function getTurnHolder_v1 () :int
    {
        validateConnected();
        var occInfo :OccupantInfo = _ezObj.getOccupantInfo(_ezObj.turnHolder);
        return (occInfo == null) ? 0 : occInfo.bodyOid;
    }

    public function getRound_v1 () :int
    {
        validateConnected();
        return _ezObj.roundId;
    }

    public function getUserCookie_v2 (
        playerId :int, callback :Function) :void
    {
        validateConnected();
        // see if that cookie is already published
        if (_ezObj.userCookies != null) {
            var uc :UserCookie =
                (_ezObj.userCookies.get(playerId) as UserCookie);
            if (uc != null) {
                callback(EZObjectMarshaller.decode(uc.cookie));
                return;
            }
        }

        if (_cookieCallbacks == null) {
            _cookieCallbacks = new Dictionary();
        }
        var arr :Array = (_cookieCallbacks[playerId] as Array);
        if (arr == null) {
            arr = [];
            _cookieCallbacks[playerId] = arr;
        }
        arr.push(callback);

        // request it to be made so by the server
        _ezObj.ezGameService.getCookie(_ctx.getClient(), playerId,
            createLoggingConfirmListener("getUserCookie"));
    }

    public function setUserCookie_v1 (cookie :Object) :Boolean
    {
        validateConnected();
        var ba :ByteArray =
            (EZObjectMarshaller.encode(cookie, false) as ByteArray);
        if (ba.length > MAX_USER_COOKIE) {
            // not saved!
            return false;
        }

        _ezObj.ezGameService.setCookie(_ctx.getClient(), ba,
            createLoggingConfirmListener("setUserCookie"));
        return true;
    }

    public function isMyTurn_v1 () :Boolean
    {
        validateConnected();
        return getUsername().equals(_ezObj.turnHolder);
    }

    public function isInPlay_v1 () :Boolean
    {
        validateConnected();
        return _ezObj.isInPlay();
    }

    public function endTurn_v2 (nextPlayerId :int) :void
    {
        validateConnected();
        _ezObj.ezGameService.endTurn(
            _ctx.getClient(), nextPlayerId, createLoggingConfirmListener("endTurn"));
    }

    public function endRound_v1 (nextRoundDelay :int) :void
    {
        validateConnected();
        _ezObj.ezGameService.endRound(
            _ctx.getClient(), nextRoundDelay, createLoggingConfirmListener("endRound"));
    }

    public function endGame_v2 (... winnerIds) :void
    {
        validateConnected();
        var winners :TypedArray = TypedArray.create(int);
        while (winnerIds.length > 0) {
            winners.push(int(winnerIds.shift()));
        }
        _ezObj.ezGameService.endGame(
            _ctx.getClient(), winners, createLoggingConfirmListener("endGame"));
    }

    public function getDictionaryLetterSet_v1 (locale :String, count :int, callback :Function) :void
    {
        validateConnected();
        var listener :InvocationService_ResultListener;
        if (callback != null) {
            var failure :Function = function (cause :String = null) :void {
                // ignore the cause, return an empty array
                callback ([]);
            }
            var success :Function = function (result :String = null) :void {
                // splice the resulting string, and return as array
                var r : Array = result.split(",");
                callback (r);
            };
            listener = new ResultWrapper (failure, success);
        } else {
            listener = createLoggingResultListener ("checkDictionaryWord");
        }
        
        // just relay the data over to the server
        _ezObj.ezGameService.getDictionaryLetterSet(_ctx.getClient(), locale, count, listener);
    }

    public function checkDictionaryWord_v1 (
        locale :String, word :String, callback :Function) :void
    {
        validateConnected();
        var listener :InvocationService_ResultListener;
        if (callback != null) {
            var failure :Function = function (cause :String = null) :void {
                // ignore the cause, return failure
                callback (word, false);
            }
            var success :Function = function (result :Object = null) :void {
                // server returns a boolean, so convert it and send it over
                var r : Boolean = Boolean(result);
                callback (word, r);
            };
            listener = new ResultWrapper (failure, success);
        } else {
            listener = createLoggingResultListener ("checkDictionaryWord");
        }

        // just relay the data over to the server
        _ezObj.ezGameService.checkDictionaryWord(_ctx.getClient(), locale, word, listener);

    }

    /**
     * Helper method for setCollection and addToCollection.
     */
    public function populateCollection_v1 (
        collName :String, values :Array, clearExisting :Boolean) :void
    {
        validateConnected();
        validateName(collName);
        if (values == null) {
            throw new ArgumentError("Collection values may not be null.");
        }
        validateValue(values);

        var encodedValues :TypedArray =
            (EZObjectMarshaller.encode(values) as TypedArray);

        _ezObj.ezGameService.addToCollection(
            _ctx.getClient(), collName, encodedValues, clearExisting,
            createLoggingConfirmListener("populateCollection"));
    }

    /**
     * Helper method for pickFromCollection and dealFromCollection.
     */
    public function getFromCollection_v2 (
        collName :String, count :int, msgOrPropName :String, playerId :int,
        consume :Boolean, callback :Function) :void
    {
        validateConnected();
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
            listener = createLoggingConfirmListener("getFromCollection");
        }

        _ezObj.ezGameService.getFromCollection(
            _ctx.getClient(), collName, consume, count, msgOrPropName,
            playerId, listener);
    }

    public function alterKeyEvents_v1 (
        keyEventType :String, add :Boolean) :void
    {
        validateConnected();
        if (add) {
            _container.addEventListener(keyEventType, handleKeyEvent);
        } else {
            _container.removeEventListener(keyEventType, handleKeyEvent);
        }
    }

    public function focusContainer_v1 () :void
    {
        validateConnected();
        _container.setFocus();
    }

    /**
     * Start a dobj transaction on the game object.
     */
    public function startTransaction_v1 () :void
    {
        validateConnected();
        _ezObj.startTransaction();
    }

    /**
     * Commit a dobj transaction on the game object.
     */
    public function commitTransaction_v1 () :void
    {
        _ezObj.commitTransaction();
    }

    /**
     * Convenience function to get our name.
     */
    public function getUsername () :Name
    {
        var body :BodyObject = 
            (_ctx.getClient().getClientObject() as BodyObject);
        return body.getVisibleName();
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
     * Create a logging confirm listener for service requests.
     */
    protected function createLoggingConfirmListener (
        service :String) :InvocationService_ConfirmListener
    {
        return new ConfirmAdapter(function (cause :String) :void {
            Log.getLog(this).warning("Service failure " +
                "[service=" + service + ", cause=" + cause + "].");
        });
    }

    /**
     * Create a logging result listener for service requests.
     */
    protected function createLoggingResultListener (
        service :String) :InvocationService_ResultListener
    {
        return new ResultWrapper(function (cause :String) :void {
            Log.getLog(this).warning("Service failure " +
                "[service=" + service + ", cause=" + cause + "].");
        });
    }

    /**
     * Validate that we're not shutdown.
     */
    public function validateConnected () :void
    {
        if (_userFuncs == null) {
            throw new Error("Not connected.");
        }
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
            if (value is ByteArray) {
                return; // kosher
            }
            var clazz :Class = ClassUtil.getClass(value);
            var clazzparentname :String = getQualifiedSuperclassName(clazz);
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
     * Called by the EZGameController when the controller changes.
     */
    public function controlDidChange () :void
    {
        callUserCode("controlDidChange_v1");
    }

    /**
     * Called by the EZGameController when the turn changes.
     */
    public function turnDidChange () :void
    {
        callUserCode("turnDidChange_v1");
    }

    /**
     * Called by the EZGameController when the game starts or ends.
     */
    public function gameStateChanged (started :Boolean) :void
    {
        if (started && _userFuncs["gameDidStart_v1"] != null) {
            callUserCode("gameDidStart_v1"); // backwards compatibility
        } else if (!started && _userFuncs["gameDidEnd_v1"] != null) {
            callUserCode("gameDidEnd_v1"); // backwards compatibility
        } else {
            callUserCode("gameStateChanged_v1", started); // new hotness
        }
    }

    /**
     * Called by the EZGameController when a round starts or ends.
     */
    public function roundStateChanged (started :Boolean) :void
    {
        callUserCode("roundStateChanged_v1", started);
    }

    // from SetListener
    public function entryAdded (event :EntryAddedEvent) :void
    {
        var name :String = event.getName();
        switch (name) {
        case EZGameObject.USER_COOKIES:
            receivedUserCookie(event.getEntry() as UserCookie);
            break;

        case PlaceObject.OCCUPANT_INFO:
            var occInfo :OccupantInfo = (event.getEntry() as OccupantInfo)
            callUserCode("occupantChanged_v1", occInfo.bodyOid, isPlayer(occInfo.username), true);
            break;
        }
    }

    // from SetListener
    public function entryUpdated (event :EntryUpdatedEvent) :void
    {
        var name :String = event.getName();
        switch (name) {
        case EZGameObject.USER_COOKIES:
            receivedUserCookie(event.getEntry() as UserCookie);
            break;
        }
    }

    // from SetListener
    public function entryRemoved (event :EntryRemovedEvent) :void
    {
        var name :String = event.getName();
        switch (name) {
        case PlaceObject.OCCUPANT_INFO:
            var occInfo :OccupantInfo = (event.getOldEntry() as OccupantInfo)
            callUserCode("occupantChanged_v1", occInfo.bodyOid, isPlayer(occInfo.username), false);
            break;
        }
    }

    // from ElementUpdateListener
    public function elementUpdated (event :ElementUpdatedEvent) :void
    {
        var name :String = event.getName();
        if (name == GameObject.PLAYERS) {
            var oldPlayer :Name = (event.getOldValue() as Name);
            var newPlayer :Name = (event.getValue() as Name);
            var occInfo :OccupantInfo;
            if (oldPlayer != null) {
                occInfo = _ezObj.getOccupantInfo(oldPlayer);
                if (occInfo != null) {
                    // old player became a watcher
                    // send player-left, then occupant-added
                    callUserCode("occupantChanged_v1", occInfo.bodyOid, true, false);
                    callUserCode("occupantChanged_v1", occInfo.bodyOid, false, true);
                }
            }
            if (newPlayer != null) {
                occInfo = _ezObj.getOccupantInfo(newPlayer);
                if (occInfo != null) {
                    // watcher became a player
                    // send occupant-left, then player-added
                    callUserCode("occupantChanged_v1", occInfo.bodyOid, false, false);
                    callUserCode("occupantChanged_v1", occInfo.bodyOid, true, true);
                }
            }
        }
    }

    // from PropertySetListener
    public function propertyWasSet (event :PropertySetEvent) :void
    {
        callUserCode("propertyWasSet_v1", event.getName(), event.getValue(),
            event.getOldValue(), event.getIndex());
    }

    // from ChatDisplay
    public function clear () :void
    {
    }

    // from ChatDisplay
    public function displayMessage (msg :ChatMessage, alreadyDisplayed :Boolean) :Boolean
    {
        if (msg is UserMessage && msg.localtype == ChatCodes.PLACE_CHAT_TYPE) {
            var info :OccupantInfo = _ezObj.getOccupantInfo((msg as UserMessage).speaker);
            if (info != null) {
                callUserCode("userChat_v1", info.bodyOid, msg.message);
            }
        }
        return true;
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
            callUserCode("messageReceived_v1", (targs[0] as String), (targs[1] as int));
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
            var arr :Array = (_cookieCallbacks[cookie.playerId] as Array);
            if (arr != null) {
                delete _cookieCallbacks[cookie.playerId];
                for each (var fn :Function in arr) {
                    try {
                        fn(EZObjectMarshaller.decode(cookie.cookie));
                    } catch (err :Error) {
                        // cope
                    }
                }
            }
        }
    }

    /**
     * Given the specified occupant name, return if they are a player.
     */
    protected function isPlayer (occupantName :Name) :Boolean
    {
        if (_ezObj.players.length == 0) {
            return true; // party game: all occupants are players
        }
        return (-1 != _ezObj.getPlayerIndex(occupantName));
    }

    protected var _ctx :CrowdContext;

    protected var _userListener :MessageAdapter =
        new MessageAdapter(messageReceivedOnUserObject);

    protected var _container :GameContainer;

    protected var _ezObj :EZGameObject;

    /** Handles trusted clientside control. */
    protected var _ctrl :EZGameController;

    protected var _userFuncs :Object;

    /** The function on the EZGameControl which we can use to directly
     * dispatch events to the user's game. */
    protected var _ezDispatcher :Function;

    protected var _gameData :Object;

    /** playerIndex -> callback functions waiting for the cookie. */
    protected var _cookieCallbacks :Dictionary;

    protected static const MAX_USER_COOKIE :int = 4096;
}
}
