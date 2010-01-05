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

package com.threerings.parlor.data {

import com.threerings.parlor.client.TableService;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.client.InvocationService_ResultListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ResultMarshaller;
import com.threerings.util.Integer;
import com.threerings.util.Name;

/**
 * Provides the implementation of the <code>TableService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TableMarshaller extends InvocationMarshaller
    implements TableService
{
    /** The method id used to dispatch <code>bootPlayer</code> requests. */
    public static const BOOT_PLAYER :int = 1;

    // from interface TableService
    public function bootPlayer (arg1 :int, arg2 :Name, arg3 :InvocationService_InvocationListener) :void
    {
        var listener3 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(BOOT_PLAYER, [
            Integer.valueOf(arg1), arg2, listener3
        ]);
    }

    /** The method id used to dispatch <code>createTable</code> requests. */
    public static const CREATE_TABLE :int = 2;

    // from interface TableService
    public function createTable (arg1 :TableConfig, arg2 :GameConfig, arg3 :InvocationService_ResultListener) :void
    {
        var listener3 :InvocationMarshaller_ResultMarshaller = new InvocationMarshaller_ResultMarshaller();
        listener3.listener = arg3;
        sendRequest(CREATE_TABLE, [
            arg1, arg2, listener3
        ]);
    }

    /** The method id used to dispatch <code>joinTable</code> requests. */
    public static const JOIN_TABLE :int = 3;

    // from interface TableService
    public function joinTable (arg1 :int, arg2 :int, arg3 :InvocationService_InvocationListener) :void
    {
        var listener3 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(JOIN_TABLE, [
            Integer.valueOf(arg1), Integer.valueOf(arg2), listener3
        ]);
    }

    /** The method id used to dispatch <code>leaveTable</code> requests. */
    public static const LEAVE_TABLE :int = 4;

    // from interface TableService
    public function leaveTable (arg1 :int, arg2 :InvocationService_InvocationListener) :void
    {
        var listener2 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(LEAVE_TABLE, [
            Integer.valueOf(arg1), listener2
        ]);
    }

    /** The method id used to dispatch <code>startTableNow</code> requests. */
    public static const START_TABLE_NOW :int = 5;

    // from interface TableService
    public function startTableNow (arg1 :int, arg2 :InvocationService_InvocationListener) :void
    {
        var listener2 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(START_TABLE_NOW, [
            Integer.valueOf(arg1), listener2
        ]);
    }
}
}
