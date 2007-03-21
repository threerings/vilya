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

package com.threerings.parlor.data;

import com.threerings.parlor.client.TableService;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link TableService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TableMarshaller extends InvocationMarshaller
    implements TableService
{
    /** The method id used to dispatch {@link #createTable} requests. */
    public static final int CREATE_TABLE = 1;

    // from interface TableService
    public void createTable (Client arg1, TableConfig arg2, GameConfig arg3, InvocationService.ResultListener arg4)
    {
        InvocationMarshaller.ResultMarshaller listener4 = new InvocationMarshaller.ResultMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, CREATE_TABLE, new Object[] {
            arg2, arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #joinTable} requests. */
    public static final int JOIN_TABLE = 2;

    // from interface TableService
    public void joinTable (Client arg1, int arg2, int arg3, InvocationService.InvocationListener arg4)
    {
        ListenerMarshaller listener4 = new ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, JOIN_TABLE, new Object[] {
            Integer.valueOf(arg2), Integer.valueOf(arg3), listener4
        });
    }

    /** The method id used to dispatch {@link #leaveTable} requests. */
    public static final int LEAVE_TABLE = 3;

    // from interface TableService
    public void leaveTable (Client arg1, int arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, LEAVE_TABLE, new Object[] {
            Integer.valueOf(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #startTableNow} requests. */
    public static final int START_TABLE_NOW = 4;

    // from interface TableService
    public void startTableNow (Client arg1, int arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, START_TABLE_NOW, new Object[] {
            Integer.valueOf(arg2), listener3
        });
    }
}
