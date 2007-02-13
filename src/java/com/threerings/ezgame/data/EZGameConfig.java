//
// $Id$

package com.threerings.ezgame.data;

import com.threerings.util.MessageBundle;

import com.threerings.crowd.client.PlaceController;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

import com.threerings.ezgame.client.EZGameController;

/**
 * A game config for a simple multiplayer game.
 */
public class EZGameConfig extends GameConfig
{
    // TODO: this will eventually contain various XML configuration bits,
    // or we'll expand this to contain other information.
    // For now, the configData is either a classname or url.
    public String configData;

    // TODO: this is separate right now, but may eventually be extracted
    // from configData? Do not read this value, use getGameType()
    public byte gameType = SEATED_GAME;

    /** If non-zero, a game id used to persistently identify the game.
     * This could be thought of as a new-style rating id. */
    public int persistentGameId;

    @Override
    public byte getGameType ()
    {
        return gameType;
    }

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

    @Override // from PlaceConfig
    public PlaceController createController ()
    {
        return new EZGameController();
    }

    // from abstract PlaceConfig
    public String getManagerClassName ()
    {
        return "com.threerings.ezgame.server.EZGameManager";
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
