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

package com.threerings.whirled.server;

import com.google.inject.Injector;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.ClientFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsClient;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.whirled.server.persist.SceneRepository;
import com.threerings.whirled.util.SceneFactory;

import static com.threerings.whirled.Log.log;

/**
 * The Whirled server extends the {@link CrowdServer} and provides access to managers and the like
 * that are needed by the Whirled serviecs.
 */
public abstract class WhirledServer extends CrowdServer
{
    /** Configures dependencies needed by the Whirled server. */
    public static class Module extends CrowdServer.Module
    {
        @Override protected void configure () {
            super.configure();
            // nada
        }
    }

    /** The scene registry. */
    public static SceneRegistry screg;

    @Override // from CrowdServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client to use our whirled client
        clmgr.setClientFactory(new ClientFactory() {
            public PresentsClient createClient (AuthRequest areq) {
                return new WhirledClient();
            }
            public ClientResolver createClientResolver (Name username) {
                return new ClientResolver();
            }
        });

        // create our scene registry
        screg = createSceneRegistry();
    }

    /**
     * Creates the scene registry to be used on this server.
     */
    protected abstract SceneRegistry createSceneRegistry ()
        throws Exception;
}
