//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

import com.threerings.util.Name;

public class UserIdentifier
{
    /**
     * Get the user id for the specified user, or 0 if they're not valid.
     */
    public static function getUserId (name :Name) :int
    {
        return (_userIder == null) ? 0 : _userIder(name);
    }

    public static function setIder (userIder :Function) :void
    {
        _userIder = userIder;
    }

    protected static var _userIder :Function;
}
}
