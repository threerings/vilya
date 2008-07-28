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

package com.threerings.parlor.server;

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.data.ParlorMarshaller;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;
import com.threerings.util.Name;

/**
 * Dispatches requests to the {@link ParlorProvider}.
 */
public class ParlorDispatcher extends InvocationDispatcher<ParlorMarshaller>
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public ParlorDispatcher (ParlorProvider provider)
    {
        this.provider = provider;
    }

    @Override // documentation inherited
    public ParlorMarshaller createMarshaller ()
    {
        return new ParlorMarshaller();
    }

    @SuppressWarnings("unchecked")
    @Override // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case ParlorMarshaller.CANCEL:
            ((ParlorProvider)provider).cancel(
                source, ((Integer)args[0]).intValue(), (InvocationService.InvocationListener)args[1]
            );
            return;

        case ParlorMarshaller.INVITE:
            ((ParlorProvider)provider).invite(
                source, (Name)args[0], (GameConfig)args[1], (ParlorService.InviteListener)args[2]
            );
            return;

        case ParlorMarshaller.RESPOND:
            ((ParlorProvider)provider).respond(
                source, ((Integer)args[0]).intValue(), ((Integer)args[1]).intValue(), args[2], (InvocationService.InvocationListener)args[3]
            );
            return;

        case ParlorMarshaller.START_SOLITAIRE:
            ((ParlorProvider)provider).startSolitaire(
                source, (GameConfig)args[0], (InvocationService.ConfirmListener)args[1]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
