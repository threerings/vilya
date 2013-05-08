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

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.util.Name;

/**
 * Provides the implementation of the {@link ParlorService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from ParlorService.java.")
public class ParlorMarshaller extends InvocationMarshaller
    implements ParlorService
{
    /**
     * Marshalls results to implementations of {@code ParlorService.InviteListener}.
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
            sendResponse(INVITE_RECEIVED, new Object[] { Integer.valueOf(arg1) });
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
    public void cancel (int arg1, InvocationService.InvocationListener arg2)
    {
        ListenerMarshaller listener2 = new ListenerMarshaller();
        listener2.listener = arg2;
        sendRequest(CANCEL, new Object[] {
            Integer.valueOf(arg1), listener2
        });
    }

    /** The method id used to dispatch {@link #invite} requests. */
    public static final int INVITE = 2;

    // from interface ParlorService
    public void invite (Name arg1, GameConfig arg2, ParlorService.InviteListener arg3)
    {
        ParlorMarshaller.InviteMarshaller listener3 = new ParlorMarshaller.InviteMarshaller();
        listener3.listener = arg3;
        sendRequest(INVITE, new Object[] {
            arg1, arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #respond} requests. */
    public static final int RESPOND = 3;

    // from interface ParlorService
    public void respond (int arg1, int arg2, Object arg3, InvocationService.InvocationListener arg4)
    {
        ListenerMarshaller listener4 = new ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(RESPOND, new Object[] {
            Integer.valueOf(arg1), Integer.valueOf(arg2), arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #startSolitaire} requests. */
    public static final int START_SOLITAIRE = 4;

    // from interface ParlorService
    public void startSolitaire (GameConfig arg1, InvocationService.ConfirmListener arg2)
    {
        InvocationMarshaller.ConfirmMarshaller listener2 = new InvocationMarshaller.ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(START_SOLITAIRE, new Object[] {
            arg1, listener2
        });
    }
}
