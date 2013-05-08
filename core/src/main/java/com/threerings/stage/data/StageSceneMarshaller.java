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

package com.threerings.stage.data;

import javax.annotation.Generated;

import com.threerings.miso.data.ObjectInfo;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.stage.client.StageSceneService;

/**
 * Provides the implementation of the {@link StageSceneService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from StageSceneService.java.")
public class StageSceneMarshaller extends InvocationMarshaller
    implements StageSceneService
{
    /** The method id used to dispatch {@link #addObject} requests. */
    public static final int ADD_OBJECT = 1;

    // from interface StageSceneService
    public void addObject (ObjectInfo arg1, InvocationService.ConfirmListener arg2)
    {
        InvocationMarshaller.ConfirmMarshaller listener2 = new InvocationMarshaller.ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(ADD_OBJECT, new Object[] {
            arg1, listener2
        });
    }

    /** The method id used to dispatch {@link #removeObjects} requests. */
    public static final int REMOVE_OBJECTS = 2;

    // from interface StageSceneService
    public void removeObjects (ObjectInfo[] arg1, InvocationService.ConfirmListener arg2)
    {
        InvocationMarshaller.ConfirmMarshaller listener2 = new InvocationMarshaller.ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(REMOVE_OBJECTS, new Object[] {
            arg1, listener2
        });
    }
}
