//
// $Id: ParlorMarshaller.java 4145 2006-05-24 01:24:24Z ray $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.util.Name;

/**
 * Provides the implementation of the {@link ParlorService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ParlorMarshaller extends InvocationMarshaller
    implements ParlorService
{
    /** The method id used to dispatch {@link #cancel} requests. */
    public static const CANCEL :int = 1;

    // documentation inherited from interface
    public function cancel (arg1 :Client, arg2 :int, arg3 :InvocationService_InvocationListener) :void
    {
        var listener3 :InvocationMarshaller_ListenerMarshaller =
            new InvocationMarshaller_ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, CANCEL, [
            Integer.valueOf(arg2), listener3
        ]);
    }

    /** The method id used to dispatch {@link #createTable} requests. */
    public static const CREATE_TABLE :int = 2;

    // documentation inherited from interface
    public function createTable (arg1 :Client, arg2 :int, arg3 :TableConfig, arg4 :GameConfig, arg5 :ParlorService_TableListener) :void
    {
        var listener5 :ParlorMarshaller_TableMarshaller =
            new ParlorMarshaller_TableMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, CREATE_TABLE, [
            Integer.valueOf(arg2), arg3, arg4, listener5
        ]);
    }

    /** The method id used to dispatch {@link #invite} requests. */
    public static const INVITE :int = 3;

    // documentation inherited from interface
    public function invite (arg1 :Client, arg2 :Name, arg3 :GameConfig, arg4 :ParlorService_InviteListener) :void
    {
        var listener4 :ParlorMarshaller_InviteMarshaller =
            new ParlorMarshaller_InviteMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, INVITE, [
            arg2, arg3, listener4
        ]);
    }

    /** The method id used to dispatch {@link #joinTable} requests. */
    public static const JOIN_TABLE :int = 4;

    // documentation inherited from interface
    public function joinTable (arg1 :Client, arg2 :int, arg3 :int, arg4 :int, arg5 :InvocationService_InvocationListener) :void
    {
        var listener5 :InvocationMarshaller_ListenerMarshaller listener5 =
            new InvocationMarshaller_ListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, JOIN_TABLE, [
            Integer.valueOf(arg2), Integer.valueOf(arg3), Integer.valueOf(arg4), listener5
        ]);
    }

    /** The method id used to dispatch {@link #leaveTable} requests. */
    public static const LEAVE_TABLE :int = 5;

    // documentation inherited from interface
    public function leaveTable (arg1 :Client, arg2 :int, arg3 :int, arg4 :InvocationService_InvocationListener) :void
    {
        var listener4 :InvocationMarshaller_ListenerMarshaller =
            new InvocationMarshaller_ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, LEAVE_TABLE, [
            Integer.valueOf(arg2), Integer.valueOf(arg3), listener4
        ]);
    }

    /** The method id used to dispatch {@link #respond} requests. */
    public static const RESPOND :int = 6;

    // documentation inherited from interface
    public function respond (arg1 :Client, arg2 :int, arg3 :int, arg4 :Object, arg5 :InvocationService_InvocationListener) :void
    {
        var listener5 :InvocationMarshaller_ListenerMarshaller =
            new InvocationMarshallerListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, RESPOND, [
            Integer.valueOf(arg2), Integer.valueOf(arg3), arg4, listener5
        ]);
    }

    /** The method id used to dispatch {@link #startSolitaire} requests. */
    public static const START_SOLITAIRE :int = 7;

    // documentation inherited from interface
    public function startSolitaire (arg1 :Client, arg2 :GameConfig, arg3 :InvocationService_ConfirmListener) :void
    {
        var listener3 :InvocationMarshaller_ConfirmMarshaller =
            new InvocationMarshaller_ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, START_SOLITAIRE, [
            arg2, listener3
        ]);
    }

}
}
