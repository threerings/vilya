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

package com.threerings.parlor.client {

import flash.utils.ByteArray;
import com.threerings.parlor.client.TableService;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.client.InvocationService_ResultListener;
import com.threerings.presents.data.InvocationMarshaller_ResultMarshaller;

/**
 * An ActionScript version of the Java TableService interface.
 */
public interface TableService extends InvocationService
{
    // from Java interface TableService
    function createTable (arg1 :Client, arg2 :TableConfig, arg3 :GameConfig, arg4 :InvocationService_ResultListener) :void;

    // from Java interface TableService
    function joinTable (arg1 :Client, arg2 :int, arg3 :int, arg4 :InvocationService_InvocationListener) :void;

    // from Java interface TableService
    function leaveTable (arg1 :Client, arg2 :int, arg3 :InvocationService_InvocationListener) :void;

    // from Java interface TableService
    function startTableNow (arg1 :Client, arg2 :int, arg3 :InvocationService_InvocationListener) :void;
}
}
