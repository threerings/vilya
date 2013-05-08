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

package com.threerings.whirled.server;

import com.google.inject.Injector;

import com.threerings.util.Name;

import com.threerings.presents.net.AuthRequest;
import com.threerings.presents.server.SessionFactory;
import com.threerings.presents.server.ClientResolver;
import com.threerings.presents.server.PresentsSession;

import com.threerings.crowd.server.CrowdServer;

/**
 * The Whirled server extends the {@link CrowdServer} and provides access to managers and the like
 * that are needed by the Whirled serviecs.
 */
public abstract class WhirledServer extends CrowdServer
{
    /** Configures dependencies needed by the Whirled server. */
    public static class WhirledModule extends CrowdModule
    {
        @Override protected void configure () {
            super.configure();
            // nada
        }
    }

    @Override
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // configure the client to use our whirled client
        _clmgr.setDefaultSessionFactory(new SessionFactory() {
            @Override public Class<? extends PresentsSession> getSessionClass (AuthRequest areq) {
                return WhirledSession.class;
            }
            @Override
            public Class<? extends ClientResolver> getClientResolverClass (Name username) {
                return ClientResolver.class;
            }
        });
    }
}
