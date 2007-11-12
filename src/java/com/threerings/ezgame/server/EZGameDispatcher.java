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

package com.threerings.ezgame.server;

import com.threerings.ezgame.client.EZGameService;
import com.threerings.ezgame.data.EZGameMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link EZGameProvider}.
 */
public class EZGameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public EZGameDispatcher (EZGameProvider provider)
    {
        this.provider = provider;
    }

    // from InvocationDispatcher
    public InvocationMarshaller createMarshaller ()
    {
        return new EZGameMarshaller();
    }

    @SuppressWarnings("unchecked") // from InvocationDispatcher
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case EZGameMarshaller.ADD_TO_COLLECTION:
            ((EZGameProvider)provider).addToCollection(
                source,
                (String)args[0], (byte[][])args[1], ((Boolean)args[2]).booleanValue(), (InvocationService.InvocationListener)args[3]
            );
            return;

        case EZGameMarshaller.CHECK_DICTIONARY_WORD:
            ((EZGameProvider)provider).checkDictionaryWord(
                source,
                (String)args[0], (String)args[1], (String)args[2], (InvocationService.ResultListener)args[3]
            );
            return;

        case EZGameMarshaller.END_GAME:
            ((EZGameProvider)provider).endGame(
                source,
                (int[])args[0], (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.END_ROUND:
            ((EZGameProvider)provider).endRound(
                source,
                ((Integer)args[0]).intValue(), (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.END_TURN:
            ((EZGameProvider)provider).endTurn(
                source,
                ((Integer)args[0]).intValue(), (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.GET_COOKIE:
            ((EZGameProvider)provider).getCookie(
                source,
                ((Integer)args[0]).intValue(), (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.GET_DICTIONARY_LETTER_SET:
            ((EZGameProvider)provider).getDictionaryLetterSet(
                source,
                (String)args[0], (String)args[1], ((Integer)args[2]).intValue(), (InvocationService.ResultListener)args[3]
            );
            return;

        case EZGameMarshaller.GET_FROM_COLLECTION:
            ((EZGameProvider)provider).getFromCollection(
                source,
                (String)args[0], ((Boolean)args[1]).booleanValue(), ((Integer)args[2]).intValue(), (String)args[3], ((Integer)args[4]).intValue(), (InvocationService.ConfirmListener)args[5]
            );
            return;

        case EZGameMarshaller.MERGE_COLLECTION:
            ((EZGameProvider)provider).mergeCollection(
                source,
                (String)args[0], (String)args[1], (InvocationService.InvocationListener)args[2]
            );
            return;

        case EZGameMarshaller.RESTART_GAME_IN:
            ((EZGameProvider)provider).restartGameIn(
                source,
                ((Integer)args[0]).intValue(), (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.SEND_MESSAGE:
            ((EZGameProvider)provider).sendMessage(
                source,
                (String)args[0], args[1], ((Integer)args[2]).intValue(), (InvocationService.InvocationListener)args[3]
            );
            return;

        case EZGameMarshaller.SET_COOKIE:
            ((EZGameProvider)provider).setCookie(
                source,
                (byte[])args[0], (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.SET_PROPERTY:
            ((EZGameProvider)provider).setProperty(
                source,
                (String)args[0], args[1], ((Integer)args[2]).intValue(), ((Boolean)args[3]).booleanValue(), args[4], (InvocationService.InvocationListener)args[5]
            );
            return;

        case EZGameMarshaller.SET_TICKER:
            ((EZGameProvider)provider).setTicker(
                source,
                (String)args[0], ((Integer)args[1]).intValue(), (InvocationService.InvocationListener)args[2]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
