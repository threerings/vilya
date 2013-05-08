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

package com.threerings.parlor.server;

import javax.annotation.Generated;

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.util.Name;

/**
 * Defines the server-side of the {@link ParlorService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from ParlorService.java.")
public interface ParlorProvider extends InvocationProvider
{
    /**
     * Handles a {@link ParlorService#cancel} request.
     */
    void cancel (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link ParlorService#invite} request.
     */
    void invite (ClientObject caller, Name arg1, GameConfig arg2, ParlorService.InviteListener arg3)
        throws InvocationException;

    /**
     * Handles a {@link ParlorService#respond} request.
     */
    void respond (ClientObject caller, int arg1, int arg2, Object arg3, InvocationService.InvocationListener arg4)
        throws InvocationException;

    /**
     * Handles a {@link ParlorService#startSolitaire} request.
     */
    void startSolitaire (ClientObject caller, GameConfig arg1, InvocationService.ConfirmListener arg2)
        throws InvocationException;
}
