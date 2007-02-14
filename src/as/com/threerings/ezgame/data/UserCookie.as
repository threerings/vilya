//
// $Id$

package com.threerings.ezgame.data {

import flash.utils.ByteArray;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DSet_Entry;

import com.threerings.ezgame.util.EZObjectMarshaller;

public class UserCookie
    implements DSet_Entry
{
    /** The id of the player that has this cookie. */
    public var playerId :int;

    /** The decoded cookie value. */
    public var cookie :Object;

    // from DSet_Entry
    public function getKey () :Object
    {
        return playerId;
    }

    // from superinterface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        playerId = ins.readInt();
        var ba :ByteArray = (ins.readField(ByteArray) as ByteArray);
        cookie = EZObjectMarshaller.decode(ba);
    }

    // from superinterface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        throw new Error();
    }
}
}
