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

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationProvider;

import com.threerings.crowd.data.BodyObject;

import com.threerings.parlor.game.data.GameConfig;

import static com.threerings.micasa.Log.log;

/**
 * The simulator provider handles game creation requests on the server
 * side, passing them off to the {@link SimulatorManager}.
 */
public class SimulatorProvider
    implements InvocationProvider
{
    /**
     * Constructs a simulator provider.
     */
    public SimulatorProvider (SimulatorManager simmgr)
    {
        _simmgr = simmgr;
    }

    /**
     * Processes a request from the client to create a new game.
     */
    public void createGame (ClientObject caller, GameConfig config,
                            String simClass, int playerCount)
    {
        log.info("handleCreateGameRequest [caller=" + caller.who() +
                 ", config=" + config + ", simClass=" + simClass +
                 ", playerCount=" + playerCount + "].");

        _simmgr.createGame((BodyObject)caller, config, simClass, playerCount);
    }

    /** The simulator manager. */
    protected SimulatorManager _simmgr;
}
