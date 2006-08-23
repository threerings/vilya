//
// $Id$

package com.threerings.ezgame.data;

import com.threerings.util.MessageBundle;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.PartyGameConfig;

/**
 * A game config for a simple multiplayer game.
 */
public class EZGameConfig extends GameConfig
    implements PartyGameConfig
{
    /** A creator-submitted name of the game. */
    public String gameName;

    // from abstract GameConfig
    public String getBundleName ()
    {
        return "general";
    }

    // from abstract GameConfig
    public GameConfigurator createConfigurator ()
    {
        return null; // nothing here on the java side
    }

    @Override
    public String getGameName ()
    {
        return MessageBundle.taint(gameName);
    }

    // from abstract PlaceConfig
    public String getManagerClassName ()
    {
        return "com.threerings.ezgame.server.EZGameManager";
    }

    // from PartyGameConfig
    public byte getPartyGameType ()
    {
        // TODO
        return NOT_PARTY_GAME;
    }

    @Override
    public boolean equals (Object other)
    {
        if (!super.equals(other)) {
            return false;
        }

        EZGameConfig that = (EZGameConfig) other;
        return this.gameName.equals(that.gameName);
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode(); // TODO: incorp game name?
    }
}
