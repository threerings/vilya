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

import com.threerings.util.ClassUtil;
import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;
import com.threerings.util.Hashable;

import com.threerings.io.SimpleStreamableObject;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.stage.data.StageLocation;
import com.threerings.whirled.spot.data.Location;

/**
 * Contains information on a scene occupant's position and orientation.
 */
public class StageLocation extends SimpleStreamableObject
    implements Location, Hashable
{
    /** The user's x position (interpreted by the display system). */
    public var x :int;

    /** The user's y position (interpreted by the display system). */
    public var y :int;

    /** The user's orientation (defined by {@link DirectionCodes}). */
    public var orient :int;

    public function StageLocation (x :int = 0, y :int = 0, orient :int = 0)
    {
        this.x = x;
        this.y = y;
        this.orient = orient;
    }

    /**
     * Computes a reasonable hashcode for location instances.
     */

    public function hashCode () :int
    {
        return x ^ y;
    }

    public function clone () :Object
    {
        var newLoc :StageLocation = (ClassUtil.newInstance(this) as StageLocation);
        newLoc.x = x;
        newLoc.y = y;
        newLoc.orient = orient;
        return newLoc;
    }

    /**
     * Location equality is determined by coordinates.
     */
    public function equals (other :Object) :Boolean
    {
        if (other is StageLocation) {
            var that :StageLocation = StageLocation(other);
            return (this.x == that.x) && (this.y == that.y);

        } else {
            return false;
        }
    }

    /** {@link Object#toString} helper function. */
    public function orientToString () :String
    {
        return DirectionUtil.toShortString(orient);
    }

    // documentation inherited from interface Location
    public function getOpposite () :Location
    {
        var opp :StageLocation = StageLocation(clone());
        opp.orient = DirectionUtil.getOpposite(orient);
        return opp;
    }

    /**
     * Location equivalence means that the coordinates and orientation are
     * the same.
     */
    public function equivalent (oloc :Location) :Boolean
    {
        return equals(oloc) && (orient == (StageLocation(oloc)).orient);
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        x = ins.readInt();
        y = ins.readInt();
        orient = ins.readByte();
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(x);
        out.writeInt(y);
        out.writeByte(orient);
    }

}
}
