//
// $Id: StageSceneModel.java 887 2010-01-05 22:12:02Z dhoover $
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.stage.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.stage.data.StageSceneModel;
import com.threerings.util.Integer;
import com.threerings.util.StreamableHashMap;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.data.SceneModel;

/**
 * Extends the basic scene model with the notion of a scene type and
 * incorporates the necessary auxiliary models used by the Stage system.
 */
public class StageSceneModel extends SceneModel
{
    /** A scene type code. */
    public static const WORLD :String = "world";

    /** This scene's type which is a string identifier used to later
     * construct a specific controller to handle this scene. */
    public var type :String;

    /** The zone id to which this scene belongs. */
    public var zoneId :int;

    /** If non-null, contains default colorizations to use for objects
     * that do not have colorizations defined. */
    public var defaultColors :StreamableHashMap;

    /**
     * Creates and returns a blank scene model.
     */
    public static function blankStageSceneModel () :StageSceneModel
    {
        var model :StageSceneModel = new StageSceneModel();
        populateBlankStageSceneModel(model);
        return model;
    }

    /**
     * Set the default colorId to use for a specified colorization
     * classId, or -1 to clear the default for that colorization.
     */
    public function setDefaultColor (classId :int, colorId :int) :void
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
                defaultColors = new StreamableHashMap();
            }
            defaultColors.put(classId, colorId);
        }
    }

    /**
     * Get the default color to use for the specified colorization
     * classId, or -1 if no default is set for that colorization.
     */
    public function getDefaultColor (classId :int) :int
    {
        if (defaultColors != null) {
            return defaultColors.get(new Integer(classId));
        }
        return -1;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        type = ins.readField(String);
        zoneId = ins.readInt();
        defaultColors = ins.readObject(StreamableHashMap);
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeField(type);
        out.writeInt(zoneId);
        out.writeObject(defaultColors);
    }

    /**
     * Populates a blank scene model with blank values.
     */
    protected static function populateBlankStageSceneModel (model :StageSceneModel) :void
    {
        populateBlankSceneModel(model);
        model.addAuxModel(new SpotSceneModel());
        model.addAuxModel(new StageMisoSceneModel());
    }

}
}
