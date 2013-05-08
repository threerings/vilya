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

package com.threerings.parlor.data {

import com.threerings.util.Integer;
import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.client.ParlorService_InviteListener;
import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides the implementation of the <code>ParlorService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ParlorMarshaller extends InvocationMarshaller
    implements ParlorService
{
    /** The method id used to dispatch <code>cancel</code> requests. */
    public static const CANCEL :int = 1;

    // from interface ParlorService
    public function cancel (arg1 :int, arg2 :InvocationService_InvocationListener) :void
    {
        var listener2 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(CANCEL, [
            Integer.valueOf(arg1), listener2
        ]);
    }

    /** The method id used to dispatch <code>invite</code> requests. */
    public static const INVITE :int = 2;

    // from interface ParlorService
    public function invite (arg1 :Name, arg2 :GameConfig, arg3 :ParlorService_InviteListener) :void
    {
        var listener3 :ParlorMarshaller_InviteMarshaller = new ParlorMarshaller_InviteMarshaller();
        listener3.listener = arg3;
        sendRequest(INVITE, [
            arg1, arg2, listener3
        ]);
    }

    /** The method id used to dispatch <code>respond</code> requests. */
    public static const RESPOND :int = 3;

    // from interface ParlorService
    public function respond (arg1 :int, arg2 :int, arg3 :Object, arg4 :InvocationService_InvocationListener) :void
    {
        var listener4 :InvocationMarshaller_ListenerMarshaller = new InvocationMarshaller_ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(RESPOND, [
            Integer.valueOf(arg1), Integer.valueOf(arg2), arg3, listener4
        ]);
    }

    /** The method id used to dispatch <code>startSolitaire</code> requests. */
    public static const START_SOLITAIRE :int = 4;

    // from interface ParlorService
    public function startSolitaire (arg1 :GameConfig, arg2 :InvocationService_ConfirmListener) :void
    {
        var listener2 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(START_SOLITAIRE, [
            arg1, listener2
        ]);
    }
}
}
