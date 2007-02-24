//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.whirled.spot.data {

import com.threerings.util.ArrayUtil;
import com.threerings.util.ClassUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.SimpleStreamableObject;
import com.threerings.io.TypedArray;

import com.threerings.whirled.data.AuxModel;
import com.threerings.whirled.data.SceneModel;

/**
 * The spot scene model extends the standard scene model with information
 * on portals. Portals are referenced by an identifier, unique within the
 * scene and unchanging, so that portals can stably reference the target
 * portal in the scene to which they connect.
 */
public class SpotSceneModel extends SimpleStreamableObject
    implements AuxModel
{
    /** An array containing all portals in this scene. */
    public var portals :TypedArray = TypedArray.create(Portal);

    /** The portal id of the default entrance to this scene. If a body
     * enters the scene without coming from another scene, this is the
     * portal at which they would appear. */
    public var defaultEntranceId :int = -1;

    /**
     * Locates and returns the {@link SpotSceneModel} among the auxiliary
     * scene models associated with the supplied scene
     * model. <code>null</code> is returned if no spot scene model could
     * be found.
     */
    public static function getSceneModel (model :SceneModel) :SpotSceneModel
    {
        for each (var aux :AuxModel in model.auxModels) {
            if (aux is SpotSceneModel) {
                return (aux as SpotSceneModel);
            }
        }
        return null;
    }

    public function SpotSceneModel ()
    {
        // nothing needed
    }

    /**
     * Removes a portal from this model.
     */
    public function removePortal (portal :Portal) :void
    {
        ArrayUtil.removeFirst(portals, portal);
    }

    /**
     * Adds a portal to this scene model.
     */
    public function addPortal (portal :Portal) :void
    {
        portals.push(portal);
    }

    // documentation inherited from superinterface Cloneable
    public function clone () :Object
    {
        var clazz :Class = ClassUtil.getClass(this);
        var model :SpotSceneModel = new clazz();

        for each (var portal :Portal in portals) {
            model.portals.push(portal.clone());
        }
        return model;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        portals = (ins.readObject() as TypedArray);
        defaultEntranceId = ins.readInt();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeObject(portals);
        out.writeInt(defaultEntranceId);
    }
}
}
