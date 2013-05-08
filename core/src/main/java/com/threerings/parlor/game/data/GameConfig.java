//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.game.data;

import java.util.List;

import com.google.common.collect.Lists;

import com.threerings.util.Name;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.parlor.client.TableConfigurator;
import com.threerings.parlor.game.client.GameConfigurator;

/**
 * The game config class encapsulates the configuration information for a particular type of game.
 * The hierarchy of game config objects mimics the hierarchy of game managers and controllers. Both
 * the game manager and game controller are provided with the game config object when the game is
 * created.
 *
 * <p> The game config object is also the mechanism used to instantiate the appropriate game
 * manager and controller. Every game must have an associated game config derived class that
 * overrides {@link PlaceConfig#createController} and {@link PlaceConfig#getManagerClassName},
 * returning the appropriate game controller and manager class for that game. Thus the entire chain
 * of events that causes a particular game to be created is the construction of the appropriate
 * game config instance which is provided to the server as part of an invitation or via some other
 * matchmaking mechanism.
 */
public abstract class GameConfig extends PlaceConfig
    implements Cloneable
{
    /** Matchmaking type constant: a game that is started with a list of players, and those are the
     * only players that may play. */
    public static final int SEATED_GAME = 0;

    /** Matchmaking type constant: a game that starts immediately, but only has a certain number of
     * player slots. Users enter the game room, and then choose where to sit. */
    public static final int SEATED_CONTINUOUS = 1;

    /** Matchmaking type constant: a game that starts immediately, and every user that enters is a
     * player. */
    public static final int PARTY = 2;

    /** Maps the matchmaking type codes into strings used in our XML configuration. */
    public static final String[] TYPE_STRINGS = { "table", "entersit", "party" };

    /** The usernames of the players involved in this game, or an empty array if such information
     * is not needed by this particular game. */
    public Name[] players = new Name[0];

    /** Indicates whether or not this game is rated. */
    public boolean rated = true;

    /** Configurations for AIs to be used in this game. Slots with real players should be null and
     * slots with AIs should contain configuration for those AIs. A null array indicates no use of
     * AIs at all. */
    public GameAI[] ais = new GameAI[0];

    /**
     * Returns a numeric identifier for this game class. This may be used to track persisent
     * information on a per-game basis.
     */
    public abstract int getGameId ();

    /**
     * Returns a string identifier for this game class (e.g. "spades"). This may be used to
     * identify a message bundle for which to obtain translations for this game configuration and
     * to look up the name of the game in said bundle.
     */
    public abstract String getGameIdent ();

    /**
     * Returns the matchmaking type of this game: {@link #SEATED_GAME}, etc.
     */
    public int getMatchType ()
    {
        return SEATED_GAME;
    }

    /**
     * Creates a configurator that can be used to create a user interface for configuring this
     * instance prior to starting the game. If no configuration is necessary, this method should
     * return null.
     */
    public GameConfigurator createConfigurator ()
    {
        return null;
    }

    /**
     * Creates a table configurator for initializing 'table' properties of the game. The default
     * implementation returns null.
     */
    public TableConfigurator createTableConfigurator ()
    {
        return null;
    }

    /**
     * Returns a List of strings that describe the configuration of this game. Default
     * implementation returns an empty list.
     */
    public List<String> getDescription ()
    {
        return Lists.newArrayList(); // nothing by default
    }

    /**
     * Returns true if this game config object is equal to the supplied object (meaning it is a
     * game config for the same game and its configuration settings are the same as ours).
     */
    @Override
    public boolean equals (Object other)
    {
        if (!(other instanceof GameConfig)) {
            return false;
        }
        GameConfig that = (GameConfig) other;
        return (this.getGameId() == that.getGameId()) && (this.rated == that.rated);
    }

    /**
     * Computes a hashcode for this game config object that supports our {@link #equals}
     * implementation. Objects that are equal should have the same hashcode.
     */
    @Override
    public int hashCode ()
    {
        // look ma, it's so sophisticated!
        return getClass().hashCode() + (rated ? 1 : 0);
    }

    @Override
    public GameConfig clone ()
    {
        try {
            return (GameConfig) super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }
}
