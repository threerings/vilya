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

package com.threerings.whirled.spot.client {

import flash.utils.ByteArray;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;
import com.threerings.whirled.client.SceneService_SceneMoveListener;
import com.threerings.whirled.data.SceneMarshaller_SceneMoveMarshaller;
import com.threerings.whirled.spot.client.SpotService;
import com.threerings.whirled.spot.data.Location;

/**
 * An ActionScript version of the Java SpotService interface.
 */
public interface SpotService extends InvocationService
{
    // from Java interface SpotService
    function changeLocation (arg1 :Client, arg2 :int, arg3 :Location, arg4 :InvocationService_ConfirmListener) :void;

    // from Java interface SpotService
    function clusterSpeak (arg1 :Client, arg2 :String, arg3 :int) :void;

    // from Java interface SpotService
    function joinCluster (arg1 :Client, arg2 :int, arg3 :InvocationService_ConfirmListener) :void;

    // from Java interface SpotService
    function traversePortal (arg1 :Client, arg2 :int, arg3 :int, arg4 :int, arg5 :SceneService_SceneMoveListener) :void;
}
}
