package com.threerings.ezgame {

/**
 * A simple card deck that encodes cards as a string like "Ac" for the
 * ace of clubs, or "Td" for the 10 of diamonds.
 */
public class CardDeck
{
    public function CardDeck (gameCtrl :EZGameControl, deckName :String = "deck")
    {
        _gameCtrl = gameCtrl;
        _deckName = deckName;

        var deck :Array = new Array();
        for each (var rank :String in ["2", "3", "4", "5", "6", "7", "8",
                "9", "T", "J", "Q", "K", "A"]) {
            for each (var suit :String in ["c", "d", "h", "s"]) {
                deck.push(rank + suit);
            }
        }

        _gameCtrl.setCollection(_deckName, deck);
    }

    public function dealToPlayer (
        playerIdx :int, count :int, msgName :String) :void
    {
        // TODO: support the callback
        _gameCtrl.dealFromCollection(_deckName, count, msgName, null, playerIdx);
    }

    public function dealToData (count :int, propName :String) :void
    {
        _gameCtrl.dealFromCollection(_deckName, count, propName, null);
    }

    /** The game control. */
    protected var _gameCtrl :EZGameControl;

    /** The name of our deck. */
    protected var _deckName :String;
}
}
