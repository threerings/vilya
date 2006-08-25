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
