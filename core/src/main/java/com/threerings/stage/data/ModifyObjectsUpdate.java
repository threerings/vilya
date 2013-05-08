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

package com.threerings.stage.data;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * A scene update that is broadcast when objects have been added to or removed
 * from the scene.
 */
public class ModifyObjectsUpdate extends SceneUpdate
{
    /** The objects added to the scene (or <code>null</code> for none). */
    public ObjectInfo[] added;

    /** The objects removed from the scene (or <code>null</code> for none). */
    public ObjectInfo[] removed;

    /**
     * Initializes this update with all necessary data.
     *
     * @param added the objects added to the scene, or <code>null</code> for
     * none
     * @param removed the objects removed from the scene, or <code>null</code>
     * for none
     */
    public void init (int targetId, int targetVersion, ObjectInfo[] added,
                      ObjectInfo[] removed)
    {
        init(targetId, targetVersion);
        this.added = added;
        this.removed = removed;
    }

    @Override
    public void apply (SceneModel model)
    {
        super.apply(model);

        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(model);

        // wipe out the objects that need to go
        if (removed != null) {
            for (ObjectInfo element : removed) {
                mmodel.removeObject(element);
            }
        }

        // add the new objects
        if (added != null) {
            for (ObjectInfo element : added) {
                mmodel.addObject(element);
            }
        }
    }
}
