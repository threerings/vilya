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

package com.threerings.stage.data {

import com.threerings.miso.data.SparseMisoSceneModel;
import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.miso.data.SparseMisoSceneModel_Section;
import com.threerings.util.Integer;
import com.threerings.stage.data.StageMisoSceneModel;

/**
 * Extends the {@link SparseMisoSceneModel} with the necessary interface
 * to wire it up to the Whirled auxiliary model system.
 */
public class StageMisoSceneModel extends SparseMisoSceneModel
    implements AuxModel
{
    /** The width (in tiles) of a scene section. */
    public static const SECTION_WIDTH :int = 9;

    /** The height (in tiles) of a scene section. */
    public static const SECTION_HEIGHT :int = 9;

    /**
     * Locates and returns the {@link StageMisoSceneModel} among the
     * auxiliary scene models associated with the supplied scene model.
     * <code>null</code> is returned if no miso scene model could be
     * found.
     */
    public static function getSceneModel (model :SceneModel) :StageMisoSceneModel
    {
        for each (var auxModel :AuxModel in model.auxModels) {
            if (auxModel is StageMisoSceneModel) {
                return StageMisoSceneModel(auxModel);
            }
        }
        return null;
    }

    override public function clone () :Object
    {
        return StageMisoSceneModel(super.clone());
    }

    /**
     * Returns the section identified by the specified key, or null if no
     * section exists for that key.
     */
    public function getSectionByKey (key :int) :SparseMisoSceneModel_Section
    {
        return _sections.get(key);
    }

    /**
     * Returns the section key for the specified tile coordinate.
     */
    public function getSectionKey (x :int, y :int) :Integer
    {
        return key(x, y);
    }

}
}
