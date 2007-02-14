package com.threerings.ezgame.client {

import com.threerings.util.Iterator;
import com.threerings.util.Name;

import com.threerings.crowd.data.OccupantInfo;
import com.threerings.ezgame.data.EZGameObject;

/**
 * Contains adapter functions to provide backwards compatibility for
 * older EZ games.
 */
public class GameControlCompatibility
{
    public function GameControlCompatibility (
        ezObj :EZGameObject, backend :GameControlBackend)
    {
        _ezObj = ezObj;
        _backend = backend;
    }

    public function populateProperties (o :Object) :void
    {
        // The below were all deprecated on Feb 13, 2007.
        // These were ever only used internally at Three Rings.
        // These can be removed sooner rather than later.
        o["getFromCollection_v1"] = getFromCollection_v1;
        o["sendMessage_v1"] = sendMessage_v1;
        o["getPlayerCount_v1"] = getPlayerCount_v1;
        o["getPlayerNames_v1"] = getPlayerNames_v1;
        o["getMyIndex_v1"] = getMyIndex_v1;
        o["getTurnHolderIndex_v1"] = getTurnHolderIndex_v1;
        o["getWinnerIndexes_v1"] = getWinnerIndexes_v1;
        o["getUserCookie_v1"] = getUserCookie_v1;
        o["endTurn_v1"] = endTurn_v1;
        o["endGame_v1"] = endGame_v1;
    }

    /**
     * BackCompat: turn a player index into an oid.
     */
    protected function indexToId (index :int) :int
    {
        var name :Name = _ezObj.players[index];
        if (name != null) {
            var occInfo :OccupantInfo = _ezObj.getOccupantInfo(name);
            if (occInfo != null) {
                return occInfo.bodyOid;
            }
        }

        return 0;
    }

    protected function getFromCollection_v1 (
        collName :String, count :int, msgOrPropName :String,
        playerIndex :int, consume :Boolean, callback :Function) :void
    {
        _backend.getFromCollection_v2(collName,  count, msgOrPropName,
            indexToId(playerIndex), consume, callback);
    }

    public function sendMessage_v1 (
        messageName :String, value :Object, playerIndex :int) :void
    {
        _backend.sendMessage_v2(messageName, value, indexToId(playerIndex));
    }

    public function getPlayerCount_v1 () :int
    {
        if (_ezObj.players.length == 0) {
            // party game
            return _ezObj.occupants.size();

        } else {
            return _ezObj.getPlayerCount();
        }
    }

    public function getPlayerNames_v1 () :Array
    {
        var names :Array = [];
        if (_ezObj.players.length == 0) {
            // party game, count all occupants
            var itr :Iterator = _ezObj.occupantInfo.iterator();
            while (itr.hasNext()) {
                var occInfo :OccupantInfo = (itr.next() as OccupantInfo);
                names.push(occInfo.username.toString());
            }

        } else {
            for each (var name :Name in _ezObj.players) {
                names.push((name == null) ? null : name.toString());
            }
        }
        return names;
    }

    public function getMyIndex_v1 () :int
    {
        if (_ezObj.players.length == 0) {
            // TODO: this shouldn't be based off of the String form of the name.
            var array :Array = getPlayerNames_v1();
            return array.indexOf(_backend.getUsername().toString());

        } else {
            return _ezObj.getPlayerIndex(_backend.getUsername());
        }
    }

    public function getTurnHolderIndex_v1 () :int
    {
        return _ezObj.getPlayerIndex(_ezObj.turnHolder);
    }

    public function getWinnerIndexes_v1 () :Array /* of int */
    {
        var arr :Array = new Array();
        if (_ezObj.winners != null) {
            for (var ii :int = 0; ii < _ezObj.winners.length; ii++) {
                if (_ezObj.winners[ii]) {
                    arr.push(ii);
                }
            }
        }
        return arr;
    }

    public function getUserCookie_v1 (
        playerIndex :int, callback :Function) :void
    {
        _backend.getUserCookie_v2(indexToId(playerIndex), callback);
    }

    public function endTurn_v1 (nextPlayerIndex :int) :void
    {
        _backend.endTurn_v2(indexToId(nextPlayerIndex));
    }

    public function endGame_v1 (... winnerDexes) :void
    {
        var winnerIds :Array = [];
        for each (var dex :int in winnerDexes) {
            winnerIds.push(indexToId(dex));
        }
        _backend.endGame_v2.apply(this, winnerIds);
    }

    protected var _ezObj :EZGameObject;

    protected var _backend :GameControlBackend;
}
}
