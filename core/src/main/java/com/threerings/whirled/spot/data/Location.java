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

import com.threerings.io.Streamable;

public interface Location extends Streamable, Cloneable
{
    /**
     * Get a new Location instance that is equals() to this one but that
     * has an orientation facing the opposite direction.
     */
    public Location getOpposite ();

    /**
     * Two locations are equivalent if they specify the same location
     * and orientation.
     */
    public boolean equivalent (Location other);

    /**
     * Two locations are equals if they specify the same coordinates, but
     * the orientation may be different.
     */
    public boolean equals (Object other);

    /**
     * The hashcode of a Location should be based only on its coordinates.
     */
    public int hashCode ();

    /**
     * Locations are cloneable.
     */
    public Location clone ();
}
