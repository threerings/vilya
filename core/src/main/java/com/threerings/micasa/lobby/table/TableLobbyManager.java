//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.micasa.lobby.table;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.server.TableManager;

import com.threerings.micasa.lobby.LobbyManager;

/**
 * Extends lobby manager only to ensure that a table lobby object is used for table lobbies.
 */
public class TableLobbyManager extends LobbyManager
{
    @Override
    protected void didStartup ()
    {
        super.didStartup();
        // now that we have our place object, we can create our table manager
        _tmgr = new TableManager(_omgr, _invmgr, _registry, getPlaceObject());
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();
        // clean up our table manager
        _tmgr.shutdown();
    }

    @Override
    protected PlaceObject createPlaceObject ()
    {
        return new TableLobbyObject();
    }

    /** A reference to our table manager. */
    protected TableManager _tmgr;
}
