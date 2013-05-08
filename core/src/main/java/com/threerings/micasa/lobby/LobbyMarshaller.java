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

package com.threerings.micasa.lobby;

import javax.annotation.Generated;

import com.threerings.presents.data.InvocationMarshaller;
import java.util.List;

/**
 * Provides the implementation of the {@link LobbyService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from LobbyService.java.")
public class LobbyMarshaller extends InvocationMarshaller
    implements LobbyService
{
    /**
     * Marshalls results to implementations of {@code LobbyService.CategoriesListener}.
     */
    public static class CategoriesMarshaller extends ListenerMarshaller
        implements CategoriesListener
    {
        /** The method id used to dispatch {@link #gotCategories}
         * responses. */
        public static final int GOT_CATEGORIES = 1;

        // from interface CategoriesMarshaller
        public void gotCategories (String[] arg1)
        {
            sendResponse(GOT_CATEGORIES, new Object[] { arg1 });
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_CATEGORIES:
                ((CategoriesListener)listener).gotCategories(
                    (String[])args[0]);
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /**
     * Marshalls results to implementations of {@code LobbyService.LobbiesListener}.
     */
    public static class LobbiesMarshaller extends ListenerMarshaller
        implements LobbiesListener
    {
        /** The method id used to dispatch {@link #gotLobbies}
         * responses. */
        public static final int GOT_LOBBIES = 1;

        // from interface LobbiesMarshaller
        public void gotLobbies (List<Lobby> arg1)
        {
            sendResponse(GOT_LOBBIES, new Object[] { arg1 });
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case GOT_LOBBIES:
                ((LobbiesListener)listener).gotLobbies(
                    this.<List<Lobby>>cast(args[0]));
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #getCategories} requests. */
    public static final int GET_CATEGORIES = 1;

    // from interface LobbyService
    public void getCategories (LobbyService.CategoriesListener arg1)
    {
        LobbyMarshaller.CategoriesMarshaller listener1 = new LobbyMarshaller.CategoriesMarshaller();
        listener1.listener = arg1;
        sendRequest(GET_CATEGORIES, new Object[] {
            listener1
        });
    }

    /** The method id used to dispatch {@link #getLobbies} requests. */
    public static final int GET_LOBBIES = 2;

    // from interface LobbyService
    public void getLobbies (String arg1, LobbyService.LobbiesListener arg2)
    {
        LobbyMarshaller.LobbiesMarshaller listener2 = new LobbyMarshaller.LobbiesMarshaller();
        listener2.listener = arg2;
        sendRequest(GET_LOBBIES, new Object[] {
            arg1, listener2
        });
    }
}
