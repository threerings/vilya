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

import com.threerings.presents.dobj.ChangeListener;
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
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;

/**
 * A table manager can be used by a place manager to take care of the
 * management of a table matchmaking service in the place managed by the
 * place manager. The place manager need instantiate a table manager and
 * implement the {@link TableManagerProvider} interface to provide access
 * to the table manager for the associated invocation services.
 */
public class TableManager
    implements ParlorCodes, OidListListener
{
    /**
     * Creates a table manager that will work in tandem with the specified
     * place manager to manage a table matchmaking service in this place.
     */
    public TableManager (PlaceManager plmgr)
    {
        // get a reference to our place object
        _plobj = plmgr.getPlaceObject();

        // add ourselves as an oidlist listener to this lobby so that we
        // can tell if a user leaves the lobby without leaving their table
        _plobj.addListener(this);

        // make sure it implements table lobby object
        _tlobj = (TableLobbyObject)_plobj;
    }

    /**
     * Set the subclass of Table that this instance should generate.
     */
    public void setTableClass (Class<? extends Table> tableClass)
    {
        _tableClass = tableClass;
    }

    /**
     * Requests that a new table be created to matchmake the game
     * described by the supplied game config instance. The config instance
     * provided must implement the {@link TableConfig} interface so that
     * the parlor services can determine how to configure the table that
     * will be created.
     *
     * @param creator the body object that will own the new table.
     * @param tableConfig the configuration parameters for the table.
     * @param config the configuration of the game to be created.
     *
     * @return the id of the newly created table.
     *
     * @exception InvocationException thrown if the table creation was
     * not able processed for some reason. The explanation will be
     * provided in the message data of the exception.
     */
    public int createTable (BodyObject creator, TableConfig tableConfig,
            GameConfig config)
        throws InvocationException
    {
        // make sure the creator is an occupant of the lobby in which
        // they are requesting to create a table
        if (!_plobj.occupants.contains(creator.getOid())) {
            Log.warning("Requested to create a table in a lobby not " +
                        "occupied by the creator [creator=" + creator +
                        ", loboid=" + _plobj.getOid() + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // create a brand spanking new table
        Table table;
        try {
            table = _tableClass.newInstance();
        } catch (Exception e) {
            Log.warning("Unable to create a new table instance! " +
                "[tableClass=" + _tableClass + ", error=" + e + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }
        table.init(_plobj.getOid(), tableConfig, config);

        if (table.bodyOids != null && table.bodyOids.length > 0) {
            // stick the creator into the first non-AI position
            int cpos = (config.ais == null) ? 0 : config.ais.length;
            String error = table.setOccupant(cpos, creator);
            if (error != null) {
                Log.warning("Unable to add creator to position zero of " +
                            "table!? [table=" + table + ", creator=" + creator +
                            ", error=" + error + "].");
                // bail out now and abort the table creation process
                throw new InvocationException(error);
            }

            // make a mapping from the creator to this table
            _boidMap.put(creator.getOid(), table);
        }

        // stick the table into the table lobby object
        _tlobj.addToTables(table);

        // also stick it into our tables tables
        _tables.put(table.tableId, table);

        // if the table has only one seat, start the game immediately
        if (table.shouldBeStarted()) {
            int oid = createGame(table);
        }

        // finally let the caller know what the new table id is
        return table.tableId;
    }

    /**
     * Requests that the specified user be added to the specified table at
     * the specified position. If the user successfully joins the table,
     * the function will return normally. If they are not able to join for
     * some reason (the slot is already full, etc.), a {@link
     * InvocationException} will be thrown with a message code that
     * describes the reason for failure. If the user does successfully
     * join, they will be added to the table entry in the tables set in
     * the place object that is hosting the table.
     *
     * @param joiner the body object of the user that wishes to join the
     * table.
     * @param tableId the id of the table to be joined.
     * @param position the position at which to join the table.
     *
     * @exception InvocationException thrown if the joining was not able
     * processed for some reason. The explanation will be provided in the
     * message data of the exception.
     */
    public void joinTable (BodyObject joiner, int tableId, int position)
        throws InvocationException
    {
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
            _boidMap.put(joiner.getOid(), table);
        }

        // update the table in the lobby
        _tlobj.updateTables(table);
    }

    /**
     * Requests that the specified user be removed from the specified
     * table. If the user successfully leaves the table, the function will
     * return normally. If they are not able to leave for some reason
     * (they aren't sitting at the table, etc.), a {@link
     * InvocationException} will be thrown with a message code that
     * describes the reason for failure.
     *
     * @param leaver the body object of the user that wishes to leave the
     * table.
     * @param tableId the id of the table to be left.
     *
     * @exception InvocationException thrown if the leaving was not able
     * processed for some reason. The explanation will be provided in the
     * message data of the exception.
     */
    public void leaveTable (BodyObject leaver, int tableId)
        throws InvocationException
    {
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
        if (_boidMap.remove(leaver.getOid()) == null) {
            Log.warning("No body to table mapping to clear? " +
                        "[leaver=" + leaver + ", table=" + table + "].");
        }

        // either update or delete the table depending on whether or not
        // we just removed the last occupant
        if (table.isEmpty()) {
            purgeTable(table);
        } else {
            _tlobj.updateTables(table);
        }
    }

    /**
     * Requests that the specified table be started now, even if there are
     * some vacant seats. This may only be done if the minimum number of
     * players are already present at the table. By convention, only the
     * player at seat 0 may request to have the game start early, as they
     * are usually the creator.
     *
     * @param requester the body object of the user that wishes to start
     * the table.
     * @param tableId the id of the table to be started.
     *
     * @exception InvocationException thrown if the starting was not able
     * processed for some reason. The explanation will be provided in the
     * message data of the exception.
     */
    public void startTableNow (BodyObject requester, int tableId)
        throws InvocationException
    {
        // look the table up
        Table table = _tables.get(tableId);
        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);

        } else if (requester.getOid() != table.bodyOids[0]) {
            throw new InvocationException(INVALID_TABLE_POSITION);

        } else if (!table.mayBeStarted()) {
            throw new InvocationException(INTERNAL_ERROR);
        }

        // I guess we're ready to go!
        createGame(table);
    }

    /**
     * Removes the table from all of our internal tables and from its
     * lobby's distributed object.
     */
    protected void purgeTable (Table table)
    {
        // remove the table from our tables table
        _tables.remove(table.tableId);

        // clear out all matching entries in the boid map
        if (table.bodyOids != null) {
            for (int ii = 0; ii < table.bodyOids.length; ii++) {
                _boidMap.remove(table.bodyOids[ii]);
            }
        }

        // remove the mapping by gameOid
        _goidMap.remove(table.gameOid); // no-op if gameOid == 0

        // remove the table from the lobby object
        _tlobj.removeFromTables(table.tableId);
    }

    /**
     * Called when we're ready to create a game (either an invitation has
     * been accepted or a table is ready to start. If there is a problem
     * creating the game manager, it should be reported in the logs.
     *
     * @return the oid of the newly-created game.
     */
    protected int createGame (final Table table)
        throws InvocationException
    {
        // fill the players array into the game config
        table.config.players = table.getPlayers();

        try {
            PlaceManager pmgr = CrowdServer.plreg.createPlace(table.config);
            GameObject gobj = (GameObject) pmgr.getPlaceObject();
            gameCreated(table, gobj);
            return gobj.getOid();

        } catch (Throwable t) {
            Log.warning("Failed to create manager for game " +
                        "[config=" + table.config + "]: " + t);
            throw new InvocationException(INTERNAL_ERROR);
        }
    }

    /**
     * Called when our game has been created, we take this opportunity to clean
     * up the table and transition it to "in play" mode.
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
            for (int i = 0; i < table.bodyOids.length; i++) {
                _boidMap.remove(table.bodyOids[i]);
            }
        }

        // add an object death listener to unmap the table when the game
        // finally goes away
        gameobj.addListener(_gameListener);

        // and then update the lobby object that contains the table
        _tlobj.updateTables(table);
    }

    /**
     * Called when a game created from a table managed by this table
     * manager was destroyed. We remove the associated table.
     */
    protected void unmapTable (int gameOid)
    {
        Table table = _goidMap.get(gameOid);
        if (table != null) {
            purgeTable(table);

        } else {
            Log.warning("Requested to unmap table that wasn't mapped " +
                        "[gameOid=" + gameOid + "].");
        }
    }

    /**
     * Called when the occupants in a game change: publish new info.
     */
    protected void updateOccupants (int gameOid)
    {
        Table table = _goidMap.get(gameOid);
        if (table == null) {
            Log.warning("Unable to find table for running game " +
                        "[gameOid=" + gameOid + "].");
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

    // documentation inherited
    public void objectAdded (ObjectAddedEvent event)
    {
        // nothing doing
    }

    // documentation inherited
    public void objectRemoved (ObjectRemovedEvent event)
    {
        // if an occupant departed, see if they are in a pending table
        if (!event.getName().equals(PlaceObject.OCCUPANTS)) {
            return;
        }

        // look up the table to which this occupant is mapped
        int bodyOid = event.getOid();
        Table pender = _boidMap.remove(bodyOid);
        if (pender == null) {
            return;
        }

        // remove this occupant from the table
        if (!pender.clearOccupantByOid(bodyOid)) {
            Log.warning("Attempt to remove body from mapped table failed " +
                        "[table=" + pender + ", bodyOid=" + bodyOid + "].");
            return;
        }

        // either update or delete the table depending on whether or not
        // we just removed the last occupant
        if (pender.isEmpty()) {
            purgeTable(pender);
        } else {
            _tlobj.updateTables(pender);
        }
    }

    /** A reference to the place object in which we're managing tables. */
    protected PlaceObject _plobj;

    /** The class of table we instantiate. */
    protected Class<? extends Table> _tableClass = Table.class;

    /** A reference to our place object casted to a table lobby object. */
    protected TableLobbyObject _tlobj;

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

        /**
         * Check to see if the set event causes us to update the table.
         */
        protected void maybeCheckOccupants (NamedEvent event) {
            if (GameObject.OCCUPANTS.equals(event.getName())) {
                updateOccupants(event.getTargetOid());
            }
        }
    } // END: class GameDeathListener

    /** A listener that prunes tables after the game dies. */
    protected ChangeListener _gameListener = new GameListener();
}
