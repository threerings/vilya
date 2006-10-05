//
// $Id: GameAI.java 3399 2005-03-12 07:37:34Z mdb $

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
        personality = ins.readInt();
        skill = ins.readInt();
    }

    // from Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(personality);
        out.writeInt(skill);
    }
}
}
