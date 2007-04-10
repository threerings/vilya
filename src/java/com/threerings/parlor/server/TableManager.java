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

package com.threerings.parlor.server;

import java.util.HashMap;
import java.util.Iterator;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;
import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.NamedEvent;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;
import com.threerings.presents.dobj.OidListListener;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsServer;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.Log;
import com.threerings.parlor.client.TableService;
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.data.TableMarshaller;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;

/**
 * A table manager can be used by a place manager (or other entity) to take care of the management
 * of a table matchmaking service on a particular distributed object.
 */
public class TableManager
    implements ParlorCodes, TableProvider
{
    /**
     * Creates a table manager that will manage tables in the supplied distributed object (which
     * must implement {@link TableLobbyObject}.
     */
    public TableManager (DObject tableObject)
    {
        // set up our object references
        _tlobj = (TableLobbyObject)tableObject;
        _tlobj.setTableService(
            (TableMarshaller)CrowdServer.invmgr.registerDispatcher(new TableDispatcher(this)));
        _dobj = tableObject;

        // if our table is in a "place" add ourselves as a listener so that we can tell if a user
        // leaves the place without leaving their table
        if (_dobj instanceof PlaceObject) {
            _dobj.addListener(_leaveListener);
        }
    }

    /**
     * This must be called when the table manager is no longer needed.
     */
    public void shutdown ()
    {
        if (_tlobj != null) {
            CrowdServer.invmgr.clearDispatcher(_tlobj.getTableService());
            _tlobj.setTableService(null);
        }
        if (_dobj instanceof PlaceObject) {
            _dobj.removeListener(_leaveListener);
        }
        _tlobj = null;
        _dobj = null;
    }

    /**
     * Set the subclass of Table that this instance should generate.
     */
    public void setTableClass (Class<? extends Table> tableClass)
    {
        _tableClass = tableClass;
    }

    // from interface ParlorProvider
    public void createTable (ClientObject caller, TableConfig tableConfig, GameConfig config,
                             TableService.ResultListener listener)
        throws InvocationException
    {
        BodyObject creator = (BodyObject)caller;

        // if we're managing tables in a place, make sure the creator is an occupant of the place
        // in which they are requesting to create a table
        if (_dobj instanceof PlaceObject &&
            !((PlaceObject)_dobj).occupants.contains(creator.getOid())) {
            Log.warning("Requested to create a table in a place not occupied by the creator " +
                        "[creator=" + creator + ", ploid=" + _dobj.getOid() + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // create a brand spanking new table
        Table table;
        try {
            table = _tableClass.newInstance();
        } catch (Exception e) {
            Log.warning("Unable to create a new table instance! [tableClass=" + _tableClass +
                        ", error=" + e + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }
        table.init(_dobj.getOid(), tableConfig, config);

        if (table.bodyOids != null && table.bodyOids.length > 0) {
            // stick the creator into the first non-AI position
            int cpos = (config.ais == null) ? 0 : config.ais.length;
            String error = table.setOccupant(cpos, creator);
            if (error != null) {
                Log.warning("Unable to add creator to position zero of table!? [table=" + table +
                            ", creator=" + creator + ", error=" + error + "].");
                // bail out now and abort the table creation process
                throw new InvocationException(error);
            }

            // make a mapping from the creator to this table
            notePlayerAdded(table, creator.getOid());
        }

        // stick the table into the table lobby object
        _tlobj.addToTables(table);

        // also stick it into our tables tables
        _tables.put(table.tableId, table);

        // if the table has only one seat, start the game immediately
        if (table.shouldBeStarted()) {
            int oid = createGame(table);
        }

        listener.requestProcessed(table.tableId);
    }

    // from interface ParlorProvider
    public void joinTable (ClientObject caller, int tableId, int position,
                           TableService.InvocationListener listener)
        throws InvocationException
    {
        BodyObject joiner = (BodyObject)caller;

        // look the table up
        Table table = _tables.get(tableId);
        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);
        }

        // request that the user be added to the table at that position
        String error = table.setOccupant(position, joiner);
        // if that failed, report the error
        if (error != null) {
            throw new InvocationException(error);
        }

        // if the table is sufficiently full, start the game automatically
        if (table.shouldBeStarted()) {
            createGame(table);
        } else {
            // make a mapping from this occupant to this table
            notePlayerAdded(table, joiner.getOid());
        }

        // update the table in the lobby
        _tlobj.updateTables(table);

        // there is normally no success response. the client will see
        // themselves show up in the table that they joined
    }

    // from interface ParlorProvider
    public void leaveTable (ClientObject caller, int tableId,
                            TableService.InvocationListener listener)
        throws InvocationException
    {
        BodyObject leaver = (BodyObject)caller;

        // look the table up
        Table table = _tables.get(tableId);
        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);
        }

        // request that the user be removed from the table
        if (!table.clearOccupant(leaver.getVisibleName())) {
            throw new InvocationException(NOT_AT_TABLE);
        }

        // remove the mapping from this user to the table
        if (!notePlayerRemoved(table, leaver.getOid())) {
            Log.warning("No body to table mapping to clear? [leaver=" + leaver +
                        ", table=" + table + "].");
        }

        // either update or delete the table depending on whether or not we just removed the last
        // occupant
        if (table.isEmpty()) {
            purgeTable(table);
        } else {
            _tlobj.updateTables(table);
        }

        // there is normally no success response. the client will see
        // themselves removed from the table they just left
    }

    // from interface ParlorProvider
    public void startTableNow (ClientObject caller, int tableId,
                               TableService.InvocationListener listener)
        throws InvocationException
    {
        BodyObject starter = (BodyObject)caller;
        Table table = _tables.get(tableId);
        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);
        } else if (starter.getOid() != table.bodyOids[0]) {
            throw new InvocationException(INVALID_TABLE_POSITION);
        } else if (!table.mayBeStarted()) {
            throw new InvocationException(INTERNAL_ERROR);
        }
        createGame(table);
    }

    /**
     * Removes the table from all of our internal tables and from its lobby's distributed object.
     */
    protected void purgeTable (Table table)
    {
        // remove the table from our tables table
        _tables.remove(table.tableId);

        // clear out all matching entries in the boid map
        if (table.bodyOids != null) {
            for (int ii = 0; ii < table.bodyOids.length; ii++) {
                if (table.bodyOids[ii] > 0) {
                    notePlayerRemoved(table, table.bodyOids[ii]);
                }
            }
        }

        // remove the mapping by gameOid
        _goidMap.remove(table.gameOid); // no-op if gameOid == 0

        // remove the table from the lobby object
        _tlobj.removeFromTables(table.tableId);
    }

    /**
     * Called when a player is added to a table to set up our mappings.
     */
    protected void notePlayerAdded (Table table, int playerOid)
    {
        _boidMap.put(playerOid, table);
        // if we're in a place, we're done
        if (_dobj instanceof PlaceObject) {
            return;
        }
        // if not, listen to their BodyObject for death so that we remove them if they logoff
        BodyObject body = (BodyObject)PresentsServer.omgr.getObject(playerOid);
        if (body != null) {
            body.addListener(_deathListener);
        }
    }

    /**
     * Called when a player leaves a table to clear our mappings.
     */
    protected boolean notePlayerRemoved (Table table, int playerOid)
    {
        boolean removed = (_boidMap.remove(playerOid) != null);
        if (!(_dobj instanceof PlaceObject)) {
            BodyObject body = (BodyObject)PresentsServer.omgr.getObject(playerOid);
            if (body != null) {
                body.removeListener(_deathListener);
            }
        }
        return removed;
    }

    /**
     * Called when we're ready to create a game (either an invitation has been accepted or a table
     * is ready to start. If there is a problem creating the game manager, it should be reported in
     * the logs.
     *
     * @return the oid of the newly-created game.
     */
    protected int createGame (final Table table)
        throws InvocationException
    {
        try {
            PlaceManager pmgr = CrowdServer.plreg.createPlace(createConfig(table));
            GameObject gobj = (GameObject) pmgr.getPlaceObject();
            gameCreated(table, gobj);
            return gobj.getOid();

        } catch (Throwable t) {
            Log.warning("Failed to create manager for game [config=" + table.config + "]: " + t);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /**
     * This method should validate that the (client provided) configuration in the supplied {@link
     * Table} object is valid and fill in any extra information that is the purview of the server.
     */
    protected GameConfig createConfig (Table table)
    {
        // fill the players array into the game config
        table.config.players = table.getPlayers();
        // we just trust the rest by default, yay!
        return table.config;
    }

    /**
     * Called when our game has been created, we take this opportunity to clean up the table and
     * transition it to "in play" mode.
     */
    protected void gameCreated (Table table, GameObject gameobj)
    {
        // update the table with the newly created game object
        table.gameOid = gameobj.getOid();

        // add it to the gameOid map
        _goidMap.put(table.gameOid, table);

        // configure the privacy of the game
        gameobj.setIsPrivate(table.tconfig.privateTable);

        if (table.bodyOids != null) {
            // clear the occupant to table mappings as this game is underway
            for (int ii = 0; ii < table.bodyOids.length; ii++) {
                if (table.bodyOids[ii] > 0) {
                    notePlayerRemoved(table, table.bodyOids[ii]);
                }
            }
        }

        // add an object death listener to unmap the table when the game finally goes away
        gameobj.addListener(_gameListener);

        // and then update the lobby object that contains the table
        _tlobj.updateTables(table);
    }

    /**
     * Called when a game created from a table managed by this table manager was destroyed. We
     * remove the associated table.
     */
    protected void unmapTable (int gameOid)
    {
        // if we've been shutdown, then we've got nothing to worry about
        if (_tlobj == null) {
            return;
        }

        Table table = _goidMap.get(gameOid);
        if (table != null) {
            purgeTable(table);
        } else {
            Log.warning("Requested to unmap table that wasn't mapped [gameOid=" + gameOid + "].");
        }
    }

    /**
     * Called when the occupants in a game change: publish new info.
     */
    protected void updateOccupants (int gameOid)
    {
        // if we've been shutdown, then we've got nothing to worry about
        if (_tlobj == null) {
            return;
        }

        Table table = _goidMap.get(gameOid);
        if (table == null) {
            Log.warning("Unable to find table for running game [gameOid=" + gameOid + "].");
            return;
        }

        GameObject gameObj = (GameObject) PresentsServer.omgr.getObject(gameOid);
        table.watcherCount = (short) gameObj.occupants.size();

        // TODO: this will become more complicated
        // As we separate watchers and players

        // TODO: for SEATED_CONTINUOUS, we will probably be showing the
        // folks in the game...

        // finally, update the table
        _tlobj.updateTables(table);
    }

    /**
     * Called when a body is known to have left either the room that contains our tables or logged
     * off of the server.
     */
    protected void bodyLeft (int bodyOid)
    {
        // look up the table to which this occupant is mapped
        Table pender = _boidMap.remove(bodyOid);
        if (pender == null) {
            return;
        }

        // remove this occupant from the table
        if (!pender.clearOccupantByOid(bodyOid)) {
            Log.warning("Attempt to remove body from mapped table failed [table=" + pender +
                        ", bodyOid=" + bodyOid + "].");
            return;
        }

        // either update or delete the table depending on whether or not we just removed the last
        // occupant
        if (pender.isEmpty()) {
            purgeTable(pender);
        } else {
            _tlobj.updateTables(pender);
        }
    }

    /** Listens for players leaving the place that contains our tables. */
    protected OidListListener _leaveListener = new OidListListener() {
        public void objectAdded (ObjectAddedEvent event) {
            // nothing doing
        }
        public void objectRemoved (ObjectRemovedEvent event) {
            // if an occupant departed, see if they are in a pending table
            if (event.getName().equals(PlaceObject.OCCUPANTS)) {
                bodyLeft(event.getOid());
            }
        }
    };

    /** Listens for the death of body objects that are in tables. This is used when we are not
     * managing tables in a place but rather across the whole server. */
    protected ObjectDeathListener _deathListener = new ObjectDeathListener() {
        public void objectDestroyed (ObjectDestroyedEvent event) {
            bodyLeft(event.getTargetOid());
        }
    };

    /** A reference to the distributed object in which we're managing tables. */
    protected DObject _dobj;

    /** A reference to our distributed object casted to a table lobby object. */
    protected TableLobbyObject _tlobj;

    /** The class of table we instantiate. */
    protected Class<? extends Table> _tableClass = Table.class;

    /** The table of pending tables. */
    protected HashIntMap<Table> _tables = new HashIntMap<Table>();

    /** A mapping from body oid to table. */
    protected HashIntMap<Table> _boidMap = new HashIntMap<Table>();

    /** Once a game starts, a mapping from gameOid to table. */
    protected HashIntMap<Table> _goidMap = new HashIntMap<Table>();

    /** Listens to all games and updates the table objects as necessary. */
    protected class GameListener
        implements ObjectDeathListener, OidListListener
    {
        // from ObjectDeathListener
        public void objectDestroyed (ObjectDestroyedEvent event) {
            unmapTable(event.getTargetOid());
        }

        // from OidListListener
        public void objectAdded (ObjectAddedEvent event) {
            maybeCheckOccupants(event);
        }

        // from OidListListener
        public void objectRemoved (ObjectRemovedEvent event) {
            maybeCheckOccupants(event);
        }

        /** Check to see if the set event causes us to update the table. */
        protected void maybeCheckOccupants (NamedEvent event) {
            if (GameObject.OCCUPANTS.equals(event.getName())) {
                updateOccupants(event.getTargetOid());
            }
        }
    } // END: class GameDeathListener

    /** A listener that prunes tables after the game dies. */
    protected GameListener _gameListener = new GameListener();
}
