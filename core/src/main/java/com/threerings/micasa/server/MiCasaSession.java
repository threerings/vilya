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

package com.threerings.micasa.server;

import com.google.inject.Inject;

import com.threerings.presents.net.BootstrapData;

import com.threerings.crowd.server.CrowdSession;

import com.threerings.micasa.data.MiCasaBootstrapData;
import com.threerings.micasa.lobby.LobbyRegistry;

/**
 * Extends the Crowd session and provides bootstrap data specific to the MiCasa services.
 */
public class MiCasaSession extends CrowdSession
{
    @Override // from PresentsSession
    protected BootstrapData createBootstrapData ()
    {
        return new MiCasaBootstrapData();
    }

    @Override // from PresentsSession
    protected void populateBootstrapData (BootstrapData data)
    {
        super.populateBootstrapData(data);

        // let the client know their default lobby oid
        ((MiCasaBootstrapData)data).defLobbyOid = _lobreg.getDefaultLobbyOid();
    }

    @Inject protected LobbyRegistry _lobreg;
}
