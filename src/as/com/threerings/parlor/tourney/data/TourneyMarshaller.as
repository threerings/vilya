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

package com.threerings.parlor.tourney.data {

import com.threerings.parlor.tourney.client.TourneyService;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;

/**
 * Provides the implementation of the <code>TourneyService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TourneyMarshaller extends InvocationMarshaller
    implements TourneyService
{
    /** The method id used to dispatch <code>cancel</code> requests. */
    public static const CANCEL :int = 1;

    // from interface TourneyService
    public function cancel (arg1 :InvocationService_ConfirmListener) :void
    {
        var listener1 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(CANCEL, [
            listener1
        ]);
    }

    /** The method id used to dispatch <code>join</code> requests. */
    public static const JOIN :int = 2;

    // from interface TourneyService
    public function join (arg1 :InvocationService_ConfirmListener) :void
    {
        var listener1 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(JOIN, [
            listener1
        ]);
    }

    /** The method id used to dispatch <code>leave</code> requests. */
    public static const LEAVE :int = 3;

    // from interface TourneyService
    public function leave (arg1 :InvocationService_ConfirmListener) :void
    {
        var listener1 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(LEAVE, [
            listener1
        ]);
    }
}
}
