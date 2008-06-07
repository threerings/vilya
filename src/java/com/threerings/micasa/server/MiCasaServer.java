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

package com.threerings.micasa.server;

import com.google.inject.Guice;
import com.google.inject.Injector;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.ClientFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.server.ParlorManager;

import com.threerings.micasa.lobby.LobbyRegistry;

import static com.threerings.micasa.Log.log;

/**
 * This class is the main entry point and general organizer of everything
 * that goes on in the MiCasa game server process.
 */
public class MiCasaServer extends CrowdServer
{
    /** The parlor manager in operation on this server. */
    public static ParlorManager parmgr = new ParlorManager();

    /** The lobby registry operating on this server. */
    public static LobbyRegistry lobreg = new LobbyRegistry();

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client manager to use our client class
        _clmgr.setClientFactory(new ClientFactory() {
            public PresentsClient createClient (AuthRequest areq) {
                return new MiCasaClient();
            }
            public ClientResolver createClientResolver (Name username) {
                return new ClientResolver();
            }
        });

        // initialize our parlor manager
        parmgr.init(invmgr, plreg);

        // initialize the lobby registry
        lobreg.init(invmgr);

        log.info("MiCasa server initialized.");
    }

    public static void main (String[] args)
    {
        Injector injector = Guice.createInjector(new Module());
        MiCasaServer server = injector.getInstance(MiCasaServer.class);
        try {
            server.init(injector);
            server.run();
        } catch (Exception e) {
            log.warning("Unable to initialize server.", e);
        }
    }
}
