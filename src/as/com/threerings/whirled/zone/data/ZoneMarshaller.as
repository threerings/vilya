//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.zone.data {

import com.threerings.util.*; // for Float, Integer, etc.

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.zone.client.ZoneService;
import com.threerings.whirled.zone.client.ZoneService_ZoneMoveListener;
import com.threerings.whirled.zone.data.ZoneMarshaller_ZoneMoveMarshaller;
import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * Provides the implementation of the {@link ZoneService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class ZoneMarshaller extends InvocationMarshaller
    implements ZoneService
{
    /** The method id used to dispatch {@link #moveTo} requests. */
    public static const MOVE_TO :int = 1;

    // from interface ZoneService
    public function moveTo (arg1 :Client, arg2 :int, arg3 :int, arg4 :int, arg5 :ZoneService_ZoneMoveListener) :void
    {
        var listener5 :ZoneMarshaller_ZoneMoveMarshaller = new ZoneMarshaller_ZoneMoveMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, MOVE_TO, [
            Integer.valueOf(arg2), Integer.valueOf(arg3), Integer.valueOf(arg4), listener5
        ]);
    }
}
}
