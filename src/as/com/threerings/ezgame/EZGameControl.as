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

package com.threerings.ezgame {

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;

import flash.display.DisplayObject;

/**
 * Dispatched when a key is pressed when the game has focus.
 *
 * @eventType flash.events.KeyboardEvent.KEY_DOWN
 */
[Event(name="keyDown", type="flash.events.KeyboardEvent")]

/**
 * Dispatched when a key is released when the game has focus.
 *
 * @eventType flash.events.KeyboardEvent.KEY_UP
 */
[Event(name="keyUp", type="flash.events.KeyboardEvent")]

/**
 * Dispatched when the controller changes for the game.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.CONTROL_CHANGED
 */
[Event(name="ControlChanged", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the game starts, usually after all players are present.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.GAME_STARTED
 */
[Event(name="GameStarted", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when a round starts.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.ROUND_STARTED
 */
[Event(name="RoundStarted", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the turn changes in a turn-based game.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.TURN_CHANGED
 */
[Event(name="TurnChanged", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when a round ends.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.ROUND_ENDED
 */
[Event(name="RoundEnded", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the game ends.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.GAME_ENDED
 */
[Event(name="GameEnded", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when a property has changed in the shared game state.
 *
 * @eventType com.threerings.ezgame.PropertyChangedEvent.TYPE
 */
[Event(name="PropChanged", type="com.threerings.ezgame.PropertyChangedEvent")]

/**
 * Dispatched when a message arrives with information that is not part of the shared game state.
 *
 * @eventType com.threerings.ezgame.MessageReceivedEvent.TYPE
 */
[Event(name="msgReceived", type="com.threerings.ezgame.MessageReceivedEvent")]

/**
 * Dispatched when a user chats.
 *
 * @eventType com.threerings.ezgame.UserChatEvent.TYPE
 */
[Event(name="UserChat", type="com.threerings.ezgame.UserChatEvent")]

/**
 * The single point of control for each client in your multiplayer EZGame.
 *
 * TODO: lots of documentation.
 */
public class EZGameControl extends BaseControl
{
    /**
     * Create an EZGameControl object using some display object currently on the hierarchy.
     *
     * @param disp the display object that is the game's UI.
     * @param autoReady if true, the game will automatically be started when initialization is
     * complete, if false, the game will not start until all clients call {@link #playerReady}.
     */
    public function EZGameControl (disp :DisplayObject, autoReady :Boolean)
    {
        var event :DynEvent = new DynEvent();
        event.userProps = new Object();
        populateProperties(event.userProps);
        event.userProps["autoReady_v1"] = autoReady;
        disp.root.loaderInfo.sharedEvents.dispatchEvent(event);
        if ("ezProps" in event) {
            setEZProps(event.ezProps);
        }

        // set up our focusing click handler
        disp.root.addEventListener(MouseEvent.CLICK, handleRootClick);

        // TODO: this should only be available if the game uses it
        _seating = new SeatingControl(this);
    }

    // documentation inherited
    override public function addEventListener (
        type :String, listener :Function, useCapture :Boolean = false,
        priority :int = 0, useWeakReference :Boolean = false) :void
    {
        super.addEventListener(type, listener, useCapture, priority, useWeakReference);

        switch (type) {
        case KeyboardEvent.KEY_UP:
        case KeyboardEvent.KEY_DOWN:
            if (hasEventListener(type)) { // ensure it was added
                callEZCode("alterKeyEvents_v1", type, true);
            }
            break;
        }
    }

    // documentation inherited
    override public function removeEventListener (
        type :String, listener :Function, useCapture :Boolean = false) :void
    {
        super.removeEventListener(type, listener, useCapture);

        switch (type) {
        case KeyboardEvent.KEY_UP:
        case KeyboardEvent.KEY_DOWN:
            if (!hasEventListener(type)) { // once it's no longer needed
                callEZCode("alterKeyEvents_v1", type, false);
            }
            break;
        }
    }

    /**
     * Are we connected and running inside the EZGame environment, or has someone just loaded up
     * this swf by itself?
     */
    public function isConnected () :Boolean
    {
        return (_gameData != null);
    }

    /**
     * Get any game-specific configurations that were set up in the lobby.
     */
    public function getConfig () :Object
    {
        return _gameConfig;
    }

    /**
     * Get the CollectionsControl, which contains methods for utilizing the server to dispatch
     * private information.
     */
    public function get collections () :CollectionsControl
    {
        if (_collections == null) {
            _collections = new CollectionsControl(this);
        }
        return _collections;
    }

    /**
     * Get the SeatingControl, which contains methods for checking and assigning player seating
     * positions.
     */
    public function get seating () :SeatingControl
    {
        return _seating;
    }

    /**
     * Get a property from data.
     */
    public function get (propName :String, index :int = -1) :Object
    {
        checkIsConnected();

        var value :Object = _gameData[propName];
        if (index >= 0) {
            if (value is Array) {
                return (value as Array)[index];

            } else {
                throw new ArgumentError("Property " + propName + " is not an array.");
            }
        }
        return value;
    }

    /**
     * Set a property that will be distributed. 
     */
    public function set (propName :String, value :Object, index :int = -1) :void
    {
        callEZCode("setProperty_v1", propName, value, index, false);
    }

    /**
     * Set a property, but have this client immediately set the value so that it can be
     * re-read. The property change event will still arrive and will be your clue as to when the
     * other clients will see the newly set value. Be careful with this method, as it can allow
     * data inconsistency: two clients may see different values for a property if one of them
     * recently set it immediately, and the resultant PropertyChangedEvent's oldValue also may not
     * be consistent.
     */
    public function setImmediate (propName :String, value :Object, index :int = -1) :void
    {
        callEZCode("setProperty_v1", propName, value, index, true);
    }

    /**
     * Set a property that will be distributed, but only if it's equal to the specified test value.
     *
     * <p> Please note that, unlike in the standard set() function, the property will not be
     * updated right away, but will require a request to the server and a response back. For this
     * reason, there may be a considerable delay between calling testAndSet, and seeing the result
     * of the update.
     *
     * <p> The operation is 'atomic', in the sense that testing and setting take place during the
     * same server event. In comparison, a separate 'get' followed by a 'set' operation would
     * involve two events with two network round-trips, and no guarantee that the value won't
     * change between the events.
     */
    public function testAndSet (
        propName :String, newValue :Object, testValue :Object, index :int = -1) :void
    {
        callEZCode("testAndSetProperty_v1", propName, newValue, testValue, index);
    }

    /**
     * Execute the specified function as a batch of commands that will be sent to the server
     * together. This is no different from executing the commands outside of a batch, but
     * may result in better use of the network and should be used if setting a number of things
     * at once.
     *
     * Example:
     * _ctrl.doBatch(function () :void {
     *     _ctrl.set("board", new Array());
     *     _ctrl.set("scores", new Array());
     *     _ctrl.set("captures", 0);
     * });
     */
    public function doBatch (fn :Function) :void
    {
        callEZCode("startTransaction");
        try {
            fn();
        } finally {
            callEZCode("commitTransaction");
        }
    }

    /**
     * Get the names of all currently-set properties that begin with the specified prefix.
     */
    public function getPropertyNames (prefix :String = "") :Array
    {
        var props :Array = [];
        for (var s :String in _gameData) {
            if (s.lastIndexOf(prefix, 0) == 0) {
                props.push(s);
            }
        }
        return props;
    }

    /**
     * Register an object to receive whatever events it should receive, based on which event
     * listeners it implements.
     */
    public function registerListener (obj :Object) :void
    {
        if (obj is MessageReceivedListener) {
            var mrl :MessageReceivedListener = (obj as MessageReceivedListener);
            addEventListener(MessageReceivedEvent.TYPE, mrl.messageReceived, false, 0, true);
        }
        if (obj is PropertyChangedListener) {
            var pcl :PropertyChangedListener = (obj as PropertyChangedListener);
            addEventListener(PropertyChangedEvent.TYPE, pcl.propertyChanged, false, 0, true);
        }
        if (obj is StateChangedListener) {
            var scl :StateChangedListener = (obj as StateChangedListener);
            addEventListener(StateChangedEvent.CONTROL_CHANGED, scl.stateChanged, false, 0, true);
            addEventListener(StateChangedEvent.GAME_STARTED, scl.stateChanged, false, 0, true);
            addEventListener(StateChangedEvent.ROUND_STARTED, scl.stateChanged, false, 0, true);
            addEventListener(StateChangedEvent.TURN_CHANGED, scl.stateChanged, false, 0, true);
            addEventListener(StateChangedEvent.ROUND_ENDED, scl.stateChanged, false, 0, true);
            addEventListener(StateChangedEvent.GAME_ENDED, scl.stateChanged, false, 0, true);
        }
        if (obj is OccupantChangedListener) {
            var ocl :OccupantChangedListener = (obj as OccupantChangedListener);
            addEventListener(OccupantChangedEvent.OCCUPANT_ENTERED, ocl.occupantEntered,
                             false, 0, true);
            addEventListener(OccupantChangedEvent.OCCUPANT_LEFT, ocl.occupantLeft, false, 0, true);
        }
    }

    /**
     * Unregister the specified object from receiving events.
     */
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
            removeEventListener(StateChangedEvent.GAME_STARTED, scl.stateChanged);
            removeEventListener(StateChangedEvent.ROUND_STARTED, scl.stateChanged);
            removeEventListener(StateChangedEvent.TURN_CHANGED, scl.stateChanged);
            removeEventListener(StateChangedEvent.ROUND_ENDED, scl.stateChanged);
            removeEventListener(StateChangedEvent.GAME_ENDED, scl.stateChanged);
        }
        if (obj is OccupantChangedListener) {
            var ocl :OccupantChangedListener = (obj as OccupantChangedListener);
            removeEventListener(OccupantChangedEvent.OCCUPANT_ENTERED, ocl.occupantEntered);
            removeEventListener(OccupantChangedEvent.OCCUPANT_LEFT, ocl.occupantLeft);
        }
    }

    /**
     * If the game was not configured to auto-start, all clients must call this function to let the
     * server know that they are ready, at which point the game will be started. Once a game is
     * over, all clients can call this function again to start a new game.
     */
    public function playerReady () :void
    {
        callEZCode("playerReady_v1");
    }

    /**
     * Requests a set of random letters from the dictionary service.  The letters will arrive in a
     * separate message with the specified key, as an array of strings.
     *
     * @param locale RFC 3066 string that represents language settings
     * @param count the number of letters to be produced
     * @param callback the function that will process the results, of the form:
     * <pre>function (letters :Array) :void</pre>
     * where letters is an array of strings containing letters for the given language settings
     * (potentially empty).
     */
    public function getDictionaryLetterSet (locale :String, count :int, callback :Function) :void
    {
        callEZCode("getDictionaryLetterSet_v1", locale, count, callback);
    }

    /**
     * Requests a check to see if the dictionary for the given locale contains the given word.
     *
     * @param RFC 3066 string that represents language settings
     * @param word the string contains the word to be checked
     * @param callback the function that will process the results, of the form:
     * <pre>function (word :String, result :Boolean) :void</pre>
     * where word is a copy of the word that was requested, and result specifies whether the word
     * is valid given language settings
     */
    public function checkDictionaryWord (locale :String, word :String, callback :Function) :void
    {
        callEZCode("checkDictionaryWord_v1", locale, word, callback);
    }

    /**
     * Send a "message" to other clients subscribed to the game.  These is similar to setting a
     * property, except that the value will not be saved- it will merely end up coming out as a
     * MessageReceivedEvent.
     *
     * @param playerId if 0 (or unset), sends to all players, otherwise the message will be private
     * to just one player
     */
    public function sendMessage (messageName :String, value :Object, playerId :int = 0) :void
    {
        callEZCode("sendMessage_v2", messageName, value, playerId);
    }

    /**
     * Start the ticker with the specified name. It will deliver messages to the game object at the
     * specified delay, the value of each message being a single integer, starting with 0 and
     * increasing by one with each messsage.
     */
    public function startTicker (tickerName :String, msOfDelay :int) :void
    {
        callEZCode("setTicker_v1", tickerName, msOfDelay);
    }

    /**
     * Stop the specified ticker.
     */
    public function stopTicker (tickerName :String) :void
    {
        startTicker(tickerName, 0);
    }

    /**
     * Send a message that will be heard by everyone in the game room, even observers.
     */
    public function sendChat (msg :String) :void
    {
        callEZCode("sendChat_v1", msg);
    }

    /**
     * Display the specified message immediately locally: not sent to any other players or
     * observers in the game room.
     */
    public function localChat (msg :String) :void
    {
        callEZCode("localChat_v1", msg);
    }

    /**
     * Run the specified text through the user's chat filter. This is not necessary for sendChat,
     * but may be desirable for other user-entered text.
     *
     * @return the filtered text, or null if it was so bad it's gone.
     */
    public function filter (text :String) :String
    {
        return (callEZCode("filter_v1", text) as String);
    }

    /**
     * Returns the player ids of all occupants in the game room.
     */
    public function getOccupantIds () :Array /* of playerId */
    {
        return (callEZCode("getOccupants_v1") as Array);
    }

    /**
     * Get the display name of the specified occupant.  Two players may have the same name: always
     * use playerId to purposes of identification and comparison. The name is for display
     * only. Will be null is the specified playerId is not in the game.
     */
    public function getOccupantName (playerId :int) :String
    {
        return String(callEZCode("getOccupantName_v1", playerId));
    }

    /**
     * Returns this client's player id.
     */
    public function getMyId () :int
    {
        return int(callEZCode("getMyId_v1"));
    }

    /**
     * Returns true if we are in control of this game. False if another client is in control.
     */
    public function amInControl () :Boolean
    {
        return getControllerId() == getMyId();
    }

    /**
     * Returns the player id of the client that is in control of this game.
     */
    public function getControllerId () :int
    {
        return int(callEZCode("getControllerId_v1"));
    }

    /**
     * Returns the player id of the current turn holder, or 0 if it's nobody's turn.
     */
    public function getTurnHolder () :int
    {
        return int(callEZCode("getTurnHolder_v1"));
    }

    /**
     * Returns the current round number. Rounds start at 1 and increase if the game calls {@link
     * #endRound} with a next round timeout. Between rounds, it returns a negative number,
     * corresponding to the negation of the round that just ended.
     */
    public function getRound () :int
    {
        return int(callEZCode("getRound_v1"));
    }

    /**
     * Get the user-specific game data for the specified user. The first time this is requested per
     * game instance it will be retrieved from the database. After that, it will be returned from
     * memory.
     */
    public function getUserCookie (occupantId :int, callback :Function) :void
    {
        callEZCode("getUserCookie_v2", occupantId, callback);
    }

    /**
     * Store persistent data that can later be retrieved by an instance of this game. The maximum
     * size of this data is 4096 bytes AFTER AMF3 encoding.  Note: there is no playerId parameter
     * because a cookie may only be stored for the current player.
     *
     * @return false if the cookie could not be encoded to 4096 bytes or less; true if the cookie
     * is going to try to be saved. There is no guarantee it will be saved and no way to find out
     * if it failed, but if it fails it will be because the shit hit the fan so hard that there's
     * nothing you can do anyway.
     */
    public function setUserCookie (cookie :Object) :Boolean
    {
        return Boolean(callEZCode("setUserCookie_v1", cookie));
    }

    /**
     * A convenience method to just check if it's our turn.
     */
    public function isMyTurn () :Boolean
    {
        return Boolean(callEZCode("isMyTurn_v1"));
    }

    /**
     * Is the game currently in play?
     */
    public function isInPlay () :Boolean
    {
        return Boolean(callEZCode("isInPlay_v1"));
    }

    /**
     * Start the next player's turn. If a playerId is specified, that player's turn will be
     * next. Otherwise the turn will progress to the next natural turn holder (following
     * seating order) or be assigned randomly if the game is just starting.
     */
    public function startNextTurn (nextPlayerId :int = 0) :void
    {
        callEZCode("startNextTurn_v1", nextPlayerId);
    }

    /**
     * Ends the current round. If nextRoundDelay is greater than zero, the next round will be
     * started in the specified number of seconds, otherwise no next round will be started.  This
     * method should not be called at the end of the last round, instead <code>endGame()</code>
     * should be called.
     */
    public function endRound (nextRoundDelay :int = 0) :void
    {
        callEZCode("endRound_v1", nextRoundDelay);
    }

    /**
     * End the game. The specified player ids are winners!
     */
    public function endGame (winnerIds :Array) :void
    {
        callEZCode("endGame_v2", winnerIds);
    }

    /**
     * Requests to start the game again in the specified number of seconds. This should only be
     * used for party games. Seated table games should have each player report that they are ready
     * again and the game will automatically start.
     */
    public function restartGameIn (seconds :int) :void
    {
        callEZCode("restartGameIn_v1", seconds);
    }

    /**
     * Populate any properties or functions we want to expose to the other side of the ezgame
     * security boundary.
     */
    protected function populateProperties (o :Object) :void
    {
        o["connectionClosed_v1"] = connectionClosed_v1;
        o["propertyWasSet_v1"] = propertyWasSet_v1;
        o["controlDidChange_v1"] = controlDidChange_v1;
        o["turnDidChange_v1"] = turnDidChange_v1;
        o["messageReceived_v1"] = messageReceived_v1;
        o["gameStateChanged_v1"] = gameStateChanged_v1;
        o["roundStateChanged_v1"] = roundStateChanged_v1;
        o["dispatchEvent_v1"] = dispatch;
        o["occupantChanged_v1"] = occupantChanged_v1;
        o["userChat_v1"] = userChat_v1;
    }

    /**
     * Private method called when the backend disconnects from us.
     */
    private function connectionClosed_v1 () :void
    {
        _gameData = null;
    }

    /**
     * Private method to post a PropertyChangedEvent.
     */
    private function propertyWasSet_v1 (
        name :String, newValue :Object, oldValue :Object, index :int) :void
    {
        dispatch(new PropertyChangedEvent(this, name, newValue, oldValue, index));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function controlDidChange_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.CONTROL_CHANGED, this));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function turnDidChange_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.TURN_CHANGED, this));
    }

    /**
     * Private method to post a MessageReceivedEvent.
     */
    private function messageReceived_v1 (name :String, value :Object) :void
    {
        dispatch(new MessageReceivedEvent(this, name, value));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function gameStateChanged_v1 (started :Boolean) :void
    {
        dispatch(new StateChangedEvent(started ? StateChangedEvent.GAME_STARTED :
                                       StateChangedEvent.GAME_ENDED, this));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function roundStateChanged_v1 (started :Boolean) :void
    {
        dispatch(new StateChangedEvent(started ? StateChangedEvent.ROUND_STARTED :
                                       StateChangedEvent.ROUND_ENDED, this));
    }

    /**
     * Private method to post a OccupantEvent.
     */
    private function occupantChanged_v1 (occupantId :int, player :Boolean, enter :Boolean) :void
    {
        dispatch(new OccupantChangedEvent(
                     enter ? OccupantChangedEvent.OCCUPANT_ENTERED :
                     OccupantChangedEvent.OCCUPANT_LEFT, this, occupantId, player));
    }

    /**
     * Private method to post a UserChatEvent.
     */
    private function userChat_v1 (speaker :int, message :String) :void
    {
        dispatch(new UserChatEvent(this, speaker, message));
    }

    /**
     * Sets the properties we received from the EZ game framework on the other side of the security
     * boundary.
     */
    protected function setEZProps (o :Object) :void
    {
        // get our gamedata
        _gameData = o.gameData;
        if (o.gameConfig != null) {
            _gameConfig = o.gameConfig;
        }

        // and functions
        _funcs = o;
    }

    /**
     * Call a method across the security boundary.
     */
    protected function callEZCode (name :String, ... args) :*
    {
        if (_funcs != null) {
            try {
                var func :Function = (_funcs[name] as Function);
                if (func != null) {
                    return func.apply(null, args);
                }
            } catch (err :Error) {
                trace(err.getStackTrace());
                trace("--");
                throw new Error("Unable to call host code: " + err.message);
            }

        } else {
            // if _funcs is null, this will almost certainly throw an error..
            checkIsConnected();
        }
    }

    /**
     * A "friend" version of callEZCode for subControls.
     */
    internal function callEZCodeFriend (name :String, ... args) :*
    {
        // var args
        args.unshift(name);
        return callEZCode.apply(this, args);
    }

    /**
     * Throw an error if we're not connected.
     */
    protected function checkIsConnected () :void
    {
        if (!isConnected()) {
            throw new IllegalOperationError(
                "The game is not connected to The Whirled, please check isConnected(). " +
                "If false, your game is being viewed standalone and should adjust.");
        }
    }

    /**
     * Internal method that is called whenever the mouse clicks our root.
     */
    protected function handleRootClick (evt :MouseEvent) :void
    {
        try {
            if (evt.target.stage == null || evt.target.stage.focus != null) {
                return;
            }
        } catch (err :SecurityError) {
        }
        callEZCode("focusContainer_v1");
    }

    /** Contains the data properties shared by all players in the game. */
    protected var _gameData :Object;

    /** Contains any custom game configuration data. */
    protected var _gameConfig :Object = {};

    /** Contains functions exposed to us from the EZGame host. */
    protected var _funcs :Object;

    /** Sub-controls. */
    protected var _collections :CollectionsControl;
    protected var _seating :SeatingControl;
}
}

import flash.events.Event;

dynamic class DynEvent extends Event
{
    public function DynEvent ()
    {
        super("ezgameQuery", true, false);
    }

    override public function clone () :Event
    {
        return new DynEvent();
    }
}
