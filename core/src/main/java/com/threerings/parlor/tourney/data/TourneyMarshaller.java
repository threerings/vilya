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

package com.threerings.parlor.tourney.data;

import javax.annotation.Generated;

import com.threerings.parlor.tourney.client.TourneyService;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link TourneyService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TourneyService.java.")
public class TourneyMarshaller extends InvocationMarshaller
    implements TourneyService
{
    /** The method id used to dispatch {@link #cancel} requests. */
    public static final int CANCEL = 1;

    // from interface TourneyService
    public void cancel (InvocationService.ConfirmListener arg1)
    {
        InvocationMarshaller.ConfirmMarshaller listener1 = new InvocationMarshaller.ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(CANCEL, new Object[] {
            listener1
        });
    }

    /** The method id used to dispatch {@link #join} requests. */
    public static final int JOIN = 2;

    // from interface TourneyService
    public void join (InvocationService.ConfirmListener arg1)
    {
        InvocationMarshaller.ConfirmMarshaller listener1 = new InvocationMarshaller.ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(JOIN, new Object[] {
            listener1
        });
    }

    /** The method id used to dispatch {@link #leave} requests. */
    public static final int LEAVE = 3;

    // from interface TourneyService
    public void leave (InvocationService.ConfirmListener arg1)
    {
        InvocationMarshaller.ConfirmMarshaller listener1 = new InvocationMarshaller.ConfirmMarshaller();
        listener1.listener = arg1;
        sendRequest(LEAVE, new Object[] {
            listener1
        });
    }
}
