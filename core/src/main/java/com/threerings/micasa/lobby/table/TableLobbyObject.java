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

package com.threerings.micasa.lobby.table;

import javax.annotation.Generated;
import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.data.Table;
import com.threerings.parlor.data.TableMarshaller;

import com.threerings.micasa.lobby.LobbyObject;

public class TableLobbyObject extends LobbyObject
    implements com.threerings.parlor.data.TableLobbyObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>tableSet</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TABLE_SET = "tableSet";

    /** The field name of the <code>tableService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TABLE_SERVICE = "tableService";
    // AUTO-GENERATED: FIELDS END

    /** A set containing all of the tables being managed by this lobby. */
    public DSet<Table> tableSet = new DSet<Table>();

    /** Handles our table services. */
    public TableMarshaller tableService;

    // from interface TableLobbyObject
    public DSet<Table> getTables ()
    {
        return tableSet;
    }

    // from interface TableLobbyObject
    public void addToTables (Table table)
    {
        addToTableSet(table);
    }

    // from interface TableLobbyObject
    public void updateTables (Table table)
    {
        updateTableSet(table);
    }

    // from interface TableLobbyObject
    public void removeFromTables (Comparable<?> key)
    {
        removeFromTableSet(key);
    }

    // from interface TableLobbyObject
    public TableMarshaller getTableService ()
    {
        return tableService;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the specified entry be added to the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToTableSet (Table elem)
    {
        requestEntryAdd(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>tableSet</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromTableSet (Comparable<?> key)
    {
        requestEntryRemove(TABLE_SET, tableSet, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>tableSet</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateTableSet (Table elem)
    {
        requestEntryUpdate(TABLE_SET, tableSet, elem);
    }

    /**
     * Requests that the <code>tableSet</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTableSet (DSet<Table> value)
    {
        requestAttributeChange(TABLE_SET, value, this.tableSet);
        DSet<Table> clone = (value == null) ? null : value.clone();
        this.tableSet = clone;
    }

    /**
     * Requests that the <code>tableService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTableService (TableMarshaller value)
    {
        TableMarshaller ovalue = this.tableService;
        requestAttributeChange(
            TABLE_SERVICE, value, ovalue);
        this.tableService = value;
    }
    // AUTO-GENERATED: METHODS END
}
