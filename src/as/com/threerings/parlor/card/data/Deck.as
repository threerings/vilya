//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.card.data {

import com.threerings.util.ArrayUtil;
import com.threerings.util.StreamableArrayList;

/**
 * Instances of this class represent decks of cards.
 */
public class Deck extends StreamableArrayList
{
    /**
     * Constructor.
     *
     * @param includeJokers whether or not to include the two jokers
     * in the deck
     */
    public function Deck (includeJokers :Boolean = false)
    {
        reset(includeJokers);
    }

    /**
     * Deals a hand of cards from the deck.
     *
     * @param size the size of the hand to deal
     * @return the newly created and populated hand, or null
     * if there are not enough cards in the deck to deal the hand
     */
    public function dealHand (size :int) :Hand
    {
        if (length < size) {
            return null;

        } else {
            var hand :Hand = new Hand();
            var offset :int = length - size;
            for (var ii :int = 0; ii < size; ii++) {
                hand.add(removeAt(offset));
            }
            return hand;
        }
    }

    /**
     * Returns a hand of cards to the deck.
     *
     * @param hand the hand of cards to return
     */
    public function returnHand (hand :Hand) :void
    {
        addAll(hand);
        hand.clear();
    }

    /**
     * Resets the deck to its initial state: an unshuffled deck of
     * 52 or 54 cards, depending on whether the jokers are included.
     *
     * @param includeJokers whether or not to include the two jokers
     * in the deck
     */
    public function reset (includeJokers :Boolean) :void
    {
        clear();

        for (var ii :int = CardCodes.SPADES; ii <= CardCodes.DIAMONDS; ii++) {
            for (var jj :int = 2; jj <= CardCodes.ACE; jj++) {
                add(new Card(jj, ii));
            }
        }

        if (includeJokers) {
            add(new Card(CardCodes.RED_JOKER, 3));
            add(new Card(CardCodes.BLACK_JOKER, 3));
        }
    }

    /**
     * Shuffles the deck.
     */
    public function shuffle () :void
    {
        ArrayUtil.shuffle(_array);
    }
}
}
