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

package com.threerings.parlor.data;

import com.threerings.util.Name;

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.game.data.GameConfig;

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
    /**
     * Marshalls results to implementations of {@link ParlorService.InviteListener}.
     */
    public static class InviteMarshaller extends ListenerMarshaller
        implements InviteListener
    {
        /** The method id used to dispatch {@link #inviteReceived}
         * responses. */
        public static final int INVITE_RECEIVED = 1;

        // from interface InviteMarshaller
        public void inviteReceived (int arg1)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, INVITE_RECEIVED,
                               new Object[] { Integer.valueOf(arg1) }, transport));
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case INVITE_RECEIVED:
                ((InviteListener)listener).inviteReceived(
                    ((Integer)args[0]).intValue());
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #cancel} requests. */
    public static final int CANCEL = 1;

    // from interface ParlorService
    public void cancel (Client arg1, int arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, CANCEL, new Object[] {
            Integer.valueOf(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #invite} requests. */
    public static final int INVITE = 2;

    // from interface ParlorService
    public void invite (Client arg1, Name arg2, GameConfig arg3, ParlorService.InviteListener arg4)
    {
        ParlorMarshaller.InviteMarshaller listener4 = new ParlorMarshaller.InviteMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, INVITE, new Object[] {
            arg2, arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #respond} requests. */
    public static final int RESPOND = 3;

    // from interface ParlorService
    public void respond (Client arg1, int arg2, int arg3, Object arg4, InvocationService.InvocationListener arg5)
    {
        ListenerMarshaller listener5 = new ListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, RESPOND, new Object[] {
            Integer.valueOf(arg2), Integer.valueOf(arg3), arg4, listener5
        });
    }

    /** The method id used to dispatch {@link #startSolitaire} requests. */
    public static final int START_SOLITAIRE = 4;

    // from interface ParlorService
    public void startSolitaire (Client arg1, GameConfig arg2, InvocationService.ConfirmListener arg3)
    {
        InvocationMarshaller.ConfirmMarshaller listener3 = new InvocationMarshaller.ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, START_SOLITAIRE, new Object[] {
            arg2, listener3
        });
    }
}
