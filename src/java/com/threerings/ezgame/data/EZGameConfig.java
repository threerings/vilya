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
    /** The name of the game. */
    public String name;

    /** If non-zero, a game id used to persistently identify the game.
     * This could be thought of as a new-style rating id. */
    public int persistentGameId;

    /** The media for the game. In flash this is the URL to the SWF file.
     * In Java, this will be a class name, or maybe a Jar. TODO? */
    public String gameMedia;

    /** The game type. */
    public byte gameType = SEATED_GAME;

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
        return MessageBundle.taint(name);
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
        return this.persistentGameId == that.persistentGameId &&
            this.gameMedia.equals(that.gameMedia);
    }

    @Override
    public int hashCode ()
    {
        return super.hashCode() ^ persistentGameId;
    }
}
