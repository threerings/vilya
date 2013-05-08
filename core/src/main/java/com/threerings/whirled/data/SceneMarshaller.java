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

package com.threerings.whirled.data;

import javax.annotation.Generated;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.whirled.client.SceneService;

/**
 * Provides the implementation of the {@link SceneService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SceneService.java.")
public class SceneMarshaller extends InvocationMarshaller
    implements SceneService
{
    /**
     * Marshalls results to implementations of {@code SceneService.SceneMoveListener}.
     */
    public static class SceneMoveMarshaller extends ListenerMarshaller
        implements SceneMoveListener
    {
        /** The method id used to dispatch {@link #moveRequiresServerSwitch}
         * responses. */
        public static final int MOVE_REQUIRES_SERVER_SWITCH = 1;

        // from interface SceneMoveMarshaller
        public void moveRequiresServerSwitch (String arg1, int[] arg2)
        {
            sendResponse(MOVE_REQUIRES_SERVER_SWITCH, new Object[] { arg1, arg2 });
        }

        /** The method id used to dispatch {@link #moveSucceeded}
         * responses. */
        public static final int MOVE_SUCCEEDED = 2;

        // from interface SceneMoveMarshaller
        public void moveSucceeded (int arg1, PlaceConfig arg2)
        {
            sendResponse(MOVE_SUCCEEDED, new Object[] { Integer.valueOf(arg1), arg2 });
        }

        /** The method id used to dispatch {@link #moveSucceededWithScene}
         * responses. */
        public static final int MOVE_SUCCEEDED_WITH_SCENE = 3;

        // from interface SceneMoveMarshaller
        public void moveSucceededWithScene (int arg1, PlaceConfig arg2, SceneModel arg3)
        {
            sendResponse(MOVE_SUCCEEDED_WITH_SCENE, new Object[] { Integer.valueOf(arg1), arg2, arg3 });
        }

        /** The method id used to dispatch {@link #moveSucceededWithUpdates}
         * responses. */
        public static final int MOVE_SUCCEEDED_WITH_UPDATES = 4;

        // from interface SceneMoveMarshaller
        public void moveSucceededWithUpdates (int arg1, PlaceConfig arg2, SceneUpdate[] arg3)
        {
            sendResponse(MOVE_SUCCEEDED_WITH_UPDATES, new Object[] { Integer.valueOf(arg1), arg2, arg3 });
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case MOVE_REQUIRES_SERVER_SWITCH:
                ((SceneMoveListener)listener).moveRequiresServerSwitch(
                    (String)args[0], (int[])args[1]);
                return;

            case MOVE_SUCCEEDED:
                ((SceneMoveListener)listener).moveSucceeded(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1]);
                return;

            case MOVE_SUCCEEDED_WITH_SCENE:
                ((SceneMoveListener)listener).moveSucceededWithScene(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1], (SceneModel)args[2]);
                return;

            case MOVE_SUCCEEDED_WITH_UPDATES:
                ((SceneMoveListener)listener).moveSucceededWithUpdates(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1], (SceneUpdate[])args[2]);
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #moveTo} requests. */
    public static final int MOVE_TO = 1;

    // from interface SceneService
    public void moveTo (int arg1, int arg2, SceneService.SceneMoveListener arg3)
    {
        SceneMarshaller.SceneMoveMarshaller listener3 = new SceneMarshaller.SceneMoveMarshaller();
        listener3.listener = arg3;
        sendRequest(MOVE_TO, new Object[] {
            Integer.valueOf(arg1), Integer.valueOf(arg2), listener3
        });
    }
}
