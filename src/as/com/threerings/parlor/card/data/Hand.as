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

import com.threerings.util.StreamableArrayList;

/**
 * Instances of this class represent hands of cards.
 */
public class Hand extends StreamableArrayList
{
    public function Hand ()
    {
        // nothing needed
    }

    /**
     * Counts the members of a particular suit within this hand.
     *
     * @param suit the suit of interest
     * @return the number of cards in the specified suit
     */
    public function getSuitMemberCount (suit :int) :int
    {
        var members :int = 0;
        for (var ii :int = 0; ii < length; ii++) {
            if ((get(ii) as Card).getSuit() == suit) {
                members++;
            }
        }
        return members;
    }

    /**
     * Get an array of the cards in this hand.
     */
    public function getCards () :Array
    {
        return toArray();
    }

    /**
     * Adds all of the specified cards to this hand.
     */
    public function addAllCards (cards :Array) :void
    {
        addAll(cards);
    }

    /**
     * Checks whether this hand contains all of the specified cards.
     */
    public function containsAllCards (cards :Array) :Boolean
    {
        for (var ii :int = 0; ii < cards.length; ii++) {
            if (!contains(cards[ii])) {
                return false;
            }
        }
        return true;
    }

    /**
     * Removes all of the specified cards from this hand.
     */
    public function removeAllCards (cards :Array) :void
    {
        for (var ii :int = 0; ii < cards.length; ii++) {
            remove(cards[ii]);
        }
    }
}
}
