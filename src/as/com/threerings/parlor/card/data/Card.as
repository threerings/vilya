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

package com.threerings.parlor.card.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.util.Byte;
import com.threerings.util.Comparable;
import com.threerings.util.Hashable;
import com.threerings.util.Integer;
import com.threerings.util.StringBuilder;

import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.DSet_Entry;

/**
 * Instances of this class represent individual playing cards.
 */
public class Card
    implements DSet_Entry, Comparable, Hashable
{
    /**
     * Creates a new card.
     *
     * @param number the number of the card
     * @param suit the suit of the card
     */
    public function Card (number :int = 0, suit :int = 0)
    {
        _value = ((suit << 5) | number);
    }

    /**
     * Returns the suit of the card: SPADES, HEARTS, DIAMONDS, or
     * CLUBS.  If the card is the joker, the suit is undefined.
     *
     * @return the suit of the card
     */
    public function getSuit () :int
    {
        return (_value >> 5);
    }

    /**
     * Checks whether the card is a number card (2 to 10).
     *
     * @return true if the card is a number card, false otherwise
     */
    public function isNumber () :Boolean
    {
        var number :int = getNumber();
        return number >= 2 && number <= 10;
    }

    /**
     * Checks whether the card is a face card (KING, QUEEN, or JACK).
     *
     * @return true if the card is a face card, false otherwise
     */
    public function isFace () :Boolean
    {
        var number :int = getNumber();
        return number == CardCodes.KING || number == CardCodes.QUEEN ||
            number == CardCodes.JACK;
    }

    /**
     * Checks whether the card is an ace.
     *
     * @return true if the card is an ace, false otherwise
     */
    public function isAce () :Boolean
    {
        return getNumber() == CardCodes.ACE;
    }

    /**
     * Checks whether the card is a joker.
     *
     * @return true if the card is a joker, false otherwise
     */
    public function isJoker () :Boolean
    {
        var number :int = getNumber();
        return number == CardCodes.RED_JOKER || number == CardCodes.BLACK_JOKER;
    }

    /**
     * Returns a hash code for this card.
     *
     * @return this card's hash code
     */
    public function hashCode () :int
    {
        return _value;
    }

    /**
     * Compares this card to another.  The card order is the same as the
     * initial deck ordering: two through ten, jack, queen, king, ace for
     * spades, hearts, clubs, and diamonds, then the red joker and the
     * black joker.
     *
     * @param other the other card to compare this to
     * @return -1, 0, or +1, depending on whether this card is less than,
     * equal to, or greater than the other card
     */
    public function compareTo (other :Object) :int
    {
        var otherValue :int = (other as Card)._value;
        if (_value > otherValue) {
            return +1;
        } else if (_value < otherValue) {
            return -1;
        } else {
            return 0;
        }
    }

    /**
     * Checks this card for equality with another.
     *
     * @param other the other card to compare
     * @return true if the cards are equal, false otherwise
     */
    public function equals (other :Object) :Boolean
    {
        if (other is Card) {
            return _value == (other as Card)._value;
        } else {
            return false;
        }
    }

    /**
     * Returns a string representation of this card.
     *
     * @return a description of this card
     */
    public function toString () :String
    {
        var number :int = getNumber();

        if (number == CardCodes.RED_JOKER) {
            return "RJ";

        } else if (number == CardCodes.BLACK_JOKER) {
            return "BJ";

        } else {
            var sb : StringBuilder = new StringBuilder();
            if (number >= 2 && number <= 9) {
                sb.append(number);

            } else {
                switch (number) {
                    case 10: sb.append('T'); break;
                    case CardCodes.JACK: sb.append('J'); break;
                    case CardCodes.QUEEN: sb.append('Q'); break;
                    case CardCodes.KING: sb.append('K'); break;
                    case CardCodes.ACE: sb.append('A'); break;
                    default: sb.append('?'); break;
                }
            }

            switch (getSuit()) {
                case CardCodes.SPADES: sb.append('s'); break;
                case CardCodes.HEARTS: sb.append('h'); break;
                case CardCodes.CLUBS: sb.append('c'); break;
                case CardCodes.DIAMONDS: sb.append('d'); break;
                default: sb.append('?'); break;
            }

            return sb.toString();
        }
    }

    // from interface DSet_Entry
    public function getKey () :Object
    {
        return _value;
    }

    /**
     * Returns the value of the card, either from 2 to 11 or
     * KING, QUEEN, JACK, ACE, RED_JOKER, or BLACK_JOKER.
     *
     * @return the value of the card
     */
    public function getNumber () :int
    {
        return (_value & 0x1F);
    }

    /**
     * Checks whether or not this card is valid.  The no-arg public
     * constructor for deserialization creates an invalid card.
     *
     * @return true if this card is valid, false if not
     */
    public function isValid () :Boolean
    {
        var number :int = getNumber(), suit :int = getSuit();
        return number == CardCodes.RED_JOKER ||
            number == CardCodes.BLACK_JOKER ||
            (number >= 2 && number <= CardCodes.ACE &&
             suit >= CardCodes.SPADES && suit <= CardCodes.DIAMONDS);
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        _value = ins.readByte();
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeByte(_value);
    }

    /** The number of the card. */
    protected var _value :int;
}
}
