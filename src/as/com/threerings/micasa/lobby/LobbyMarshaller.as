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

package com.threerings.micasa.lobby {

import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the <code>LobbyService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class LobbyMarshaller extends InvocationMarshaller
    implements LobbyService
{
    /** The method id used to dispatch <code>getCategories</code> requests. */
    public static const GET_CATEGORIES :int = 1;

    // from interface LobbyService
    public function getCategories (arg1 :LobbyService_CategoriesListener) :void
    {
        var listener1 :LobbyMarshaller_CategoriesMarshaller = new LobbyMarshaller_CategoriesMarshaller();
        listener1.listener = arg1;
        sendRequest(GET_CATEGORIES, [
            listener1
        ]);
    }

    /** The method id used to dispatch <code>getLobbies</code> requests. */
    public static const GET_LOBBIES :int = 2;

    // from interface LobbyService
    public function getLobbies (arg1 :String, arg2 :LobbyService_LobbiesListener) :void
    {
        var listener2 :LobbyMarshaller_LobbiesMarshaller = new LobbyMarshaller_LobbiesMarshaller();
        listener2.listener = arg2;
        sendRequest(GET_LOBBIES, [
            arg1, listener2
        ]);
    }
}
}
