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

import com.threerings.util.ClassUtil;
import com.threerings.util.Hashable;
import com.threerings.util.MessageBundle;
import com.threerings.util.StreamableHashMap;

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
{
    /** Our configuration parameters. These will be seeded with the defaults from the game
     * definition and then configured by the player in the lobby. */
    public var params :StreamableHashMap = new StreamableHashMap();

    public function EZGameConfig (gameId :int = 0, gameDef :GameDefinition = null)
    {
        _gameId = gameId;
        _gameDef = gameDef;
    }

    /** Returns the game definition associated with this config instance. */
    public function getGameDefinition () :GameDefinition
    {
        return _gameDef;
    }

    // from GameConfig
    override public function getGameId () :int
    {
        return _gameId;
    }

    // from GameConfig
    override public function getGameIdent () :String
    {
        return _gameDef.ident;
    }

    // from GameConfig
    override public function getMatchType () :int
    {
        return _gameDef.match.getMatchType();
    }

    // from GameConfig
    override public function createConfigurator () :GameConfigurator
    {
        return null;
    }

    // from abstract PlaceConfig
    override public function getManagerClassName () :String
    {
        throw new Error("Not implemented.");
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        params = (ins.readObject() as StreamableHashMap);
        _gameId = ins.readInt();
        _gameDef = (ins.readObject() as GameDefinition);
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(params);
        out.writeInt(_gameId);
        out.writeObject(_gameDef);
    }

    // from PlaceConfig
    override public function createController () :PlaceController
    {
        var controller :String = getGameDefinition().controller;
        if (controller == null) {
            return createDefaultController();
        }
        var c :Class = ClassUtil.getClassByName(controller);
        return (new c() as PlaceController);
    }

    /**
     * Creates the controller to be used if the game definition does not specify a custom
     * controller.
     */
    protected function createDefaultController () :PlaceController
    {
        return new EZGameController();
    }

    /** Our game's unique id. */
    protected var _gameId :int;

    /** Our game definition. */
    protected var _gameDef :GameDefinition;
}
}
