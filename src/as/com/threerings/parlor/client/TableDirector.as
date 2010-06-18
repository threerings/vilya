//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.client {

import com.threerings.util.ArrayUtil;
import com.threerings.util.Log;
import com.threerings.util.Name;
import com.threerings.util.ObserverList;
import com.threerings.util.Util;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.InvocationService_ResultListener;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.EntryAddedEvent;
import com.threerings.presents.dobj.EntryRemovedEvent;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.TableLobbyObject;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * As tables are created and managed within the scope of a place (a lobby), we want to fold the
 * table management functionality into the standard hierarchy of place controllers that deal with
 * place-related functionality on the client. Thus, instead of forcing places that expect to have
 * tables to extend a <code>TableLobbyController</code> or something similar, we instead provide
 * the table director which can be instantiated by the place controller (or specific table related
 * views) to handle the table matchmaking services.
 *
 * <p> Entites that do so, will need to implement the {@link TableObserver} interface so that the
 * table director can notify them when table related things happen.
 *
 * <p> The table services expect that the place object being used as a lobby in which the table
 * matchmaking takes place implements the {@link TableLobbyObject} interface.
 */
public class TableDirector extends BasicDirector
    implements SetListener, InvocationService_ResultListener
{
    /**
     * Creates a new table director to manage tables with the specified observer which will receive
     * callbacks when interesting table related things happen.
     *
     * @param ctx the parlor context in use by the client.
     * @param tableField the field name of the distributed set that contains the tables we will be
     * managing.
     * @param bundle the message bundle to use when reporting errors.
     */
    public function TableDirector (
        ctx :ParlorContext, tableField :String, errorBundle :String = null)
    {
        super(ctx);

        // keep track of this stuff
        _pctx = ctx;
        _tableField = tableField;
        _errorBundle = errorBundle;
    }

    /**
     * This must be called by the entity that uses the table director when the using entity
     * prepares to enter and display a "place".
     */
    public function setTableObject (tlobj :DObject) :void
    {
        // the distributed object should be a TableLobbyObject
        _tlobj = TableLobbyObject(tlobj);
        // listen for table set updates
        tlobj.addListener(this);
    }

    /**
     * This must be called by the entity that uses the table director when the using entity has
     * left and is done displaying the "place".
     */
    public function clearTableObject () :void
    {
        // remove our listenership and clear out
        if (_tlobj != null) {
            (_tlobj as DObject).removeListener(this);
            _tlobj = null;
            _ourTable = null;
        }
    }

    /**
     * Requests that the specified observer be added to the list of observers that are notified
     * of table updates
     */
    public function addTableObserver (observer :TableObserver) :void
    {
        _tableObservers.add(observer);
    }

    /**
     * Requests that the specified observer be removed from to the list of observers that are
     * notified of table updates
     */
    public function removeTableObserver (observer :TableObserver) :void
    {
        _tableObservers.remove(observer);
    }

    /**
     * Requests that the specified observer be added to the list of observers that are notified
     * when this client sits down at or stands up from a table.
     */
    public function addSeatednessObserver (observer :SeatednessObserver) :void
    {
        _seatedObservers.add(observer);
    }

    /**
     * Requests that the specified observer be removed from to the list of observers that are
     * notified when this client sits down at or stands up from a table.
     */
    public function removeSeatednessObserver (observer :SeatednessObserver) :void
    {
        _seatedObservers.remove(observer);
    }

    /**
     * Returns true if this client is currently seated at a table, false if they are not.
     */
    public function isSeated () :Boolean
    {
        return (_ourTable != null);
    }

    /**
     * Returns the table this client is currently seated at, or null.
     */
    public function getSeatedTable () :Table
    {
        return _ourTable;
    }

    /**
     * By default, failure is handled by reporting feedback via the chat director. Clients that
     * require custom error handling can provide a function with the signature:
     * <pre>
     * function (cause :String) :void
     * </pre>
     */
    public function setFailureHandler (handler :Function) :void
    {
        _failureHandler = handler;
    }

    /**
     * Sends a request to create a table with the specified game configuration. This user will
     * become the owner of this table and will be added to the first position in the table. The
     * response will be communicated via the {@link TableObserver} interface.
     */
    public function createTable (tableConfig :TableConfig, config :GameConfig) :void
    {
        // if we're already in a table, refuse the request
        if (_ourTable != null) {
            log.warning("Ignoring request to create table as we're already in a table",
                "table", _ourTable);
            return;
        }

        // make sure we're currently in a place
        if (_tlobj == null) {
            log.warning("Requested to create a table but we're not currently in a place",
                "config", config);
            return;
        }

        // go ahead and issue the create request
        _tlobj.getTableService().createTable(tableConfig, config, this);
    }

    /**
     * Sends a request to join the specified table at the specified position. The response will be
     * communicated via the {@link TableObserver} interface.
     */
    public function joinTable (tableId :int, position :int) :void
    {
        // if we're already in a table, refuse the request
        if (_ourTable != null) {
            log.warning("Ignoring request to join table as we're already in a table",
                "table", _ourTable);
            return;
        }

        // make sure we're currently in a place
        if (_tlobj == null) {
            log.warning("Requested to join a table but we're not currently in a place",
                "tableId", tableId);
            return;
        }

        // issue the join request
        log.info("Joining table", "tid", tableId, "pos", position);
        _tlobj.getTableService().joinTable(tableId, position, this);
    }

    /**
     * Sends a request to leave the specified table at which we are presumably seated. The response
     * will be communicated via the {@link TableObserver} interface.
     */
    public function leaveTable (tableId :int) :void
    {
        // make sure we're currently in a place
        if (_tlobj == null) {
            log.warning("Requested to leave a table but we're not currently in a place",
                "tableId", tableId);
            return;
        }

        // issue the leave request
        _tlobj.getTableService().leaveTable(tableId, this);
    }

    /**
     * Sends a request to have the specified table start now, even if all the seats have not yet
     * been filled.
     */
    public function startTableNow (tableId :int) :void
    {
        if (_tlobj == null) {
            log.warning("Requested to start a table but we're not currently in a place",
                "tableId", tableId);
            return;
        }

        _tlobj.getTableService().startTableNow(tableId, this);
    }

    /**
     * Sends a request to boot a player from a table.
     */
    public function bootPlayer (tableId :int, target :Name) :void
    {
        if (_tlobj == null) {
            log.warning("Requesting to boot a player from a table we're not currently in",
                "tableId", tableId, "target", target);
            return;
        }

        _tlobj.getTableService().bootPlayer(tableId, target, this);
    }

    // documentation inherited
    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);
        _tlobj = null;
        _ourTable = null;
    }

    // documentation inherited
    override protected function fetchServices (client :Client) :void
    {
    }

    // documentation inherited
    public function entryAdded (event :EntryAddedEvent) :void
    {
        if (event.getName() == _tableField) {
            var table :Table = (event.getEntry() as Table);
            // check to see if we just joined a table
            checkSeatedness(table);
            // now let the observers know what's up
            _tableObservers.apply(function (to :TableObserver) :void {
                to.tableAdded(table);
            });
        }
    }

    // documentation inherited
    public function entryUpdated (event :EntryUpdatedEvent) :void
    {
        if (event.getName() == _tableField) {
            var table :Table = (event.getEntry() as Table);
            // check to see if we just joined or left a table
            checkSeatedness(table);
            // now let the observers know what's up
            _tableObservers.apply(function (to :TableObserver) :void {
                to.tableUpdated(table);
            });
        }
    }

    // documentation inherited
    public function entryRemoved (event :EntryRemovedEvent) :void
    {
        if (event.getName() == _tableField) {
            var tableId :int = (event.getKey() as int);
            // check to see if our table just disappeared
            if (_ourTable != null && tableId == _ourTable.tableId) {
                _ourTable = null;
                notifySeatedness(false);
            }
            // now let the observer know what's up
            _tableObservers.apply(function (to :TableObserver) :void {
                to.tableRemoved(tableId);
            });
        }
    }

    // documentation inherited from interface
    public function requestProcessed (result :Object) :void
    {
        var tableId :int = int(result);
        if (_tlobj == null) {
            // we've left, it's none of our concern anymore
            log.info("Table created, but no lobby.", "tableId", tableId);
            return;
        }

        var table :Table = (_tlobj.getTables().get(tableId) as Table);
        if (table == null) {
            log.warning("Table created, but where is it?", "tableId", tableId);
            return;
        }

        // All this to check to see if we created a party game (and should now enter).
        if (table.gameOid != -1 && (table.players.length == 0)) {
            // let's boogie!
            _pctx.getParlorDirector().gameIsReady(table.gameOid);
        }
    }

    // documentation inherited from interface
    public function requestFailed (reason :String) :void
    {
        if (_failureHandler != null) {
            _failureHandler(reason);
        } else {
            _pctx.getChatDirector().displayFeedback(_errorBundle, reason);
        }
    }

    /**
     * Checks to see if we're a member of this table and notes it as our table, if so.
     */
    protected function checkSeatedness (table :Table) :void
    {
        var oldTable :Table = _ourTable;

        // if this is the same table as our table, clear out our table reference and allow it to be
        // added back if we are still in the table
        if (table.equals(_ourTable)) {
            _ourTable = null;
        }

        // look for our username in the players array
        var self :BodyObject = (_pctx.getClient().getClientObject() as BodyObject);
        if (ArrayUtil.contains(table.players, self.getVisibleName())) {
            _ourTable = table;
        }

        // if nothing changed, bail now
        if (Util.equals(oldTable, _ourTable)) {
            return;
        }

        // otherwise notify the observers
        notifySeatedness(_ourTable != null);
    }

    /**
     * Notifies the seatedness observers of a seatedness change.
     */
    protected function notifySeatedness (isSeated :Boolean) :void
    {
        _seatedObservers.apply(function (so :SeatednessObserver) :void {
            so.seatednessDidChange(isSeated);
        });
    }

    /** A context by which we can access necessary client services. */
    protected var _pctx :ParlorContext;

    /** The place object, cast as a TableLobbyObject. */
    protected var _tlobj :TableLobbyObject;

    /** The field name of the distributed set that contains our tables. */
    protected var _tableField :String;

    /** The message bundle to use when reporting errors. */
    protected var _errorBundle :String;

    /** The table of which we are a member if any. */
    protected var _ourTable :Table;

    /** An array of entities that want to hear about table updates. */
    protected var _tableObservers :ObserverList = new ObserverList();

    /** An array of entities that want to hear about when we stand up or sit down. */
    protected var _seatedObservers :ObserverList = new ObserverList();

    /** A custom failure handler, or null. */
    protected var _failureHandler :Function;

    private static const log :Log = Log.getLog(TableDirector);
}
}
