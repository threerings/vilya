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

package com.threerings.whirled.data {

import flash.utils.ByteArray;
import com.threerings.util.*; // for Float, Integer, etc.

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;
import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.client.SceneService_SceneMoveListener;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Marshalls instances of the SceneService_SceneMoveMarshaller interface.
 */
public class SceneMarshaller_SceneMoveMarshaller
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
            (listener as SceneService_SceneMoveListener).moveSucceeded(
                (args[0] as Integer).value, (args[1] as PlaceConfig));
            return;

        case MOVE_SUCCEEDED_WITH_SCENE:
            (listener as SceneService_SceneMoveListener).moveSucceededWithScene(
                (args[0] as Integer).value, (args[1] as PlaceConfig), (args[2] as SceneModel));
            return;

        case MOVE_SUCCEEDED_WITH_UPDATES:
            (listener as SceneService_SceneMoveListener).moveSucceededWithUpdates(
                (args[0] as Integer).value, (args[1] as PlaceConfig), (args[2] as Array));
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
