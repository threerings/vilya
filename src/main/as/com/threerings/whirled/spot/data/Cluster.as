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

package com.threerings.whirled.spot.data {

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.util.Hashable;

import com.threerings.presents.dobj.DSet_Entry;

/**
 * Contains information on clusters.
 */
public class Cluster
    implements DSet_Entry, Streamable, Hashable
{
    /** The bounding rectangle of this cluster. */
    public var x :int;
    public var y :int;
    public var width :int;
    public var height :int;

    /** A unique identifier for this cluster (also the distributed object
     * id of the cluster chat object). */
    public var clusterOid :int;

    public function Cluster ()
    {
        // nothing needed
    }

    // from Hashable
    public function hashCode () :int
    {
        return clusterOid;
    }

    // from Hashable
    public function equals (o :Object) :Boolean
    {
        return (o is Cluster) && ((o as Cluster).clusterOid == this.clusterOid);
    }

    /**
     * Generates a string representation of this instance.
     */
    public function toString () :String
    {
        return "x=" + x + ", y=" + y + ", width=" + width + ", height=" + height +
            ", clusterOid=" + clusterOid;
    }

    // documentation inherited from interface DSet_Entry
    public function getKey () :Object
    {
        return clusterOid;
    }

    // documentation inherited from interface Streamable
    public function readObject (ins :ObjectInputStream) :void
    {
        x = ins.readInt();
        y = ins.readInt();
        width = ins.readInt();
        height = ins.readInt();

        clusterOid = ins.readInt();
    }

    // documentation inherited from interface Streamable
    public function writeObject (out :ObjectOutputStream) :void
    {
        out.writeInt(x);
        out.writeInt(y);
        out.writeInt(width);
        out.writeInt(height);

        out.writeInt(clusterOid);
    }
}
}
