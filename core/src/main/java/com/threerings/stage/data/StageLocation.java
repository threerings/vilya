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

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;

import com.threerings.whirled.spot.data.Location;

/**
 * Contains information on a scene occupant's position and orientation.
 */
public class StageLocation extends SimpleStreamableObject
    implements Location
{
    /** The user's x position (interpreted by the display system). */
    public int x;

    /** The user's y position (interpreted by the display system). */
    public int y;

    /** The user's orientation (defined by {@link DirectionCodes}). */
    public byte orient;

    /** {@link Object#toString} helper function. */
    public String orientToString ()
    {
        return DirectionUtil.toShortString(orient);
    }

    /**
     * A zero-argument constructor used when unserializing instances.
     */
    public StageLocation ()
    {
    }

    /**
     * Constructs a location with the specified coordinates and
     * orientation.
     */
    public StageLocation (int x, int y, byte orient)
    {
        this.x = x;
        this.y = y;
        this.orient = orient;

        if (orient == -1) {
            Thread.dumpStack();
        }
    }

    // documentation inherited from interface Location
    public Location getOpposite ()
    {
        StageLocation opp = clone();
        opp.orient = (byte) DirectionUtil.getOpposite(orient);
        return opp;
    }

    /**
     * Location equivalence means that the coordinates and orientation are
     * the same.
     */
    public boolean equivalent (Location oloc)
    {
        return equals(oloc) && (orient == ((StageLocation) oloc).orient);
    }

    /**
     * Location equality is determined by coordinates.
     */
    @Override
    public boolean equals (Object other)
    {
        if (other instanceof StageLocation) {
            StageLocation that = (StageLocation) other;
            return (this.x == that.x) && (this.y == that.y);

        } else {
            return false;
        }
    }

    /**
     * Computes a reasonable hashcode for location instances.
     */
    @Override
    public int hashCode ()
    {
        return x ^ y;
    }

    @Override
    public StageLocation clone ()
    {
        try {
            return (StageLocation) super.clone();
        } catch (CloneNotSupportedException cnse) {
            throw new AssertionError(cnse);
        }
    }
}
