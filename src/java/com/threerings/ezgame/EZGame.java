package com.threerings.ezgame;

import java.util.Collection;

/**
 * The game object that you'll be using to manage your game.
 */
public interface EZGame
{
    /**
     * Get a property from data.
     */
    public Object get (String propName);

    /**
     * Get a property from data.
     */
    public Object get (String propName, int index);

    /**
     * Set a property that will be distributed. 
     */
    public void set (String propName, Object value);

    /**
     * Set a property that will be distributed. 
     */
    public void set (String propName, Object value, int index);

    /**
     * Set a property that will be distributed, if the previous value
     * matches the test value.
     */
    public void testAndSet (String propName, Object value, Object testValue);

    /**
     * Set a property that will be distributed, if the previous value
     * matches the test value.
     */
    public void testAndSet (String propName, Object value, Object testValue, int index);

    /**
     * Register an object to receive whatever events it should receive,
     * based on which event listeners it implements. Note that it is not
     * necessary to register any objects which appear on the display list,
     * as they'll be registered automatically.
     */
    public void registerListener (Object obj);

    /**
     * Unregister the specified object from receiving events.
     */
    public void unregisterListener (Object obj);

    /**
     * Set the specified collection to contain the specified values,
     * clearing any previous values.
     *
     * @param values may be a Collection or array. Note that arrays
     * of primitive types will be transmogrified into an Object[] containing
     * wrapper objects.
     */
    public void setCollection (String collName, Object values);

    /**
     * Add to an existing collection. If it doesn't exist, it will
     * be created. The new values will be inserted randomly into the
     * collection.
     *
     * @param values may be a Collection or array. Note that arrays
     * of primitive types will be transmogrified into an Object[] containing
     * wrapper objects.
     */
    public void addToCollection (String collName, Object values);

    /**
     * Pick (do not remove) the specified number of elements from a collection,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     */
    // TODO: a way to specify exclusive picks vs. duplicate-OK picks?
    public void pickFromCollection (
        String collName, int count, String propName);

    public void pickFromCollection (
        String collName, int count, String msgName, int playerIndex);

    /**
     * Deal (remove) the specified number of elements from a collection,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     */
    // TODO: figure out the method signature of the callback
    public void dealFromCollection (
        String collName, int count, String propName,
        DealListener listener);

    public void dealFromCollection (
        String collName, int count, String msgName,
        DealListener listener, int playerIndex);

    /**
     * Merge the specified collection into the other collection.
     * The source collection will be destroyed. The elements from
     * The source collection will be shuffled and appended to the end
     * of the destination collection.
     */
    public void mergeCollection (String srcColl, String intoColl);

    /**
     * Send a "message" to other clients subscribed to the game.
     * These is similar to setting a property, except that the
     * value will not be saved- it will merely end up coming out
     * as a MessageReceivedEvent.
     *
     * @param playerIndex if -1, sends to all players, otherwise
     * the message will be private to just one player
     */
    public void sendMessage (
        String messageName, Object value);
    
    public void sendMessage (
        String messageName, Object value, int playerIndex);

    /**
     * Start the ticker with the specified name. It will deliver
     * messages to the game object at the specified delay,
     * the value of each message being a single integer, starting with 0
     * and increasing by one with each messsage.
     */
    public void startTicker (String tickerName, int msOfDelay);

    /**
     * Stop the specified ticker.
     */
    public void stopTicker (String tickerName);

    /**
     * Send a message that will be heard by everyone in the game room,
     * even observers.
     */
    public void sendChat (String msg);

    /**
     * Display the specified message immediately locally: not sent
     * to any other players or observers in the game room.
     */
    public void localChat (String msg);

    /**
     * Get the number of players currently in the game.
     */
    public int getPlayerCount ();

    /**
     * Get the player names, as an array.
     */
    public String[] getPlayerNames ();

    /**
     * Get the index into the player names array of the current player,
     * or -1 if the user is not a player.
     */
    public int getMyIndex ();

    /**
     * Get the turn holder's index, or -1 if it's nobody's turn.
     */
    public int getTurnHolderIndex ();

    /**
     * Get the indexes of the winners
     */
    public int[] getWinnerIndexes ();

    /**
     * A convenience method to just check if it's our turn.
     */
    public boolean isMyTurn ();

    /**
     * Is the game currently in play?
     */
    public boolean isInPlay ();

    /**
     * End the current turn. If no next player index is specified,
     * then the next player after the current one is used.
     */
    public void endTurn ();

    public void endTurn (int nextPlayerIndex);

    /**
     * End the game. The specified player indexes are winners!
     */
    public void endGame (int... winners);
}
