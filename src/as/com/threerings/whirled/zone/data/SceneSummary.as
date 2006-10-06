//
// $Id: SceneSummary.java 3310 2005-01-24 23:08:21Z mdb $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.whirled.zone.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;
import com.threerings.io.TypedArray;

/**
 * The scene summary class is used to provide info about the connected
 * group of scenes that make up a zone. The group of scenes that make up a
 * zone is a self-contained set of scenes, connected with one another (by
 * portals) but not to any scenes outside the group.
 */
public class SceneSummary
    implements Streamable
{
    /** The id of this scene. */
    public var sceneId :int;

    /** The name of this scene. */
    public var name :String;

    /** The ids of the scenes to which this scene is connected via
     * portals. */
    public var neighbors :TypedArray;

    /** The directions in which each of the neighbors lay. */
    public var neighborDirs :TypedArray;

    public function SceneSummary ()
    {
        // nothing needed
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return "[sceneId=" + sceneId + ", name=" + name + "]";
    }

    // from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        sceneId = ins.readInt();
        name = (ins.readField(String) as String);
        neighbors = (ins.readObject() as TypedArray);
        neighborDirs = (ins.readObject() as TypedArray);
    }

    // from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(sceneId);
        out.writeField(name);
        out.writeObject(neighbors);
        out.writeObject(neighborDirs);
    }
}
}
