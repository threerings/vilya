//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.spot.data;

import javax.annotation.Generated;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.dobj.InvocationResponseEvent;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.client.SpotService;

/**
 * Provides the implementation of the {@link SpotService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from SpotService.java.")
public class SpotMarshaller extends InvocationMarshaller
    implements SpotService
{
    /**
     * Marshalls results to implementations of {@link SpotService.SpotSceneMoveListener}.
     */
    public static class SpotSceneMoveMarshaller extends ListenerMarshaller
        implements SpotSceneMoveListener
    {
        /** The method id used to dispatch {@link #moveRequiresServerSwitch}
         * responses. */
        public static final int MOVE_REQUIRES_SERVER_SWITCH = 1;

        // from interface SpotSceneMoveMarshaller
        public void moveRequiresServerSwitch (String arg1, int[] arg2)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_REQUIRES_SERVER_SWITCH,
                               new Object[] { arg1, arg2 }, transport));
        }

        /** The method id used to dispatch {@link #moveSucceeded}
         * responses. */
        public static final int MOVE_SUCCEEDED = 2;

        // from interface SpotSceneMoveMarshaller
        public void moveSucceeded (int arg1, PlaceConfig arg2)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED,
                               new Object[] { Integer.valueOf(arg1), arg2 }, transport));
        }

        /** The method id used to dispatch {@link #moveSucceededWithScene}
         * responses. */
        public static final int MOVE_SUCCEEDED_WITH_SCENE = 3;

        // from interface SpotSceneMoveMarshaller
        public void moveSucceededWithScene (int arg1, PlaceConfig arg2, SceneModel arg3)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED_WITH_SCENE,
                               new Object[] { Integer.valueOf(arg1), arg2, arg3 }, transport));
        }

        /** The method id used to dispatch {@link #moveSucceededWithUpdates}
         * responses. */
        public static final int MOVE_SUCCEEDED_WITH_UPDATES = 4;

        // from interface SpotSceneMoveMarshaller
        public void moveSucceededWithUpdates (int arg1, PlaceConfig arg2, SceneUpdate[] arg3)
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, MOVE_SUCCEEDED_WITH_UPDATES,
                               new Object[] { Integer.valueOf(arg1), arg2, arg3 }, transport));
        }

        /** The method id used to dispatch {@link #requestCancelled}
         * responses. */
        public static final int REQUEST_CANCELLED = 5;

        // from interface SpotSceneMoveMarshaller
        public void requestCancelled ()
        {
            _invId = null;
            omgr.postEvent(new InvocationResponseEvent(
                               callerOid, requestId, REQUEST_CANCELLED,
                               new Object[] {  }, transport));
        }

        @Override // from InvocationMarshaller
        public void dispatchResponse (int methodId, Object[] args)
        {
            switch (methodId) {
            case MOVE_REQUIRES_SERVER_SWITCH:
                ((SpotSceneMoveListener)listener).moveRequiresServerSwitch(
                    (String)args[0], (int[])args[1]);
                return;

            case MOVE_SUCCEEDED:
                ((SpotSceneMoveListener)listener).moveSucceeded(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1]);
                return;

            case MOVE_SUCCEEDED_WITH_SCENE:
                ((SpotSceneMoveListener)listener).moveSucceededWithScene(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1], (SceneModel)args[2]);
                return;

            case MOVE_SUCCEEDED_WITH_UPDATES:
                ((SpotSceneMoveListener)listener).moveSucceededWithUpdates(
                    ((Integer)args[0]).intValue(), (PlaceConfig)args[1], (SceneUpdate[])args[2]);
                return;

            case REQUEST_CANCELLED:
                ((SpotSceneMoveListener)listener).requestCancelled(
                    );
                return;

            default:
                super.dispatchResponse(methodId, args);
                return;
            }
        }
    }

    /** The method id used to dispatch {@link #changeLocation} requests. */
    public static final int CHANGE_LOCATION = 1;

    // from interface SpotService
    public void changeLocation (Client arg1, int arg2, Location arg3, InvocationService.ConfirmListener arg4)
    {
        InvocationMarshaller.ConfirmMarshaller listener4 = new InvocationMarshaller.ConfirmMarshaller();
        listener4.listener = arg4;
        sendRequest(arg1, CHANGE_LOCATION, new Object[] {
            Integer.valueOf(arg2), arg3, listener4
        });
    }

    /** The method id used to dispatch {@link #clusterSpeak} requests. */
    public static final int CLUSTER_SPEAK = 2;

    // from interface SpotService
    public void clusterSpeak (Client arg1, String arg2, byte arg3)
    {
        sendRequest(arg1, CLUSTER_SPEAK, new Object[] {
            arg2, Byte.valueOf(arg3)
        });
    }

    /** The method id used to dispatch {@link #joinCluster} requests. */
    public static final int JOIN_CLUSTER = 3;

    // from interface SpotService
    public void joinCluster (Client arg1, int arg2, InvocationService.ConfirmListener arg3)
    {
        InvocationMarshaller.ConfirmMarshaller listener3 = new InvocationMarshaller.ConfirmMarshaller();
        listener3.listener = arg3;
        sendRequest(arg1, JOIN_CLUSTER, new Object[] {
            Integer.valueOf(arg2), listener3
        });
    }

    /** The method id used to dispatch {@link #traversePortal} requests. */
    public static final int TRAVERSE_PORTAL = 4;

    // from interface SpotService
    public void traversePortal (Client arg1, int arg2, int arg3, int arg4, SpotService.SpotSceneMoveListener arg5)
    {
        SpotMarshaller.SpotSceneMoveMarshaller listener5 = new SpotMarshaller.SpotSceneMoveMarshaller();
        listener5.listener = arg5;
        sendRequest(arg1, TRAVERSE_PORTAL, new Object[] {
            Integer.valueOf(arg2), Integer.valueOf(arg3), Integer.valueOf(arg4), listener5
        });
    }
}
