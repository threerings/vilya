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

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.Place;

/**
 * Extends {@link Place} with scene information.
 */
public class ScenePlace extends Place
{
    /** The id of the scene occupied by the body. */
    public int sceneId;

    /**
     * Returns the scene id for the supplied place or -1 if the place is null or not a scene place.
     */
    public static int getSceneId (Place place)
    {
        return (place instanceof ScenePlace) ? ((ScenePlace)place).sceneId : -1;
    }

    /**
     * Returns the scene id occupied by the supplied body or -1 if the body is not in a scene.
     */
    public static int getSceneId (BodyObject bobj)
    {
        return getSceneId(bobj.location);
    }

    /**
     * Creates a scene place with the supplied {@link SceneObject} oid and scene id.
     */
    public ScenePlace (int sceneOid, int sceneId)
    {
        super(sceneOid);
        this.sceneId = sceneId;
    }
}
