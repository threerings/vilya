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

package com.threerings.stage.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Update to change the default colorization for objects in a scene which
 * do not define their own colorization.
 */
public class DefaultColorUpdate extends SceneUpdate
{
    /** The class id of the colorization we're changing. */
    public var classId :int;

    /** The color id to set as the new default, or -1 to remove the default. */
    public var colorId :int;

    /**
     * Initializes this update.
     */
    public function initUpdate (targetId :int, targetVersion :int,
        classId :int, colorId :int) :void
    {
        init(targetId, targetVersion);
        this.classId = classId;
        this.colorId = colorId;
    }

    override public function apply (model :SceneModel) :void
    {
        super.apply(model);

        var smodel :StageSceneModel = StageSceneModel(model);
        smodel.setDefaultColor(classId, colorId);
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        classId = ins.readInt();
        colorId = ins.readInt();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(classId);
        out.writeInt(colorId);
    }

}
}
