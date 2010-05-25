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

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import java.util.List;

/**
 * Marshalls instances of the LobbyService_LobbiesMarshaller interface.
 */
public class LobbyMarshaller_LobbiesMarshaller
    extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch <code>gotLobbies</code> responses. */
    public static const GOT_LOBBIES :int = 1;

    // from InvocationMarshaller_ListenerMarshaller
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case GOT_LOBBIES:
            (listener as LobbyService_LobbiesListener).gotLobbies(
                (args[0] as List));
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
