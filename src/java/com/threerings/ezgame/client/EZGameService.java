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

package com.threerings.ezgame.client;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;

/**
 * Provides services for ez games.
 */
public interface EZGameService extends InvocationService
{
    /**
     * Request to set the specified property.
     *
     * @param value either a byte[] if setting a non-array property or a property at an array
     * index, or a byte[][] if setting an array property where index is -1.
     */
    public void setProperty (Client client, String propName, Object value, int index,
                             boolean testAndSet, Object testValue, InvocationListener listener);

    /**
     * Request to end the turn, possibly futzing the next turn holder unless -1 is specified for
     * the nextPlayerIndex.
     */
    public void endTurn (Client client, int nextPlayerId, InvocationListener listener);

    /**
     * Requests to end the current round. If nextRoundDelay is greater than zero, the next round
     * will be started in the specified number of seconds.
     */
    public void endRound (Client client, int nextRoundDelay, InvocationListener listener);

    /**
     * Request to end the game, with the specified player oids assigned as winners.
     */
    public void endGame (Client client, int[] winnerOids, InvocationListener listener);

    /**
     * Requests to start the game again in the specified number of seconds. This should only be
     * used for party games. Seated table games should have each player report that they are ready
     * again and the game will automatically start.
     */
    public void restartGameIn (Client client, int seconds, InvocationListener listener);

    /**
     * Request to send a private message to one other player in the game.
     *
     * @param value either a byte[] if setting a non-array property or a property at an array
     * index, or a byte[][] if setting an array property where index is -1.
     */
    public void sendMessage (Client client, String msgName, Object value, int playerId,
                             InvocationListener listener);

    /**
     * Ask the dictionary service for a set of random letters appropriate for the given
     * language/culture settings. These will be returned via a message back to the caller.
     *
     * @param client stores information about the caller
     * @param locale is an RFC 3066 string specifying language settings, for example, "en" or
     * "en-us".
     * @param count is the number of letters to be returned.
     * @param listener is the callback function
     */
    public void getDictionaryLetterSet (
        Client client, String locale, int count, ResultListener listener);
    
    /**
     * Ask the dictionary service whether the specified word is valid with the given
     * language/culture settings. The result will be returned via a message back to the caller.
     *
     * @param client stores information about the caller
     * @param locale is an RFC 3066 string specifying language settings, for example, "en" or
     * "en-us".
     * @param word is the word to be checked against the dictionary.
     * @param listener is the callback function
     */
    public void checkDictionaryWord (
        Client client, String locale, String word, ResultListener listener);

    /**
     * Add to the specified named collection.
     *
     * @param clearExisting if true, wipe the old contents.
     */
    public void addToCollection (Client client, String collName, byte[][] data,
                                 boolean clearExisting, InvocationListener listener);

    /**
     * Merge the specified collection into the other.
     */
    public void mergeCollection (
        Client client, String srcColl, String intoColl, InvocationListener listener);

    /**
     * Pick or deal some number of elements from the specified collection, and either set a
     * property in the flash object, or delivery the picks to the specified player index via a game
     * message.
     */
    public void getFromCollection (Client client, String collName, boolean consume, int count,
                                   String msgOrPropName, int playerId, ConfirmListener listener);

    /**
     * Start a ticker that will send out timestamp information at the interval specified.
     *
     * @param msOfDelay must be at least 50, or 0 may be set to halt and clear a previously started
     * ticker.
     */
    public void setTicker (
        Client client, String tickerName, int msOfDelay, InvocationListener listener);

    /**
     * Request to get the specified user's cookie.
     */
    public void getCookie (Client client, int playerId, InvocationListener listener);

    /**
     * Request to set our cookie.
     */
    public void setCookie (Client client, byte[] cookie, InvocationListener listener);
}
