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
    // TODO: this will eventually contain various XML configuration bits,
    // or we'll expand this to contain other information.
    // For now, the configData is either a classname or url.
    public String configData;

    // from abstract GameConfig
    public String getBundleName ()
    {
        return "general";
    }

    // from abstract GameConfig
    public GameConfigurator createConfigurator ()
    {
        // TODO
        return null;
    }

    @Override
    public String getGameName ()
    {
        return MessageBundle.taint(configData);
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
        return this.configData.equals(that.configData);
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode(); // TODO: incorp configData?
    }
}
