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

package com.threerings.whirled.data {

import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.util.Integer;
import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.client.SceneService_SceneMoveListener;

/**
 * Provides the implementation of the <code>SceneService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class SceneMarshaller extends InvocationMarshaller
    implements SceneService
{
    /** The method id used to dispatch <code>moveTo</code> requests. */
    public static const MOVE_TO :int = 1;

    // from interface SceneService
    public function moveTo (arg1 :int, arg2 :int, arg3 :SceneService_SceneMoveListener) :void
    {
        var listener3 :SceneMarshaller_SceneMoveMarshaller = new SceneMarshaller_SceneMoveMarshaller();
        listener3.listener = arg3;
        sendRequest(MOVE_TO, [
            Integer.valueOf(arg1), Integer.valueOf(arg2), listener3
        ]);
    }
}
}
