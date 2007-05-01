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

package com.threerings.parlor.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.io.TypedArray;

/**
 * Table configuration parameters for a game that is to be matchmade using the table services.
 */
public class TableConfig extends SimpleStreamableObject
{
    /** The total number of players that are desired for the table. For team games, this should be
     * set to the total number of players overall, as teams may be unequal. */
    public var desiredPlayerCount :int;

    /** The minimum number of players needed overall (or per-team if a team-based game) for the
     * game to start at the creator's discretion. */
    public var minimumPlayerCount :int;

    /** If non-null, indicates that this is a team-based game and contains the team assignments for
     * each player. For example, a game with three players in two teams- players 0 and 2 versus
     * player 1- would have { {0, 2}, {1} }; */
    public var teamMemberIndices :TypedArray;

    /** Whether the table is "private". */
    public var privateTable :Boolean;

    public function TableConfig ()
    {
        // nothing needed
    }

    // from Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        desiredPlayerCount = ins.readInt();
        minimumPlayerCount = ins.readInt();
        teamMemberIndices = (ins.readObject() as TypedArray);
        privateTable = ins.readBoolean();
    }

    // from Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(desiredPlayerCount);
        out.writeInt(minimumPlayerCount);
        out.writeObject(teamMemberIndices);
        out.writeBoolean(privateTable);
    }
}
}
