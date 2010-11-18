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

package com.threerings.whirled.spot.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * A scene update to add/remove portals.
 */
public class ModifyPortalsUpdate extends SceneUpdate
{
    /** The portals to be removed from the room. */
    public var portalsRemoved :TypedArray;

    /** The portals to be added to the scene. */
    public var portalsAdded :TypedArray;

    public function ModifyPortalsUpdate ()
    {
        // nothing needed
    }

    override public function apply (model :SceneModel) :void
    {
        super.apply(model);

        // extract the spot scene model
        var spotModel :SpotSceneModel = SpotSceneModel.getSceneModel(model);

        var portal :Portal;
        if (portalsRemoved != null) {
            for each (portal in portalsRemoved) {
                spotModel.removePortal(portal);
            }
        }
        if (portalsAdded != null) {
            for each (portal in portalsAdded) {
                spotModel.addPortal(portal);
            }
        }
    }

    /**
     * Initialize the update with all necessary data.
     */
    public function initialize (
            targetId :int, targetVersion :int, removed :TypedArray,
            added :TypedArray) :void
    {
        init(targetId, targetVersion);

        portalsRemoved = removed;
        portalsAdded = added;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        portalsRemoved = TypedArray(ins.readObject());
        portalsAdded = TypedArray(ins.readObject());
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(portalsRemoved);
        out.writeObject(portalsAdded);
    }
}
}
