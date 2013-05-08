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

package com.threerings.parlor.client;

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;

import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides table lobbying services.
 */
public interface TableService extends InvocationService
{
    /**
     * Requests that a new table be created.
     *
     * @param tableConfig the table configuration parameters.
     * @param config the game config for the game to be matchmade by the table.
     * @param listener will receive and process the response.
     */
    public void createTable (TableConfig tableConfig, GameConfig config,
                             ResultListener listener);

    /**
     * Requests that the current user be added to the specified table at the specified position.
     *
     * @param tableId the unique id of the table to which this user wishes to be added.
     * @param position the position at the table to which this user desires to be added.
     * @param listener will receive and process the response.
     */
    public void joinTable (int tableId, int position, InvocationListener listener);

    /**
     * Requests that the current user be removed from the specified table.
     *
     * @param tableId the unique id of the table from which this user wishes to be removed.
     * @param listener will receive and process the response.
     */
    public void leaveTable (int tableId, InvocationListener listener);

    /**
     * Requests that the specified table be started now, even if all seats are not occupied. This
     * will always fail if called by any other player than that seated in position 0 (usually the
     * creator).
     */
    public void startTableNow (int tableId, InvocationListener listener);

    /**
     * Requests that another user be booted from the specified table.
     */
    public void bootPlayer (int tableId, Name target, InvocationListener listener);
}
