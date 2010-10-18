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

package com.threerings.stage.data {

import com.threerings.io.TypedArray;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;
import com.threerings.stage.client.StageSceneService;

/**
 * Provides the implementation of the <code>StageSceneService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class StageSceneMarshaller extends InvocationMarshaller
    implements StageSceneService
{
    /** The method id used to dispatch <code>addObject</code> requests. */
    public static const ADD_OBJECT :int = 1;

    // from interface StageSceneService
    public function addObject (arg1 :ObjectInfo, arg2 :InvocationService_ConfirmListener) :void
    {
        var listener2 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(ADD_OBJECT, [
            arg1, listener2
        ]);
    }

    /** The method id used to dispatch <code>removeObjects</code> requests. */
    public static const REMOVE_OBJECTS :int = 2;

    // from interface StageSceneService
    public function removeObjects (arg1 :TypedArray /* of class com.threerings.miso.data.ObjectInfo */, arg2 :InvocationService_ConfirmListener) :void
    {
        var listener2 :InvocationMarshaller_ConfirmMarshaller = new InvocationMarshaller_ConfirmMarshaller();
        listener2.listener = arg2;
        sendRequest(REMOVE_OBJECTS, [
            arg1, listener2
        ]);
    }
}
}
