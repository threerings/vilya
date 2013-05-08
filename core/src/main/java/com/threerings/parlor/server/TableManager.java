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

package com.threerings.parlor.server;

import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;
import com.samskivert.util.ListUtil;

import com.google.common.base.Preconditions;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.ChangeListener;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.NamedEvent;
import com.threerings.presents.dobj.ObjectAddedEvent;
import com.threerings.presents.dobj.ObjectDeathListener;
import com.threerings.presents.dobj.ObjectDestroyedEvent;
import com.threerings.presents.dobj.ObjectRemovedEvent;
import com.threerings.presents.dobj.OidListListener;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationManager;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.parlor.client.TableService;
import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.data.TableMarshaller;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.server.GameManager;

import static com.threerings.parlor.Log.log;

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
    public TableManager (RootDObjectManager omgr, InvocationManager invmgr, PlaceRegistry plreg,
                         DObject tableObject)
    {
        _omgr = omgr;
        _invmgr = invmgr;
        _plreg = plreg;

        if (tableObject != null) {
            setTableObject(tableObject);
        }
    }

    /**
     * Initialize the TableLobbyObject. Do not call this more than once.
     */
    public void setTableObject (DObject tableObject)
    {
        Preconditions.checkState((_tlobj == null), "Already got one!");

        // set up our object references
        _tlobj = (TableLobbyObject)tableObject;
        _tlobj.setTableService(_invmgr.registerProvider(this, TableMarshaller.class));
        _dobj = tableObject;

        // if our table is in a "place" add ourselves as a listener so that we can tell if a user
        // leaves the place without leaving their table
        if (_dobj instanceof PlaceObject) {
            _dobj.addListener(_placeListener);
        }
    }

    /**
     * Returns the number of tables being managed currently.
     */
    public int getTableCount ()
    {
        return _tables.size();
    }

    /**
     * This must be called when the table manager is no longer needed.
     */
    public void shutdown ()
    {
        if (_tlobj != null) {
            _invmgr.clearDispatcher(_tlobj.getTableService());
            _tlobj.setTableService(null);
        }
        if (_dobj instanceof PlaceObject) {
            _dobj.removeListener(_placeListener);
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

    /**
     * Allow a player in the first position of a table to boot others.
     */
    public void setAllowBooting (boolean allowBooting)
    {
        _allowBooting = allowBooting;
    }

    /**
     * Creates a table for the specified creator and returns said table.
     *
     * @exception InvocationException thrown if the table could not be created for any reason.
     */
    public Table createTable (BodyObject creator, TableConfig tableConfig, GameConfig config)
        throws InvocationException
    {
        // make sure the caller is not already in a table
        if (_boidMap.containsKey(creator.getOid())) {
            throw new InvocationException(ALREADY_AT_TABLE);
        }

        // create a brand spanking new table
        Table table;
        try {
            table = _tableClass.newInstance();
        } catch (Exception e) {
            log.warning("Table.newInstance() failed", "class", _tableClass, e);
            throw new InvocationException(INTERNAL_ERROR);
        }
        table.init(_dobj.getOid(), tableConfig, config);

        if (table.bodyOids != null && table.bodyOids.length > 0) {
            // stick the creator into the first non-AI position
            int cpos = (config.ais == null) ? 0 : config.ais.length;
            String error = table.setPlayer(cpos, creator);
            if (error != null) {
                log.warning("Unable to add creator to position zero of table!?",
                   "table", table, "creator", creator, "error", error);
                // bail out now and abort the table creation process
                throw new InvocationException(error);
            }

            // make a mapping from the creator to this table
            notePlayerAdded(table, creator);
        }

        // stick the table into our tables tables
        _tables.put(table.tableId, table);

        // if the table has only one seat, start the game immediately
        if (table.shouldBeStarted()) {
            createGame(table);
        }

        // wire our table into our bits
        tableCreated(table);

        return table;
    }

    // from interface TableProvider
    public void createTable (ClientObject caller, TableConfig tableConfig, GameConfig config,
                             TableService.ResultListener listener)
        throws InvocationException
    {
        BodyObject creator = (BodyObject)caller;

        // if we're managing tables in a place, make sure the creator is an occupant of the place
        // in which they are requesting to create a table
        if (_dobj instanceof PlaceObject &&
            !((PlaceObject)_dobj).occupants.contains(creator.getOid())) {
            log.warning("Requested to create a table in a place not occupied by the creator",
                "creator", creator, "ploid", _dobj.getOid());
            throw new InvocationException(INTERNAL_ERROR);
        }

        // if createTable() returns, we're good to go
        listener.requestProcessed(createTable(creator, tableConfig, config).tableId);
    }

    // from interface TableProvider
    public void joinTable (ClientObject caller, int tableId, int position,
                           TableService.InvocationListener listener)
        throws InvocationException
    {
        BodyObject joiner = (BodyObject)caller;

        // make sure the caller is not already in a table
        if (_boidMap.containsKey(joiner.getOid())) {
            throw new InvocationException(ALREADY_AT_TABLE);
        }

        // look the table up
        Table table = _tables.get(tableId);
        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);
        }

        // request that the user be added to the table at that position
        String error = table.setPlayer(position, joiner);
        // if that failed, report the error
        if (error != null) {
            throw new InvocationException(error);
        }

        // if the table is sufficiently full, start the game automatically
        if (table.shouldBeStarted()) {
            createGame(table);
        } else {
            // make a mapping from this player to this table
            notePlayerAdded(table, joiner);
        }

        // update the table in the lobby
        updateTableInLobby(table);

        // there is normally no success response. the client will see
        // themselves show up in the table that they joined
    }

    // from interface TableProvider
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

        // if the table is in play, the user is not allowed to leave (he must leave the game)
        if (table.inPlay()) {
            throw new InvocationException(GAME_ALREADY_STARTED);
        }

        // request that the user be removed from the table
        if (!table.clearPlayer(leaver.getVisibleName())) {
            throw new InvocationException(NOT_AT_TABLE);
        }

        // remove the mapping from this user to the table
        if (null == notePlayerRemoved(leaver.getOid(), leaver)) {
            log.warning("No body to table mapping to clear?",
                "leaver", leaver.who(), "table", table);
        }

        // either update or delete the table depending on whether or not we just removed the last
        // player
        if (table.isEmpty()) {
            purgeTable(table);
        } else {
            updateTableInLobby(table);
        }

        // there is normally no success response. the client will see
        // themselves removed from the table they just left
    }

    // from interface TableProvider
    public void startTableNow (ClientObject caller, int tableId,
                               TableService.InvocationListener listener)
        throws InvocationException
    {
        BodyObject starter = (BodyObject)caller;
        Table table = _tables.get(tableId);
        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);
        } else if (starter.getOid() != table.bodyOids[0]) {
            throw new InvocationException(MUST_BE_CREATOR);
        } else if (!table.mayBeStarted()) {
            throw new InvocationException(INTERNAL_ERROR);
        }
        createGame(table);
    }

    // from interface TableProvider
    public void bootPlayer (ClientObject caller, int tableId, Name target,
                            TableService.InvocationListener listener)
        throws InvocationException
    {
        BodyObject booter = (BodyObject) caller;
        Table table = _tables.get(tableId);

        if (table == null) {
            throw new InvocationException(NO_SUCH_TABLE);
        } else if ( ! _allowBooting) {
            throw new InvocationException(INTERNAL_ERROR);
        }

        int position = ListUtil.indexOf(table.players, target);
        if (position < 0) {
            throw new InvocationException(NOT_AT_TABLE);
        } else if (booter.getOid() != table.bodyOids[0]) {
            throw new InvocationException(MUST_BE_CREATOR);
        } else if (booter.getOid() == table.bodyOids[position]) {
            throw new InvocationException(NO_SELF_BOOT);
        }

        // Remember to keep him banned
        table.addBannedUser(target);

        // Remove the player from the table
        if (notePlayerRemoved(table.bodyOids[position]) == null) {
            log.warning("No body to table mapping to clear?",
               "position", position, "table", table);
        }
        table.clearPlayerPos(position);
        updateTableInLobby(table);
    }

    /**
     * Publishes a newly created table to the lobby object. Can also be overridden by custom
     * managers that want to react to table creation.
     */
    protected void tableCreated (Table table)
    {
        // publish the table in the lobby object if desired
        if (shouldPublish(table)) {
            addTableToLobby(table);
        }
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
            for (int bodyOid : table.bodyOids) {
                if (bodyOid != 0) {
                    notePlayerRemoved(bodyOid);
                }
            }
        }

        // remove the mapping by gameOid
        Table removed = _goidMap.remove(table.gameOid); // no-op if gameOid == 0

        // remove the table from the lobby object (the table may not have been published if a
        // derived class decided it was not worth publishing, see shouldPublish())
        removeTableFromLobby(table.tableId);

        // remove the listener too so we do not get a request later on to update the occupants or
        // unmap this table
        if (removed != null) {
            DObject gameObj = _omgr.getObject(table.gameOid);
            if (gameObj != null) {
                gameObj.removeListener(_gameListener);
            }
        }
    }

    /**
     * Called when a player is added to a table to set up our mappings.
     */
    protected void notePlayerAdded (Table table, BodyObject body)
    {
        _boidMap.put(body.getOid(), table);
        body.addListener(_userListener);
    }

    /**
     * Called when a player leaves the room and we're not sure if the user is still online.
     */
    final protected Table notePlayerRemoved (int playerOid)
    {
        return notePlayerRemoved(playerOid, (BodyObject)_omgr.getObject(playerOid));
    }

    /**
     * Called when a player leaves a table to clear our mappings.
     * @param body will be non-null if the user is still online.
     */
    protected Table notePlayerRemoved (int playerOid, BodyObject body)
    {
        if (body != null) {
            body.removeListener(_userListener);
        }
        return _boidMap.remove(playerOid);
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
            GameManager gmgr = createGameManager(createConfig(table));
            GameObject gobj = (GameObject)gmgr.getPlaceObject();
            gameCreated(table, gobj, gmgr);
            return gobj.getOid();

        } catch (Throwable t) {
            log.warning("Failed to create manager for game", "config", table.config, t);
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
     * Creates a {@link GameManager} using the supplied config. Used by {@link #createGame}, but
     * extracted into a method to allow customization of this process.
     */
    protected GameManager createGameManager (GameConfig config)
        throws InstantiationException, InvocationException
    {
        return (GameManager)_plreg.createPlace(config);
    }

    /**
     * Called when our game has been created, we take this opportunity to clean up the table and
     * transition it to "in play" mode.
     */
    protected void gameCreated (Table table, GameObject gameobj, GameManager gmgr)
    {
        // update the table with the newly created game object
        table.gameOid = gameobj.getOid();

        // add it to the gameOid map
        _goidMap.put(table.gameOid, table);

        // configure the privacy of the game
        gameobj.setIsPrivate(table.tconfig.privateTable);

        if (table.bodyOids != null) {
            // clear the player to table mappings as this game is underway
            for (int bodyOid : table.bodyOids) {
                if (bodyOid != 0) {
                    notePlayerRemoved(bodyOid);
                }
            }
        }

        // add an object death listener to unmap the table when the game finally goes away
        gameobj.addListener(_gameListener);

        // and then update the lobby object that contains the table
        if (shouldPublish(table)) {
            updateTableInLobby(table);
        } else {
            // or remove it if the table is no longer "interesting"
            removeTableFromLobby(table.tableId);
        }
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
            log.warning("Requested to unmap table that wasn't mapped", "gameOid", gameOid);
        }
    }

    /**
     * Called when the occupants in a game change: publishes new info.
     */
    protected void updateOccupants (int gameOid)
    {
        // if we've been shutdown, then we've got nothing to worry about
        if (_tlobj == null) {
            return;
        }

        Table table = _goidMap.get(gameOid);
        if (table == null) {
            log.warning("Unable to find table for running game", "gameOid", gameOid);
            return;
        }

        // update this table's occupants information and update the table
        GameObject gameObj = (GameObject)_omgr.getObject(gameOid);
        table.updateOccupants(gameObj);
        updateTableInLobby(table);
    }

    /**
     * Called when a body is known to have left either the room that contains our tables or logged
     * off of the server.
     */
    protected void bodyLeft (int bodyOid)
    {
        // look up the table to which this player is mapped
        Table pender = notePlayerRemoved(bodyOid);
        if (pender == null) {
            return;
        }

        // remove this player from the table
        if (!pender.clearPlayerByOid(bodyOid)) {
            log.warning("Attempt to remove body from mapped table failed",
                "table", pender, "bodyOid", bodyOid);
            return;
        }

        // update or delete the table depending on whether or not we just removed the last player
        if (pender.isEmpty()) {
            purgeTable(pender);
        } else {
            updateTableInLobby(pender);
        }
    }

    /**
     * Derived classes can override this method to filter certain tables from being published in
     * the lobby object. Such tables are probably unwatchable and unjoinable and thus just take up
     * space in the lobby object for no good reason. This will be checked when the table is first
     * created and when the table's game transitions to in-play.
     */
    protected boolean shouldPublish (Table table)
    {
        return true; // we publish all tables by default
    }

    /**
     * Add the table to the lobby object, <b>after it has already been validated as being
     * publishable</b>.
     */
    protected void addTableToLobby (Table table)
    {
        _tlobj.addToTables(table);
    }

    /**
     * Safely update the table in the lobby, if it's there.
     */
    protected void updateTableInLobby (Table table)
    {
        // the table may not have been published, see shouldPublish()
        if (_tlobj.getTables().containsKey(table.tableId)) {
            _tlobj.updateTables(table);
        }
    }

    /**
     * Safely remove the table from the lobby. Broken out for overriding.
     */
    protected void removeTableFromLobby (Integer tableId)
    {
        if (_tlobj.getTables().containsKey(tableId)) {
            _tlobj.removeFromTables(tableId);
        }
    }

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

    /** Listens to all users who have joined a table, takes care of removing them
     * as necessary. */
    protected class UserListener
        implements AttributeChangeListener, ObjectDeathListener
    {
        // from AttributeChangeListener
        public void attributeChanged (AttributeChangedEvent event) {
            // Remove folks from tables when they disconnect
            if (BodyObject.STATUS.equals(event.getName())) {
                if (event.getByteValue() == OccupantInfo.DISCONNECTED) {
                    bodyLeft(event.getTargetOid());
                }
            }
        }

        // from ObjectDeathListener
        public void objectDestroyed (ObjectDestroyedEvent event) {
            // Remove folks from tables when they die
            bodyLeft(event.getTargetOid());
        }
    } // END: class UserListener

    /** Listens for players leaving the place that contains our tables. */
    protected ChangeListener _placeListener = new OidListListener() {
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

    /** A reference to the distributed object in which we're managing tables. */
    protected DObject _dobj;

    /** A reference to our distributed object casted to a table lobby object. */
    protected TableLobbyObject _tlobj;

    /** The class of table we instantiate. */
    protected Class<? extends Table> _tableClass = Table.class;

    /** The table of pending tables. */
    protected IntMap<Table> _tables = IntMaps.newHashIntMap();

    /** A mapping from body oid to table. */
    protected IntMap<Table> _boidMap = IntMaps.newHashIntMap();

    /** Once a game starts, a mapping from gameOid to table. */
    protected IntMap<Table> _goidMap = IntMaps.newHashIntMap();

    /** A listener that prunes tables after the game dies. */
    protected ChangeListener _gameListener = new GameListener();

    /** A listener that removes users from tables when they're no longer able to play. */
    protected ChangeListener _userListener = new UserListener();

    /** Whether or not tables should support booting. */
    protected boolean _allowBooting = false;

    protected RootDObjectManager _omgr;
    protected InvocationManager _invmgr;
    protected PlaceRegistry _plreg;
}
