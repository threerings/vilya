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

import java.util.List;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.threerings.micasa.simulator.data.SimulatorMarshaller;
import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.ClientManager;
import com.threerings.presents.server.ClientResolutionListener;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.LocationManager;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.game.data.GameAI;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;

import static com.threerings.micasa.Log.log;

/**
 * The simulator manager is responsible for handling the simulator services on the server side.
 */
@Singleton
public class SimulatorManager
{
    @Inject public SimulatorManager (InvocationManager invmgr)
    {
        // register our simulator provider
        SimulatorProvider sprov = new SimulatorProvider(this);
        invmgr.registerProvider(sprov, SimulatorMarshaller.class, InvocationCodes.GLOBAL_GROUP);
    }

    /**
     * Creates a game along with the specified number of simulant players and forcibly moves all
     * players into the game room.
     */
    public void createGame (BodyObject source, GameConfig config, String simClass, int playerCount)
    {
        new CreateGameTask(source, config, simClass, playerCount);
    }

    public class CreateGameTask
    {
        public CreateGameTask (BodyObject source, GameConfig config, String simClass,
                               int playerCount) {
            // save off game request info
            _source = source;
            _config = config;
            _simClass = simClass;
            _playerCount = playerCount;

            try {
                // create the game manager and begin its initialization process. the game manager
                // will take care of notifying the players that the game has been created

                // configure the game config with the player names
                config.players = new Name[_playerCount];
                config.players[0] = _source.getVisibleName();
                for (int ii = 1; ii < _playerCount; ii++) {
                    config.players[ii] = new Name("simulant" + ii);
                }
                _gmgr = (GameManager)_plreg.createPlace(config);

            } catch (Exception e) {
                log.warning("Unable to create game manager", "e", e, e);
                return;
            }

            // cast the place to the game object for the game we're creating
            _gobj = (GameObject)_gmgr.getPlaceObject();

            // determine the AI player skill level
            byte skill;
            try {
                skill = Byte.parseByte(System.getProperty("skill"));
            } catch (NumberFormatException nfe) {
                skill = DEFAULT_SKILL;
            }

            for (int ii = 1; ii < _playerCount; ii++) {
                // mark all simulants as AI players
                _gmgr.setAI(ii, new GameAI(0, skill));
            }

            // resolve the simulant body objects
            ClientResolutionListener listener = new ClientResolutionListener() {
                public void clientResolved (Name username, ClientObject clobj) {
                    // hold onto the body object for later game creation
                    _sims.add(clobj);
                    // create the game if we've received all body objects
                    if (_sims.size() == (_playerCount - 1)) {
                        createSimulants();
                    }
                }
                public void resolutionFailed (Name username, Exception cause) {
                    log.warning("Unable to create simulant body object", "error", cause);
                }
            };

            // resolve client objects for all of our simulants
            for (int ii = 1; ii < _playerCount; ii++) {
                Name username = new Name("simulant" + ii);
                _clmgr.resolveClientObject(username, listener);
            }
        }

        /**
         * Called when all simulant body objects are present and the simulants are ready to be
         * created.
         */
        protected void createSimulants () {
            // finish setting up the simulants
            for (int ii = 1; ii < _playerCount; ii++) {
                // create the simulant object
                Simulant sim;
                try {
                    sim = (Simulant)Class.forName(_simClass).newInstance();
                } catch (Exception e) {
                    log.warning("Unable to create simulant", "class", _simClass);
                    return;
                }

                // give the simulant its body
                BodyObject bobj = (BodyObject)_sims.get(ii - 1);
                sim.init(bobj, _config, _gmgr, _omgr);

                // give the simulant a chance to engage in place antics
                sim.willEnterPlace(_gobj);

                // move the simulant into the game room since they have no location director to
                // move them automagically
                try {
                    _locman.moveTo(bobj, _gobj.getOid());
                } catch (Exception e) {
                    log.warning("Failed to move simulant into room", "e", e);
                    return;
                }
            }
        }

        /** The simulant body objects. */
        protected List<ClientObject> _sims = Lists.newArrayList();

        /** The game object for the game being created. */
        protected GameObject _gobj;

        /** The game manager for the game being created. */
        protected GameManager _gmgr;

        /** The number of players in the game. */
        protected int _playerCount;

        /** The simulant class instantiated on game creation. */
        protected String _simClass;

        /** The game config object. */
        protected GameConfig _config;

        /** The body object of the player requesting the game creation. */
        protected BodyObject _source;
    }

    // needed for general operation
    @Inject protected PlaceRegistry _plreg;
    @Inject protected ClientManager _clmgr;
    @Inject protected RootDObjectManager _omgr;
    @Inject protected LocationManager _locman;

    /** The default skill level for AI players. */
    protected static final byte DEFAULT_SKILL = 50;
}
