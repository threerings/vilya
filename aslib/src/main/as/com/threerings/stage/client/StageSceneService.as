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

package com.threerings.stage.client {

import com.threerings.io.TypedArray;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_ConfirmListener;

import com.threerings.miso.data.ObjectInfo;

/**
 * An ActionScript version of the Java StageSceneService interface.
 */
public interface StageSceneService extends InvocationService
{
    // from Java interface StageSceneService
    function addObject (arg1 :ObjectInfo, arg2 :InvocationService_ConfirmListener) :void;

    // from Java interface StageSceneService
    function removeObjects (arg1 :TypedArray /* of class com.threerings.miso.data.ObjectInfo */, arg2 :InvocationService_ConfirmListener) :void;
}
}
