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
 * Dispatched when the game starts, usually after all players are present.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.GAME_STARTED
 */
[Event(name="GameStarted", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the turn changes in a turn-based game.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.TURN_CHANGED
 */
[Event(name="GameStarted", type="com.threerings.ezgame.StateChangedEvent")]

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
 * Dispatched when a message arrives with information that is not part
 * of the shared game state.
 *
 * @eventType com.threerings.ezgame.MessageReceivedEvent.TYPE
 */
[Event(name="msgReceived", type="com.threerings.ezgame.MessageReceivedEvent")]


/**
 * The single point of control for each client in your multiplayer EZGame.
 *
 * TODO: lots of documentation.
 */
public class EZGameControl extends BaseControl
{
    /**
     * Create an EZGameControl object using some display object currently
     * on the hierarchy.
     */
    public function EZGameControl (disp :DisplayObject)
    {
        var event :DynEvent = new DynEvent();
        event.userProps = new Object();
        populateProperties(event.userProps);
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
        super.addEventListener(type, listener, useCapture, priority,
            useWeakReference);

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
     * Are we connected and running inside the EZGame environment, or
     * has someone just loaded up this swf by itself?
     */
    public function isConnected () :Boolean
    {
        return (_gameData != null);
    }

    /**
     * Get the CollectionsControl, which contains methods for utilizing
     * the server to dispatch private information.
     */
    public function get collections () :CollectionsControl
    {
        if (_collections == null) {
            _collections = new CollectionsControl(this);
        }
        return _collections;
    }

    /**
     * Get the SeatingControl, which contains methods for checking
     * and assigning player seating positions.
     */
    public function get seating () :SeatingControl
    {
        return _seating;
    }

//    /**
//     * Data accessor.
//     */
//    public function get data () :Object
//    {
//        return _gameData;
//    }

    /**
     * Get a property from data.
     */
    public function get (propName :String, index :int = -1) :Object
    {
        var value :Object = _gameData[propName];
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

    /**
     * Set a property that will be distributed. 
     */
    public function set (propName :String, value :Object, index :int = -1) :void
    {
        callEZCode("setProperty_v1", propName, value, index, false);
    }

    /**
     * Set a property, but have this client immediately set the value so that
     * it can be re-read. The property change event will still arrive and
     * will be your clue as to when the other clients will see the newly
     * set value. Be careful with this method, as it can allow data
     * inconsistency: two clients may see different values for a property
     * if one of them recently set it immediately, and the resultant
     * PropertyChangedEvent's oldValue also may not be consistent.
     */
    public function setImmediate (propName :String, value :Object, index :int = -1) :void
    {
        callEZCode("setProperty_v1", propName, value, index, true);
    }

    /**
     * Set a property that will be distributed, but only if it's equal
     * to the specified test value.
     *
     * Please note that, unlike in the standard set() function, the property
     * will not be updated right away, but will require a request to the server
     * and a response back. For this reason, there may be a considerable delay
     * between calling testAndSet, and seeing the result of the update.
     *
     * The operation is 'atomic', in the sense that testing and setting take place
     * during the same server event. In comparison, a separate 'get' followed by
     * a 'set' operation would involve two events with two network round-trips,
     * and no guarantee that the value won't change between the events.
     */
    public function testAndSet (
        propName :String, newValue :Object, testValue :Object, index :int = -1) :void
    {
        callEZCode("testAndSetProperty_v1", propName, newValue, testValue, index);
    }

    /**
     * Register an object to receive whatever events it should receive,
     * based on which event listeners it implements.
     */
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
        if (obj is OccupantChangedListener) {
            var ocl :OccupantChangedListener = (obj as OccupantChangedListener);
            addEventListener(OccupantChangedEvent.OCCUPANT_ENTERED, ocl.occupantEntered,
                false, 0, true);
            addEventListener(OccupantChangedEvent.OCCUPANT_LEFT, ocl.occupantLeft,
                false, 0, true);
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
            removeEventListener(
                StateChangedEvent.GAME_STARTED, scl.stateChanged);
            removeEventListener(
                StateChangedEvent.TURN_CHANGED, scl.stateChanged);
            removeEventListener(
                StateChangedEvent.GAME_ENDED, scl.stateChanged);
        }
        if (obj is OccupantChangedListener) {
            var ocl :OccupantChangedListener = (obj as OccupantChangedListener);
            removeEventListener(OccupantChangedEvent.OCCUPANT_ENTERED, ocl.occupantEntered);
            removeEventListener(OccupantChangedEvent.OCCUPANT_LEFT, ocl.occupantLeft);
        }
    }

    /**
     * Requests a set of random letters from the dictionary service.
     * The letters will arrive in a separate message with the specified key,
     * as an array of strings.
     *
     * @param locale RFC 3066 string that represents language settings
     * @param count the number of letters to be produced
     * @param callback the function that will process the results, of the form:
     *          function (letters :Array) :void
     *        where letters is an array of strings containing letters
     *        for the given language settings (potentially empty).
     */
    public function getDictionaryLetterSet (
        locale :String, count :int, callback :Function) :void
    {
        callEZCode("getDictionaryLetterSet_v1", locale, count, callback);
    }

    /**
     * Requests a set of random letters from the dictionary service.
     * The letters will arrive in a separate message with the specified key,
     * as an array of strings.
     *
     * @param RFC 3066 string that represents language settings
     * @param word the string contains the word to be checked
     * @param callback the function that will process the results, of the form:
     *          function (word :String, result :Boolean) :void
     *        where word is a copy of the word that was requested, and result
     *        specifies whether the word is valid given language settings
     */
    public function checkDictionaryWord (
        locale :String, word :String, callback :Function) :void
    {
        callEZCode("checkDictionaryWord_v1", locale, word, callback);
    }

    /**
     * Send a "message" to other clients subscribed to the game.
     * These is similar to setting a property, except that the
     * value will not be saved- it will merely end up coming out
     * as a MessageReceivedEvent.
     *
     * @param playerId if 0 (or unset), sends to all players, otherwise
     * the message will be private to just one player
     */
    public function sendMessage (
        messageName :String, value :Object, playerId :int = 0) :void
    {
        callEZCode("sendMessage_v2", messageName, value, playerId);
    }

    /**
     * Start the ticker with the specified name. It will deliver
     * messages to the game object at the specified delay,
     * the value of each message being a single integer, starting with 0
     * and increasing by one with each messsage.
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
     * Send a message that will be heard by everyone in the game room,
     * even observers.
     */
    public function sendChat (msg :String) :void
    {
        callEZCode("sendChat_v1", msg);
    }

    /**
     * Display the specified message immediately locally: not sent
     * to any other players or observers in the game room.
     */
    public function localChat (msg :String) :void
    {
        callEZCode("localChat_v1", msg);
    }

    // TODO: NEW
    public function getOccupants () :Array /* of playerId */
    {
        return (callEZCode("getOccupants_v1") as Array);
    }

    // TODO: NEW
    /**
     * Get the display name of the specified occupant.
     * Two players may have the same name: always use playerId to
     * purposes of identification and comparison. The name is for display
     * only.
     */
    public function getOccupantName (playerId :int) :String
    {
        return String(callEZCode("getOccupantName_v1", playerId));
    }

    // TODO: NEW
    public function getMyId () :int
    {
        return int(callEZCode("getMyId_v1"));
    }

    public function getTurnHolder () :int
    {
        return int(callEZCode("getTurnHolder_v1"));
    }

    /**
     * Get the user-specific game data for the specified user. The
     * first time this is requested per game instance it will be retrieved
     * from the database. After that, it will be returned from memory.
     */
    public function getUserCookie (playerId :int, callback :Function) :void
    {
        callEZCode("getUserCookie_v1", playerId, callback);
    }

    /**
     * Store persistent data that can later be retrieved by an instance
     * of this game. The maximum size of this data is 4096 bytes AFTER
     * AMF3 encoding.
     *
     * Note: there is no playerIndex parameter because a cookie may only
     * be stored for the current player.
     *
     * @return false if the cookie could not be encoded to 4096 bytes
     * or less; true if the cookie is going to try to be saved. There is
     * no guarantee it will be saved and no way to find out if it failed,
     * but if it fails it will be because the shit hit the fan so hard that
     * there's nothing you can do anyway.
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
     * End the current turn. If no next player id is specified,
     * then the next player after the current one is used.
     */
    public function endTurn (nextPlayerId :int = 0) :void
    {
        callEZCode("endTurn_v2", nextPlayerId);
    }

    /**
     * End the game. The specified player ids are winners!
     */
    public function endGame (... winnerIds) :void
    {
        var args :Array = winnerIds;
        args.unshift("endGame_v2");

        // goddamn var-args complications in actionscript
        callEZCode.apply(null, args);
    }

    /**
     * Populate any properties or functions we want to expose to
     * the other side of the ezgame security boundary.
     */
    protected function populateProperties (o :Object) :void
    {
        o["propertyWasSet_v1"] = propertyWasSet_v1;
        o["turnDidChange_v1"] = turnDidChange_v1;
        o["messageReceived_v1"] = messageReceived_v1;
        o["gameDidStart_v1"] = gameDidStart_v1;
        o["gameDidEnd_v1"] = gameDidEnd_v1;
        o["dispatchEvent_v1"] = dispatch;
        o["occupantChanged_v1"] = occupantChanged_v1;
    }

    /**
     * Private method to post a PropertyChangedEvent.
     */
    private function propertyWasSet_v1 (
        name :String, newValue :Object, oldValue :Object, index :int) :void
    {
        dispatch(
            new PropertyChangedEvent(this, name, newValue, oldValue, index));
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
    private function gameDidStart_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.GAME_STARTED, this));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function gameDidEnd_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.GAME_ENDED, this));
    }

    /**
     * Private method to post a OccupantEvent.
     */
    private function occupantChanged_v1 (occupantId :int, player :Boolean, enter :Boolean) :void
    {
        dispatch(new OccupantChangedEvent(
            enter ? OccupantChangedEvent.OCCUPANT_ENTERED
                  : OccupantChangedEvent.OCCUPANT_LEFT, 
            this, occupantId, player));
    }

    /**
     * Sets the properties we received from the EZ game framework
     * on the other side of the security boundary.
     */
    protected function setEZProps (o :Object) :void
    {
        // get our gamedata
        _gameData = o.gameData;

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
                trace("Unable to call ez code: " + err);
            }
        }
    }

    /**
     * A "friend" version of callEZCode for subControls.
     */
    internal function callEZCodeFriend (name :String, ... args) :*
    {
        // var args
        args.unshift(name);
        callEZCode.apply(this, args);
    }

    /**
     * Internal method that is called whenever the mouse clicks our root.
     */
    protected function handleRootClick (evt :MouseEvent) :void
    {
        try {
            if (evt.target.stage.focus != null) {
                return;
            }
        } catch (err :SecurityError) {
        }
        callEZCode("focusContainer_v1");
    }

    /** Contains the data properties shared by all players in the game. */
    protected var _gameData :Object;

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
