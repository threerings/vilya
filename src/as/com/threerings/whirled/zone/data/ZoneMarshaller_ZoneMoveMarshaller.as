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

package com.threerings.whirled.zone.data {

import flash.utils.ByteArray;
import com.threerings.util.*; // for Float, Integer, etc.
import com.threerings.io.TypedArray;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.zone.client.ZoneService;
import com.threerings.whirled.zone.client.ZoneService_ZoneMoveListener;
import com.threerings.whirled.zone.data.ZoneSummary;

/**
 * Marshalls instances of the ZoneService_ZoneMoveMarshaller interface.
 */
public class ZoneMarshaller_ZoneMoveMarshaller
    extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch {@link #moveSucceeded} responses. */
    public static const MOVE_SUCCEEDED :int = 1;

    /** The method id used to dispatch {@link #moveSucceededWithScene} responses. */
    public static const MOVE_SUCCEEDED_WITH_SCENE :int = 2;

    /** The method id used to dispatch {@link #moveSucceededWithUpdates} responses. */
    public static const MOVE_SUCCEEDED_WITH_UPDATES :int = 3;

    // from InvocationMarshaller_ListenerMarshaller
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case MOVE_SUCCEEDED:
            (listener as ZoneService_ZoneMoveListener).moveSucceeded(
                (args[0] as int), (args[1] as PlaceConfig), (args[2] as ZoneSummary));
            return;

        case MOVE_SUCCEEDED_WITH_SCENE:
            (listener as ZoneService_ZoneMoveListener).moveSucceededWithScene(
                (args[0] as int), (args[1] as PlaceConfig), (args[2] as ZoneSummary), (args[3] as SceneModel));
            return;

        case MOVE_SUCCEEDED_WITH_UPDATES:
            (listener as ZoneService_ZoneMoveListener).moveSucceededWithUpdates(
                (args[0] as int), (args[1] as PlaceConfig), (args[2] as ZoneSummary), (args[3] as TypedArray /* of class com.threerings.whirled.data.SceneUpdate */));
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
