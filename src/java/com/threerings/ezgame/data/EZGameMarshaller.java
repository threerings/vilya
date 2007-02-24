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

package com.threerings.ezgame.data;

import com.threerings.ezgame.client.EZGameService;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;

/**
 * Provides the implementation of the {@link EZGameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class EZGameMarshaller extends InvocationMarshaller
    implements EZGameService
{
    /** The method id used to dispatch {@link #addToCollection} requests. */
    public static final int ADD_TO_COLLECTION = 1;

    // from interface EZGameService
    public void addToCollection (Client arg1, String arg2, byte[][] arg3, boolean arg4, InvocationService.InvocationListener arg5)
    {
        ListenerMarshaller listener5 = new ListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, ADD_TO_COLLECTION, new Object[] {
            arg2, arg3, Boolean.valueOf(arg4), listener5
        });
    }

    /** The method id used to dispatch {@link #checkDictionaryWord} requests. */
    public static final int CHECK_DICTIONARY_WORD = 2;

    // from interface EZGameService
    public void checkDictionaryWord (Client arg1, String arg2, String arg3, InvocationService.ResultListener arg4)
    {
        InvocationMarshaller.ResultMarshaller listener4 = new InvocationMarshaller.ResultMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, CHECK_DICTIONARY_WORD, new Object[] {
            arg2, arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #endGame} requests. */
    public static final int END_GAME = 3;

    // from interface EZGameService
    public void endGame (Client arg1, int[] arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, END_GAME, new Object[] {
            arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #endTurn} requests. */
    public static final int END_TURN = 4;

    // from interface EZGameService
    public void endTurn (Client arg1, int arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, END_TURN, new Object[] {
            Integer.valueOf(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #getCookie} requests. */
    public static final int GET_COOKIE = 5;

    // from interface EZGameService
    public void getCookie (Client arg1, int arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, GET_COOKIE, new Object[] {
            Integer.valueOf(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #getDictionaryLetterSet} requests. */
    public static final int GET_DICTIONARY_LETTER_SET = 6;

    // from interface EZGameService
    public void getDictionaryLetterSet (Client arg1, String arg2, int arg3, InvocationService.ResultListener arg4)
    {
        InvocationMarshaller.ResultMarshaller listener4 = new InvocationMarshaller.ResultMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, GET_DICTIONARY_LETTER_SET, new Object[] {
            arg2, Integer.valueOf(arg3), listener4
        });
    }

    /** The method id used to dispatch {@link #getFromCollection} requests. */
    public static final int GET_FROM_COLLECTION = 7;

    // from interface EZGameService
    public void getFromCollection (Client arg1, String arg2, boolean arg3, int arg4, String arg5, int arg6, InvocationService.ConfirmListener arg7)
    {
        InvocationMarshaller.ConfirmMarshaller listener7 = new InvocationMarshaller.ConfirmMarshaller();
        listener7.listener = arg7;
        sendRequest(arg1, GET_FROM_COLLECTION, new Object[] {
            arg2, Boolean.valueOf(arg3), Integer.valueOf(arg4), arg5, Integer.valueOf(arg6), listener7
        });
    }

    /** The method id used to dispatch {@link #mergeCollection} requests. */
    public static final int MERGE_COLLECTION = 8;

    // from interface EZGameService
    public void mergeCollection (Client arg1, String arg2, String arg3, InvocationService.InvocationListener arg4)
    {
        ListenerMarshaller listener4 = new ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, MERGE_COLLECTION, new Object[] {
            arg2, arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #sendMessage} requests. */
    public static final int SEND_MESSAGE = 9;

    // from interface EZGameService
    public void sendMessage (Client arg1, String arg2, Object arg3, int arg4, InvocationService.InvocationListener arg5)
    {
        ListenerMarshaller listener5 = new ListenerMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, SEND_MESSAGE, new Object[] {
            arg2, arg3, Integer.valueOf(arg4), listener5
        });
    }

    /** The method id used to dispatch {@link #setCookie} requests. */
    public static final int SET_COOKIE = 10;

    // from interface EZGameService
    public void setCookie (Client arg1, byte[] arg2, InvocationService.InvocationListener arg3)
    {
        ListenerMarshaller listener3 = new ListenerMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, SET_COOKIE, new Object[] {
            arg2, listener3
        });
    }

    /** The method id used to dispatch {@link #setProperty} requests. */
    public static final int SET_PROPERTY = 11;

    // from interface EZGameService
    public void setProperty (Client arg1, String arg2, Object arg3, int arg4, boolean arg5, Object arg6, InvocationService.InvocationListener arg7)
    {
        ListenerMarshaller listener7 = new ListenerMarshaller();
        listener7.listener = arg7;
        sendRequest(arg1, SET_PROPERTY, new Object[] {
            arg2, arg3, Integer.valueOf(arg4), Boolean.valueOf(arg5), arg6, listener7
        });
    }

    /** The method id used to dispatch {@link #setTicker} requests. */
    public static final int SET_TICKER = 12;

    // from interface EZGameService
    public void setTicker (Client arg1, String arg2, int arg3, InvocationService.InvocationListener arg4)
    {
        ListenerMarshaller listener4 = new ListenerMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, SET_TICKER, new Object[] {
            arg2, Integer.valueOf(arg3), listener4
        });
    }
}
