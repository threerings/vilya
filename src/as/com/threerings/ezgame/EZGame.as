package com.threerings.ezgame {

import flash.events.IEventDispatcher;

/**
 * The game object that you'll be using to manage your game.
 */
public interface EZGame
    extends IEventDispatcher
{
    /**
     * Data accessor.
     */
    function get data () :Object;

    /**
     * Get a property from data.
     */
    function get (propName :String, index :int = -1) :Object;

    /**
     * Set a property that will be distributed. 
     */
    function set (propName :String, value :Object, index :int = -1) :void;

    /**
     * Register an object to receive whatever events it should receive,
     * based on which event listeners it implements. Note that it is not
     * necessary to register any objects which appear on the display list,
     * as they'll be registered automatically.
     */
    function registerListener (obj :Object) :void;

    /**
     * Unregister the specified object from receiving events.
     */
    function unregisterListener (obj :Object) :void;

    /**
     * Set the specified collection to contain the specified values,
     * clearing any previous values.
     */
    function setCollection (collName :String, values :Array) :void;

    /**
     * Add to an existing collection. If it doesn't exist, it will
     * be created. The new values will be inserted randomly into the
     * collection.
     */
    function addToCollection (collName :String, values :Array) :void;

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
    function pickFromCollection (
        collName :String, count :int, msgOrPropName :String,
        playerIndex :int = -1) :void;

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
    function dealFromCollection (
        collName :String, count :int, msgOrPropName :String,
        callBack :Function = null, playerIndex :int = -1) :void;

    /**
     * Merge the specified collection into the other collection.
     * The source collection will be destroyed. The elements from
     * The source collection will be shuffled and appended to the end
     * of the destination collection.
     */
    function mergeCollection (srcColl :String, intoColl :String) :void;

    /**
     * Send a "message" to other clients subscribed to the game.
     * These is similar to setting a property, except that the
     * value will not be saved- it will merely end up coming out
     * as a MessageReceivedEvent.
     *
     * @param playerIndex if -1, sends to all players, otherwise
     * the message will be private to just one player
     */
    function sendMessage (
        messageName :String, value :Object, playerIndex :int = -1) :void;

    /**
     * Start the ticker with the specified name. It will deliver
     * messages to the game object at the specified delay,
     * the value of each message being a single integer, starting with 0
     * and increasing by one with each messsage.
     */
    function startTicker (tickerName :String, msOfDelay :int) :void;

    /**
     * Stop the specified ticker.
     */
    function stopTicker (tickerName :String) :void;

    /**
     * Send a message that will be heard by everyone in the game room,
     * even observers.
     */
    function sendChat (msg :String) :void;

    /**
     * Display the specified message immediately locally: not sent
     * to any other players or observers in the game room.
     */
    function localChat (msg :String) :void;

    /**
     * Get the number of players currently in the game.
     */
    function getPlayerCount () :int;

    /**
     * Get the player names, as an array.
     */
    function getPlayerNames () :Array /* of String */;

    /**
     * Get the index into the player names array of the current player,
     * or -1 if the user is not a player.
     */
    function getMyIndex () :int;

    /**
     * Get the turn holder's index, or -1 if it's nobody's turn.
     */
    function getTurnHolderIndex () :int;

    /**
     * Get the indexes of the winners
     */
    function getWinnerIndexes () :Array /* of int */;

    /**
     * A convenience method to just check if it's our turn.
     */
    function isMyTurn () :Boolean;

    /**
     * Is the game currently in play?
     */
    function isInPlay () :Boolean;

    /**
     * End the current turn. If no next player index is specified,
     * then the next player after the current one is used.
     */
    function endTurn (optionalNextPlayerIndex :int = -1) :void;

    /**
     * End the game. The specified player indexes are winners!
     */
    function endGame (winnerIndex :int, ... rest) :void;

//    function getCurrentRoom () :int;
//
//    function sendPlayerToRoom (playerIndex :int, room :int) :void

    /**
     * Get the user-specific game data for the specified user. The
     * first time this is requested per game instance it will be retrieved
     * from the database. After that, it will be returned from memory.
     */
    function getUserCookie (playerIndex :int, callback :Function) :void;

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
    function setUserCookie (cookie :Object) :Boolean;
//
//    /**
//     * Check to see if the user has the specified token.
//     */
//    function checkUserToken (token :String, callback :Function) :void;
//
//    /**
//     * Take the user to a purchase page.
//     */
//    function purchaseUserToken (token :String, callback :Function) :void;
}
}
