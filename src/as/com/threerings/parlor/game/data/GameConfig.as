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

package com.threerings.parlor.game.data {

import com.threerings.util.ClassUtil;
import com.threerings.util.Cloneable;
import com.threerings.util.Hashable;
import com.threerings.util.Name;
import com.threerings.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.parlor.client.TableConfigurator;
import com.threerings.parlor.game.client.GameConfigurator;

/**
 * The game config class encapsulates the configuration information for a particular type of
 * game. The hierarchy of game config objects mimics the hierarchy of game managers and
 * controllers. Both the game manager and game controller are provided with the game config object
 * when the game is created.
 *
 * <p> The game config object is also the mechanism used to instantiate the appropriate game
 * manager and controller. Every game must have an associated game config derived class that
 * overrides {@link #createController} and {@link #getManagerClassName}, returning the appropriate
 * game controller and manager class for that game. Thus the entire chain of events that causes a
 * particular game to be created is the construction of the appropriate game config instance which
 * is provided to the server as part of an invitation or via some other matchmaking mechanism.
 */
public /*abstract*/ class GameConfig extends PlaceConfig
    implements Cloneable, Hashable
{
    /** Game type constant: a game that is started with a list of players, and those are the only
     * players that may play. */
    public static const SEATED_GAME :int = 0;

    /** Game type constant: a game that starts immediately, but only has a certain number of player
     * slots. Users enter the game room, and then choose where to sit. */
    public static const SEATED_CONTINUOUS :int = 1;

    /** Game type constant: a game that starts immediately, and every user that enters is a
     * player. */
    public static const PARTY :int = 2;

    /** The usernames of the players involved in this game, or an empty array if such information
     * is not needed by this particular game. */
    public var players :TypedArray = TypedArray.create(Name);

    /** Indicates whether or not this game is rated. */
    public var rated :Boolean = true;

    /** Configurations for AIs to be used in this game. Slots with real players should be null and
     * slots with AIs should contain configuration for those AIs. A null array indicates no use of
     * AIs at all. */
    public var ais :TypedArray = TypedArray.create(GameAI);

    public function GameConfig ()
    {
        // nothing needed
    }

    /**
     * Returns a numeric identifier for this game class. This may be used to track persisent
     * information on a per-game basis.
     */
    public function getGameId () :int
    {
        throw new Error("abstract");
    }

    public function getGameIdent () :String
    {
        throw new Error("abstract");
    }

    /**
     * Get the type of game.
     */
    public function getMatchType () :int
    {
        return SEATED_GAME;
    }

    /**
     * Creates a configurator that can be used to create a user interface for configuring this
     * instance prior to starting the game. If no configuration is necessary, this method should
     * return null.
     */
    public /*abstract*/ function createConfigurator () :GameConfigurator
    {
        throw new Error("abstract");
    }

    /**
     * Creates a table configurator for initializing 'table' properties of the game. The default
     * implementation returns null.
     */
    public function createTableConfigurator () :TableConfigurator
    {
        return null;
    }

    /**
     * Computes a hashcode for this game config object that supports our {@link #equals}
     * implementation. Objects that are equal should have the same hashcode.
     */
    public function hashCode () :int
    {
        // look ma, it's so sophisticated!
        return StringUtil.hashCode(ClassUtil.getClassName(this)) + (rated ? 1 : 0);
    }

    // from Cloneable
    public function clone () :Object
    {
        var copy :GameConfig = (ClassUtil.newInstance(this) as GameConfig);
        copy.players = this.players;
        copy.rated = this.rated;
        copy.ais = this.ais;
        return copy;
    }

    /**
     * Returns true if this game config object is equal to the supplied object (meaning it is also
     * a game config object and its configuration settings are the same as ours).
     */
    public function equals (other :Object) :Boolean
    {
        // make sure they're of the same class
        if (ClassUtil.isSameClass(other, this)) {
            var that :GameConfig = GameConfig(other);
            return this.getGameId() == that.getGameId() && this.rated == that.rated;
        } else {
            return false;
        }
    }

    /**
     * Returns an Array of strings that describe the configuration of this game. Default
     * implementation returns an empty array.
     */
    public function getDescription () :Array
    {
        return new Array(); // nothing by default
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        players = TypedArray(ins.readObject());
        rated = ins.readBoolean();
        ais = TypedArray(ins.readObject());
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(players);
        out.writeBoolean(rated);
        out.writeObject(ais);
    }
}
}
