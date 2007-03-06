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

package com.threerings.ezgame.data {

import com.threerings.util.Hashable;
import com.threerings.util.MessageBundle;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.crowd.client.PlaceController;

import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

import com.threerings.ezgame.client.EZGameController;

/**
 * A game config for a simple multiplayer ez game.
 */
public class EZGameConfig extends GameConfig
    implements Hashable
{
    /** The name of the game. */
    public var name :String;

    /** If non-zero, a game id used to persistently identify the game.
     * This could be thought of as a new-style rating id. */
    public var persistentGameId:int;

    /** The media for the game. In flash, this is the URL to the SWF file. */
    public var gameMedia :String;

    /** The game type. */
    public var gameType :int = SEATED_GAME;

    public function EZGameConfig ()
    {
        // nothing needed
    }

    override public function getGameType () :int
    {
        return gameType;
    }

    // from abstract GameConfig
    override public function getBundleName () :String
    {
        return "general";
    }

    // TODO
    //override public function createConfigurator () :GameConfigurator


    override public function getGameName () :String
    {
        return MessageBundle.taint(name);
    }

    // from PlaceConfig
    override public function createController () :PlaceController
    {
        return new EZGameController();
    }

    // from abstract PlaceConfig
    override public function getManagerClassName () :String
    {
        throw new Error("Not implemented.");
    }

    override public function hashCode () :int
    {
        return super.hashCode() ^ persistentGameId;
    }

    override public function equals (other :Object) :Boolean
    {
        if (!super.equals(other)) {
            return false;
        }

        var that :EZGameConfig = (other as EZGameConfig);
        return (this.persistentGameId == that.persistentGameId) &&
            (this.gameMedia === that.gameMedia);
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        name = (ins.readField(String) as String)
        persistentGameId = ins.readInt();
        gameMedia = (ins.readField(String) as String);
        gameType = ins.readByte();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);

        out.writeField(name);
        out.writeInt(persistentGameId);
        out.writeField(gameMedia);
        out.writeByte(gameType);
    }
}
}
