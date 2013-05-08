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

import com.threerings.util.StreamableHashMap;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.spot.data.SpotSceneModel;

/**
 * Extends the basic scene model with the notion of a scene type and
 * incorporates the necessary auxiliary models used by the Stage system.
 */
public class StageSceneModel extends SceneModel
{
    /** A scene type code. */
    public static final String WORLD = "world";

    /** This scene's type which is a string identifier used to later
     * construct a specific controller to handle this scene. */
    public String type;

    /** The zone id to which this scene belongs. */
    public int zoneId;

    /** If non-null, contains default colorizations to use for objects
     * that do not have colorizations defined. */
    public StreamableHashMap<Integer, Integer> defaultColors;

    /**
     * Get the default color to use for the specified colorization
     * classId, or -1 if no default is set for that colorization.
     */
    public int getDefaultColor (int classId)
    {
        if (defaultColors != null) {
            Integer result = defaultColors.get(classId);
            if (result != null) {
                return result;
            }
        }
        return -1;
    }

    /**
     * Set the default colorId to use for a specified colorization
     * classId, or -1 to clear the default for that colorization.
     */
    public void setDefaultColor (int classId, int colorId)
    {
        if (colorId == -1) {
            if (defaultColors != null) {
                defaultColors.remove(classId);
                if (defaultColors.size() == 0) {
                    defaultColors = null;
                }
            }

        } else {
            if (defaultColors == null) {
                defaultColors = StreamableHashMap.newMap();
            }
            defaultColors.put(classId, colorId);
        }
    }

    /**
     * Creates and returns a blank scene model.
     */
    public static StageSceneModel blankStageSceneModel ()
    {
        StageSceneModel model = new StageSceneModel();
        populateBlankStageSceneModel(model);
        return model;
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static void populateBlankStageSceneModel (StageSceneModel model)
    {
        populateBlankSceneModel(model);
        model.addAuxModel(new SpotSceneModel());
        model.addAuxModel(new StageMisoSceneModel());
    }
}
