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

package com.threerings.micasa.simulator.server;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.util.ResultListener;

import com.threerings.micasa.server.MiCasaServer;

/**
 * A simple simulator server implementation that extends the MiCasa server.
 */
@Singleton
public class SimpleServer extends MiCasaServer
    implements SimulatorServer
{
    public void init (Injector injector, ResultListener<SimulatorServer> obs)
        throws Exception
    {
        init(injector); // do our standard initialization

        if (obs != null) {
            // let the initialization observer know that we've started up
            obs.requestCompleted(this);
        }
    }

    // cause the simulator manager to be created
    @Inject protected SimulatorManager _simmgr;
}
