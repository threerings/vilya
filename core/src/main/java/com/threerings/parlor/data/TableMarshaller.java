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

import javax.annotation.Generated;

import com.threerings.parlor.client.TableService;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.util.Name;

/**
 * Provides the implementation of the {@link TableService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TableService.java.")
public class TableMarshaller extends InvocationMarshaller
    implements TableService
{
    /** The method id used to dispatch {@link #bootPlayer} requests. */
    public static final int BOOT_PLAYER = 1;

    // from interface TableService
    public void bootPlayer (int arg1, Name arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(BOOT_PLAYER, new Object[] {
            Integer.valueOf(arg1), arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #createTable} requests. */
    public static final int CREATE_TABLE = 2;

    // from interface TableService
    public void createTable (TableConfig arg1, GameConfig arg2, InvocationService.ResultListener arg3)
    {
        InvocationMarshaller.ResultMarshaller listener3 = new InvocationMarshaller.ResultMarshaller();
        listener3.listener = arg3;
        sendRequest(CREATE_TABLE, new Object[] {
            arg1, arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #joinTable} requests. */
    public static final int JOIN_TABLE = 3;

    // from interface TableService
    public void joinTable (int arg1, int arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(JOIN_TABLE, new Object[] {
            Integer.valueOf(arg1), Integer.valueOf(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #leaveTable} requests. */
    public static final int LEAVE_TABLE = 4;

    // from interface TableService
    public void leaveTable (int arg1, InvocationService.InvocationListener arg2)
    {
        ListenerMarshaller listener2 = new ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(LEAVE_TABLE, new Object[] {
            Integer.valueOf(arg1), listener2
        });
    }

    /** The method id used to dispatch {@link #startTableNow} requests. */
    public static final int START_TABLE_NOW = 5;

    // from interface TableService
    public void startTableNow (int arg1, InvocationService.InvocationListener arg2)
    {
        ListenerMarshaller listener2 = new ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(START_TABLE_NOW, new Object[] {
            Integer.valueOf(arg1), listener2
        });
    }
}
