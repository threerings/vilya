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
import com.google.inject.Injector;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.SessionFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsSession;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.server.ParlorManager;

import com.threerings.micasa.lobby.LobbyRegistry;

import static com.threerings.micasa.Log.log;

/**
 * The main general organizer of everything that goes on in the MiCasa game server process.
 */
public class MiCasaServer extends CrowdServer
{
    public static void main (String[] args)
    {
        runServer(new CrowdModule(), new PresentsServerModule(MiCasaServer.class));
    }

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client manager to use our client class
        _clmgr.setDefaultSessionFactory(new SessionFactory() {
            @Override public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
                return MiCasaSession.class;
            }
            @Override public Class<? extends ClientResolver> getClientResolverClass (Name username) {
                return ClientResolver.class;
            }
        });

        // initialize the lobby registry
        _lobreg.init();

        log.info("MiCasa server initialized.");
    }

    @Inject protected LobbyRegistry _lobreg;
    @Inject protected ParlorManager _parmgr;
}
