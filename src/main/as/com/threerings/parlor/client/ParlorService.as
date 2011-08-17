//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.client {

import com.threerings.util.Name;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.client.InvocationService_InvocationListener;

import com.threerings.parlor.game.data.GameConfig;

/**
 * An ActionScript version of the Java ParlorService interface.
 */
public interface ParlorService extends InvocationService
{
    // from Java interface ParlorService
    function cancel (arg1 :int, arg2 :InvocationService_InvocationListener) :void;

    // from Java interface ParlorService
    function invite (arg1 :Name, arg2 :GameConfig, arg3 :ParlorService_InviteListener) :void;

    // from Java interface ParlorService
    function respond (arg1 :int, arg2 :int, arg3 :Object, arg4 :InvocationService_InvocationListener) :void;

    // from Java interface ParlorService
    function startSolitaire (arg1 :GameConfig, arg2 :InvocationService_ConfirmListener) :void;
}
}
