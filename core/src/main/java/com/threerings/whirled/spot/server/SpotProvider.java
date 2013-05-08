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

package com.threerings.whirled.spot.server;

import javax.annotation.Generated;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.data.Location;

/**
 * Defines the server-side of the {@link SpotService}.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SpotService.java.")
public interface SpotProvider extends InvocationProvider
{
    /**
     * Handles a {@link SpotService#changeLocation} request.
     */
    void changeLocation (ClientObject caller, int arg1, Location arg2, InvocationService.ConfirmListener arg3)
        throws InvocationException;

    /**
     * Handles a {@link SpotService#clusterSpeak} request.
     */
    void clusterSpeak (ClientObject caller, String arg1, byte arg2);

    /**
     * Handles a {@link SpotService#joinCluster} request.
     */
    void joinCluster (ClientObject caller, int arg1, InvocationService.ConfirmListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link SpotService#traversePortal} request.
     */
    void traversePortal (ClientObject caller, int arg1, int arg2, int arg3, SpotService.SpotSceneMoveListener arg4)
        throws InvocationException;
}
