package com.threerings.ezgame {

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;
import flash.events.KeyboardEvent;
import flash.events.MouseEvent;

import flash.display.DisplayObject;

/**
 * The single point of control for each client in your multiplayer EZGame.
 *
 * TODO: lots of documentation.
 */
public class EZGameControl extends EventDispatcher
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
    }

    /**
     * Are we connected and running inside the EZGame environment, or
     * has someone just loaded up this swf by itself?
     */
    public function isConnected () :Boolean
    {
        return (_gameData != null);
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
        }
    }

    /**
     * Data accessor.
     */
    public function get data () :Object
    {
        return _gameData;
    }

    /**
     * Get a property from data.
     */
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

    /**
     * Set a property that will be distributed. 
     */
    public function set (propName :String, value :Object, index :int = -1) :void
    {
        callEZCode("setProperty_v1", propName, value, index);
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
    }

    /**
     * Set the specified collection to contain the specified values,
     * clearing any previous values.
     */
    public function setCollection (collName :String, values :Array) :void
    {
        populateCollection(collName, values, true);
    }

    /**
     * Add to an existing collection. If it doesn't exist, it will
     * be created. The new values will be inserted randomly into the
     * collection.
     */
    public function addToCollection (collName :String, values :Array) :void
    {
        populateCollection(collName, values, false);
    }

    /**
     * Pick (do not remove) the specified number of elements from a collection,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     *
     * @param collName the collection name.
     * @param count the number of elements to pick
     * @param msgOrPropName the name of the message or property
     *        that will contain the picked elements.
     * @param playerIndex if -1 (or unset), the picked elements should be
     *        set on the gameObject as a property for all to see.
     *        If a playerIndex is specified, only that player will receive
     *        the elements as a message.
     */
    // TODO: a way to specify exclusive picks vs. duplicate-OK picks?
    public function pickFromCollection (
        collName :String, count :int, msgOrPropName :String,
        playerIndex :int = -1) :void
    {
        getFromCollection(collName, count, msgOrPropName, playerIndex,
            false, null);
    }

    /**
     * Deal (remove) the specified number of elements from a collection,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     *
     * @param collName the collection name.
     * @param count the number of elements to pick
     * @param msgOrPropName the name of the message or property
     *        that will contain the picked elements.
     * @param playerIndex if -1 (or unset), the picked elements should be
     *        set on the gameObject as a property for all to see.
     *        If a playerIndex is specified, only that player will receive
     *        the elements as a message.
     */
    // TODO: figure out the method signature of the callback
    public function dealFromCollection (
        collName :String, count :int, msgOrPropName :String,
        callback :Function = null, playerIndex :int = -1) :void
    {
        getFromCollection(collName, count, msgOrPropName, playerIndex,
            true, callback);
    }

    /**
     * Merge the specified collection into the other collection.
     * The source collection will be destroyed. The elements from
     * The source collection will be shuffled and appended to the end
     * of the destination collection.
     */
    public function mergeCollection (srcColl :String, intoColl :String) :void
    {
        callEZCode("mergeCollection_v1", srcColl, intoColl);
    }

    /**
     * Send a "message" to other clients subscribed to the game.
     * These is similar to setting a property, except that the
     * value will not be saved- it will merely end up coming out
     * as a MessageReceivedEvent.
     *
     * @param playerIndex if -1, sends to all players, otherwise
     * the message will be private to just one player
     */
    public function sendMessage (
        messageName :String, value :Object, playerIndex :int = -1) :void
    {
        callEZCode("sendMessage_v1", messageName, value, playerIndex);
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

    /**
     * Get the number of players currently in the game.
     */
    public function getPlayerCount () :int
    {
        return int(callEZCode("getPlayerCount_v1"));
    }

    /**
     * Get the player names, as an array.
     */
    public function getPlayerNames () :Array
    {
        return (callEZCode("getPlayerNames_v1") as Array);
    }

    /**
     * Get the index into the player names array of the current player,
     * or -1 if the user is not a player.
     */
    public function getMyIndex () :int
    {
        return int(callEZCode("getMyIndex_v1"));
    }

    /**
     * Get the turn holder's index, or -1 if it's nobody's turn.
     */
    public function getTurnHolderIndex () :int
    {
        return int(callEZCode("getTurnHolderIndex_v1"));
    }

    /**
     * Get the indexes of the winners
     */
    public function getWinnerIndexes () :Array /* of int */
    {
        return (callEZCode("getWinnerIndexes_v1") as Array);
    }

    /**
     * Get the user-specific game data for the specified user. The
     * first time this is requested per game instance it will be retrieved
     * from the database. After that, it will be returned from memory.
     */
    public function getUserCookie (playerIndex :int, callback :Function) :void
    {
        callEZCode("getUserCookie_v1", playerIndex, callback);
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
     * End the current turn. If no next player index is specified,
     * then the next player after the current one is used.
     */
    public function endTurn (nextPlayerIndex :int = -1) :void
    {
        callEZCode("endTurn_v1", nextPlayerIndex);
    }

    /**
     * End the game. The specified player indexes are winners!
     */
    public function endGame (... winnerIndexes) :void
    {
        var args :Array = winnerIndexes;
        args.unshift("endGame_v1");

        // goddamn var-args complications in actionscript
        callEZCode.apply(null, args);
    }

    /**
     * Your own events may not be dispatched here.
     */
    override public function dispatchEvent (event :Event) :Boolean
    {
        // Ideally we want to not be an IEventDispatcher so that people
        // won't try to do this on us, but if we do that, then some other
        // object will be the target during dispatch, and that's confusing.
        // It's really nice to be able to 
        throw new IllegalOperationError();
    }

    /**
     * Helper method for setCollection and addToCollection.
     */
    protected function populateCollection (
        collName :String, values :Array, clearExisting :Boolean) :void
    {
        callEZCode("populateCollection_v1", collName, values, clearExisting);
    }

    /**
     * Helper method for pickFromCollection and dealFromCollection.
     */
    protected function getFromCollection(
        collName :String, count :int, msgOrPropName :String, playerIndex :int,
        consume :Boolean, callback :Function) :void
    {
        callEZCode("getFromCollection_v1", collName, count, msgOrPropName,
            playerIndex, consume, callback);
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

    /**
     * Secret function to dispatch property changed events.
     */
    internal function dispatch (event :Event) :void
    {
        try {
            super.dispatchEvent(event);
        } catch (err :Error) {
            trace("Error dispatching event to user game.");
            trace(err.getStackTrace());
        }
    }

    /** Contains the data properties shared by all players in the game. */
    protected var _gameData :Object;

    /** Contains functions exposed to us from the EZGame host. */
    protected var _funcs :Object;
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
