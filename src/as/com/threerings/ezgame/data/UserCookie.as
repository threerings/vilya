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
