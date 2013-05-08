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

import com.threerings.miso.data.SparseMisoSceneModel;
import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;

/**
 * Extends the {@link SparseMisoSceneModel} with the necessary interface
 * to wire it up to the Whirled auxiliary model system.
 */
public class StageMisoSceneModel extends SparseMisoSceneModel
    implements AuxModel
{
    /** The width (in tiles) of a scene section. */
    public static final int SECTION_WIDTH = 9;

    /** The height (in tiles) of a scene section. */
    public static final int SECTION_HEIGHT = 9;

    /**
     * Creates a completely uninitialized scene model.
     */
    public StageMisoSceneModel ()
    {
        super(SECTION_WIDTH, SECTION_HEIGHT);
    }

    /**
     * Locates and returns the {@link StageMisoSceneModel} among the
     * auxiliary scene models associated with the supplied scene model.
     * <code>null</code> is returned if no miso scene model could be
     * found.
     */
    public static StageMisoSceneModel getSceneModel (SceneModel model)
    {
        for (AuxModel auxModel : model.auxModels) {
            if (auxModel instanceof StageMisoSceneModel) {
                return (StageMisoSceneModel)auxModel;
            }
        }
        return null;
    }

    /**
     * Returns the section key for the specified tile coordinate.
     */
    public int getSectionKey (int x, int y)
    {
        return key(x, y);
    }

    /**
     * Returns the section identified by the specified key, or null if no
     * section exists for that key.
     */
    public Section getSection (int key)
    {
        return _sections.get(key);
    }

    @Override
    public StageMisoSceneModel clone ()
    {
        return (StageMisoSceneModel)super.clone();
    }
}
