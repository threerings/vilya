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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.Place;

/**
 * Extends {@link Place} with scene information.
 */
public class ScenePlace extends Place
{
    /** The id of the scene occupied by the body. */
    public var sceneId :int;

    /**
     * Returns the scene id occupied by the supplied body or -1 if the body is not in a scene.
     */
    public static function getSceneId (bobj :BodyObject) :int
    {
        return (bobj.location is ScenePlace) ? (bobj.location as ScenePlace).sceneId : -1;
    }

    /**
     * Creates a scene place with the supplied {@link SceneObject} oid and scene id.
     */
    public function ScenePlace (sceneOid :int = 0, sceneID :int = 0)
    {
        super(sceneOid);
        this.sceneId = sceneId;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        sceneId = ins.readInt();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(sceneId);
    }
}
}
