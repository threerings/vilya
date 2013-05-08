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

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Update to change the default colorization for objects in a scene which
 * do not define their own colorization.
 */
public class DefaultColorUpdate extends SceneUpdate
{
    /** The class id of the colorization we're changing. */
    public int classId;

    /** The color id to set as the new default, or -1 to remove the default. */
    public int colorId;

    /**
     * Initializes this update.
     */
    public void init (int targetId, int targetVersion, int classId, int colorId)
    {
        init(targetId, targetVersion);
        this.classId = classId;
        this.colorId = colorId;
    }

    @Override
    public void apply (SceneModel model)
    {
        super.apply(model);

        StageSceneModel smodel = (StageSceneModel)model;
        smodel.setDefaultColor(classId, colorId);
    }
}
