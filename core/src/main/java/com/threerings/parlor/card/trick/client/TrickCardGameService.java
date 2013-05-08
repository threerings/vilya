//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
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

package com.threerings.parlor.card.trick.client;

import com.threerings.presents.client.InvocationService;

import com.threerings.parlor.card.data.Card;

/**
 * Service calls related to trick card games.
 */
public interface TrickCardGameService extends InvocationService
{
    /**
     * Sends a group of cards to the player at the specified index.
     *
     * @param toidx the index of the player to send the cards to
     * @param cards the cards to send
     */
    public void sendCardsToPlayer (int toidx, Card[] cards);

    /**
     * Plays a card in the trick.
     *
     * @param card the card to play
     * @param handSize the size of the player's hand, which is used to verify
     * that the request is for the current trick
     */
    public void playCard (Card card, int handSize);

    /**
     * A request for a rematch.
     */
    public void requestRematch ();
}
