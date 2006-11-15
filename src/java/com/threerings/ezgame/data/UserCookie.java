//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.presents.dobj.DSet;

/**
 * Represents a user's game-specific cookie data.
 */
public class UserCookie
    implements DSet.Entry
{
    /** The index of the player that has this cookie. */
    public int playerIndex;

    /** The cookie value. */
    public byte[] cookie;

    /**
     */
    public UserCookie (int playerIndex, byte[] cookie)
    {
        this.playerIndex = playerIndex;
        this.cookie = cookie;
    }

    // from DSet.Entry
    public Comparable getKey ()
    {
        return playerIndex;
    }
}