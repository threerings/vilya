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

package com.threerings.ezgame;

import java.util.ArrayList;

/**
 * A simple card deck that encodes cards as a string like "Ac" for the
 * ace of clubs, or "Td" for the 10 of diamonds.
 */
public class CardDeck
{
    public CardDeck (EZGame gameObj)
    {
        this(gameObj, "deck");
    }

    public CardDeck (EZGame gameObj, String deckName)
    {
        _gameObj = gameObj;
        _deckName = deckName;

        ArrayList<String> deck = new ArrayList<String>();
        for (String rank : new String[] { "2", "3", "4", "5", "6", "7", "8",
                "9", "T", "J", "Q", "K", "A" }) {
            for (String suit : new String[] { "c", "d", "h", "s" }) {
                deck.add(rank + suit);
            }
        }

        _gameObj.setCollection(_deckName, deck);
    }

    public void dealToPlayer (int playerIdx, int count, String msgName)
    {
        // TODO: support the callback
        _gameObj.dealFromCollection(_deckName, count, msgName, null, playerIdx);
    }

    public void dealToData (int count, String propName)
    {
        _gameObj.dealFromCollection(_deckName, count, propName, null);
    }

    /** The game object. */
    protected EZGame _gameObj;

    /** The name of our deck. */
    protected String _deckName;
}
