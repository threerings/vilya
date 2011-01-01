//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.card.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

/**
 * Pairs a player index with the card that the player played in the trick.
 */
public class PlayerCard
    implements Streamable
{
    /** The index of the player. */
    public var pidx :int;

    /** The card that the player played. */
    public var card :Card;

    /**
     * Creates a new player card.
     *
     * @param pidx the index of the player
     * @param card the card played
     */
    public function PlayerCard (pidx :int = 0, card :Card = null)
    {
        this.pidx = pidx;
        this.card = card;
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        pidx = ins.readInt();
        card = Card(ins.readObject());
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(pidx);
        out.writeObject(card);
    }
}
}
