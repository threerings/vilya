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

package com.threerings.parlor.data;

import com.threerings.presents.dobj.DSet;

/**
 * This interface must be implemented by the place object used by a lobby
 * that wishes to make use of the table services.
 */
public interface TableLobbyObject
{
    /**
     * Returns a reference to the distributed set instance that will be
     * holding the tables.
     */
    DSet<Table> getTables ();

    /**
     * Adds the supplied table instance to the tables set (using the
     * appropriate distributed object mechanisms).
     */
    void addToTables (Table table);

    /**
     * Updates the value of the specified table instance in the tables
     * distributed set (using the appropriate distributed object
     * mechanisms).
     */
    void updateTables (Table table);

    /**
     * Removes the table instance that matches the specified key from the
     * tables set (using the appropriate distributed object mechanisms).
     */
    void removeFromTables (Comparable<?> key);

    /**
     * Returns a reference to the table service configured in this object.
     */
    TableMarshaller getTableService ();

    /**
     * Configures the table service that clients should use to communicate table requests back to
     * the table manager handling these tables.
     */
    void setTableService (TableMarshaller service);
}
