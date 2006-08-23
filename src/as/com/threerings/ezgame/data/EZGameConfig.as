//
// $Id$

package com.threerings.ezgame.data {

import com.threerings.util.MessageBundle;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.crowd.client.PlaceController;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.PartyGameConfig;
import com.threerings.parlor.game.data.PartyGameCodes;

import com.threerings.ezgame.client.EZGameController;

/**
 * A game config for a simple multiplayer ez game.
 */
public /* abstract */ class EZGameConfig extends GameConfig
    implements PartyGameConfig
{
    /** A creator-submitted name of the game. */
    public var gameName :String;

    override public function createController () :PlaceController
    {
        return new EZGameController();
    }

    override public function getBundleName () :String
    {
        return "general";
    }

    // TODO
    //override public function createConfigurator () :GameConfigurator

    override public function getGameName () :String
    {
        return MessageBundle.taint(gameName);
    }

    // from PartyGameConfig
    public function getPartyGameType () :int
    {
        // TODO
        return PartyGameCodes.NOT_PARTY_GAME;
    }

    override public function equals (other :Object) :Boolean
    {
        if (!super.equals(other)) {
            return false;
        }

        var that :EZGameConfig = (other as EZGameConfig);
        return (this.gameName === that.gameName);
    }

    override public function hashCode () :int
    {
        return super.hashCode(); // TODO: incorporate gamename?
    }

    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeField(gameName);
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        gameName = (ins.readField(String) as String);
    }
}
}
