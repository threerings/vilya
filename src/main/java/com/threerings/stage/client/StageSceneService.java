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

package com.threerings.stage.client;

import com.threerings.presents.client.InvocationService;

import com.threerings.miso.data.ObjectInfo;

/**
 * Provides services relating to Stage scenes.
 */
public interface StageSceneService extends InvocationService
{
    /**
     * Requests to add the supplied object to the current scene.
     */
    public void addObject (ObjectInfo info, ConfirmListener listener);

    /**
     * Requests to remove the supplied objects from the current scene.
     */
    public void removeObjects (ObjectInfo[] info, ConfirmListener listener);
}
