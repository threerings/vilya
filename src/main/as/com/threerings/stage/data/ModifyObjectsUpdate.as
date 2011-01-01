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

package com.threerings.stage.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.data.SceneModel;
import com.threerings.miso.data.ObjectInfo;

/**
 * A scene update that is broadcast when objects have been added to or removed
 * from the scene.
 */
public class ModifyObjectsUpdate extends SceneUpdate
{
    /** The objects added to the scene (or <code>null</code> for none). */
    public var added :TypedArray;

    /** The objects removed from the scene (or <code>null</code> for none). */
    public var removed :TypedArray;

    /**
     * Initializes this update with all necessary data.
     *
     * @param added the objects added to the scene, or <code>null</code> for
     * none
     * @param removed the objects removed from the scene, or <code>null</code>
     * for none
     */
    public function initUpdate (targetId :int, targetVersion :int, added :Array,
        removed :Array) :void
    {
        init(targetId, targetVersion);
        this.added = TypedArray.create(ObjectInfo, added);
        this.removed = TypedArray.create(ObjectInfo, removed);
    }

    override public function apply (model :SceneModel) :void
    {
        super.apply(model);

        var mmodel :StageMisoSceneModel = StageMisoSceneModel.getSceneModel(model);

        // wipe out the objects that need to go
        if (removed != null) {
            for each (var rmElement :ObjectInfo in removed) {
                mmodel.removeObject(rmElement);
            }
        }

        // add the new objects
        if (added != null) {
            for each (var addElement :ObjectInfo in added) {
                mmodel.addObject(addElement);
            }
        }
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        added = ins.readObject(TypedArray);
        removed = ins.readObject(TypedArray);
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(added);
        out.writeObject(removed);
    }

}
}
