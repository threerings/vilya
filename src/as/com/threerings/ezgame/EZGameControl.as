package com.threerings.ezgame {

import flash.errors.IllegalOperationError;

import flash.events.Event;
import flash.events.EventDispatcher;

import flash.display.DisplayObject;

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
        setEZProps(event.ezProps);
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
        callEZCode("setProperty_v1", propName, value, index);
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
        callEZCode("mergeCollection_v1", srcColl, intoColl);
    }

    // from EZGame
    public function sendMessage (
        messageName :String, value :Object, playerIndex :int = -1) :void
    {
        callEZCode("sendMessage_v1", messageName, value, playerIndex);
    }

    // from EZGame
    public function startTicker (tickerName :String, msOfDelay :int) :void
    {
        callEZCode("setTicker_v1", tickerName, msOfDelay);
    }

    // from EZGame
    public function stopTicker (tickerName :String) :void
    {
        startTicker(tickerName, 0);
    }

    // from EZGame
    public function sendChat (msg :String) :void
    {
        callEZCode("sendChat_v1", msg);
    }

    // from EZGame
    public function localChat (msg :String) :void
    {
        callEZCode("localChat_v1", msg);
    }

    // from EZGame
    public function getPlayerCount () :int
    {
        return int(callEZCode("getPlayerCount_v1"));
    }

    // from EZGame
    public function getPlayerNames () :Array
    {
        return (callEZCode("getPlayerNames_v1") as Array);
    }

    // from EZGame
    public function getMyIndex () :int
    {
        return int(callEZCode("getMyIndex_v1"));
    }

    // from EZGame
    public function getTurnHolderIndex () :int
    {
        return int(callEZCode("getTurnHolderIndex_v1"));
    }

    // from EZGame
    public function getWinnerIndexes () :Array /* of int */
    {
        return (callEZCode("getWinnerIndexes_v1") as Array);
    }

    // from EZGame
    public function getUserCookie (playerIndex :int, callback :Function) :void
    {
        callEZCode("getUserCookie_v1", playerIndex, callback);
    }

    // from EZGame
    public function setUserCookie (cookie :Object) :Boolean
    {
        return Boolean(callEZCode("setUserCookie_v1", cookie));
    }

    // from EZGame
    public function isMyTurn () :Boolean
    {
        return Boolean(callEZCode("isMyTurn_v1"));
    }

    // from EZGame
    public function isInPlay () :Boolean
    {
        return Boolean(callEZCode("isInPlay_v1"));
    }

    // from EZGame
    public function endTurn (nextPlayerIndex :int = -1) :void
    {
        callEZCode("endTurn_v1", nextPlayerIndex);
    }

    // from EZGame
    public function endGame (... winnerIndexes) :void
    {
        var args :Array = winnerIndexes;
        args.unshift("endGame_v1");

        // goddamn var-args complications in actionscript
        callEZCode.apply(null, args);
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
    }

    private function propertyWasSet_v1 (
        name :String, newValue :Object, oldValue :Object, index :int) :void
    {
        dispatch(
            new PropertyChangedEvent(this, name, newValue, oldValue, index));
    }

    private function turnDidChange_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.TURN_CHANGED, this));
    }

    private function messageReceived_v1 (name :String, value :Object) :void
    {
        dispatch(new MessageReceivedEvent(this, name, value));
    }

    private function gameDidStart_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.GAME_STARTED, this));
    }

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

    protected var _gameData :Object;

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
