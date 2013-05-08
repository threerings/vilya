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

package com.threerings.whirled.spot.data;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * A scene update to add/remove portals.
 */
public class ModifyPortalsUpdate extends SceneUpdate
{
    /** The portals to be removed from the room. */
    public Portal[] portalsRemoved;

    /** The portals to be added to the scene. */
    public Portal[] portalsAdded;

    /**
     * Initialize the update with all necessary data.
     */
    public void initialize (
            int targetId, int targetVersion, Portal[] removed, Portal[] added)
    {
        init(targetId, targetVersion);

        portalsRemoved = removed;
        portalsAdded = added;
    }

    @Override
    public void apply (SceneModel model)
    {
        super.apply(model);

        // extract the spot scene model
        SpotSceneModel spotModel = SpotSceneModel.getSceneModel(model);

        if (portalsRemoved != null) {
            for (Portal portal : portalsRemoved) {
                spotModel.removePortal(portal);
            }
        }
        if (portalsAdded != null) {
            for (Portal portal : portalsAdded) {
                spotModel.addPortal(portal);
            }
        }
    }
}
