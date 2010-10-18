//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.game.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;

/**
 * Represents attributes of an AI player.
 */
public class GameAI extends SimpleStreamableObject
{
    /** The "personality" of the AI, which can be interpreted by
     * each puzzle. */
    public var personality :int;

    /** The skill level of the AI. */
    public var skill :int;

    /**
     * Constructs an AI with the specified (game-interpreted) skill and
     * personality.
     */
    public function GameAI (personality :int = 0, skill :int = 0)
    {
        this.personality = personality;
        this.skill = skill;
    }

    // from Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        personality = ins.readInt();
        skill = ins.readInt();
    }

    // from Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(personality);
        out.writeInt(skill);
    }
}
}
