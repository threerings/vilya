//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.spot.data {

import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;
import com.threerings.util.Byte;
import com.threerings.util.Integer;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.client.SpotService_SpotSceneMoveListener;

/**
 * Provides the implementation of the <code>SpotService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class SpotMarshaller extends InvocationMarshaller
    implements SpotService
{
    /** The method id used to dispatch <code>changeLocation</code> requests. */
    public static const CHANGE_LOCATION :int = 1;

    // from interface SpotService
    public function changeLocation (arg1 :int, arg2 :Location, arg3 :InvocationService_ConfirmListener) :void
    {
        var listener3 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(CHANGE_LOCATION, [
            Integer.valueOf(arg1), arg2, listener3
        ]);
    }

    /** The method id used to dispatch <code>clusterSpeak</code> requests. */
    public static const CLUSTER_SPEAK :int = 2;

    // from interface SpotService
    public function clusterSpeak (arg1 :String, arg2 :int) :void
    {
        sendRequest(CLUSTER_SPEAK, [
            arg1, Byte.valueOf(arg2)
        ]);
    }

    /** The method id used to dispatch <code>joinCluster</code> requests. */
    public static const JOIN_CLUSTER :int = 3;

    // from interface SpotService
    public function joinCluster (arg1 :int, arg2 :InvocationService_ConfirmListener) :void
    {
        var listener2 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(JOIN_CLUSTER, [
            Integer.valueOf(arg1), listener2
        ]);
    }

    /** The method id used to dispatch <code>traversePortal</code> requests. */
    public static const TRAVERSE_PORTAL :int = 4;

    // from interface SpotService
    public function traversePortal (arg1 :int, arg2 :int, arg3 :int, arg4 :SpotService_SpotSceneMoveListener) :void
    {
        var listener4 :SpotMarshaller_SpotSceneMoveMarshaller = new SpotMarshaller_SpotSceneMoveMarshaller();
        listener4.listener = arg4;
        sendRequest(TRAVERSE_PORTAL, [
            Integer.valueOf(arg1), Integer.valueOf(arg2), Integer.valueOf(arg3), listener4
        ]);
    }
}
}
