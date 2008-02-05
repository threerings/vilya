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

package com.threerings.ezgame {

/**
 * Provides access to 'player' game services. Do not instantiate this class directly,
 * instead access it via GameControl.player.
 */
public class EZPlayerSubControl extends AbstractSubControl
{
    /**
     * @private Constructed via EZGameControl
     */
    public function EZPlayerSubControl (parent :AbstractGameControl)
    {
        super(parent);
    }

    /**
     * Get the user-specific game data for the specified user. The first time this is requested per
     * game instance it will be retrieved from the database. After that, it will be returned from
     * memory. 
     */
    public function getUserCookie (occupantId :int, callback :Function) :void
    {
        callHostCode("getUserCookie_v2", occupantId, callback);
    }
    
    /**
     * Store persistent data that can later be retrieved by an instance of this game. The maximum
     * size of this data is 4096 bytes AFTER AMF3 encoding.  Note: there is no playerId parameter
     * because a cookie may only be stored for the current player.
     *
     * @return false if the cookie could not be encoded to 4096 bytes or less; true if the cookie
     * is going to try to be saved. There is no guarantee it will be saved and no way to find out
     * if it failed, but if it fails it will be because the shit hit the fan so hard that there's
     * nothing you can do anyway.
     */
    public function setUserCookie (cookie :Object) :Boolean
    {
        return Boolean(callHostCode("setUserCookie_v1", cookie));
    }
}
}
