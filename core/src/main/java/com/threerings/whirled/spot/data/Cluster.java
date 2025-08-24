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

import java.awt.Rectangle;
import java.io.IOException;

import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.util.ActionScript;

import com.threerings.presents.dobj.DSet;

/**
 * Contains information on clusters.
 */
public class Cluster extends Rectangle
    implements DSet.Entry
{
    /** A unique identifier for this cluster (also the distributed object
     * id of the cluster chat object). */
    public int clusterOid;

    // documentation inherited
    public Comparable<?> getKey ()
    {
        if (_key == null) {
            _key = Integer.valueOf(clusterOid);
        }
        return _key;
    }

    /**
     * Writes our streamable fields. This is needed because we can no longer reflectively access the
     * internals of java.awt.Rectangle.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(this.x);
        out.writeInt(this.y);
        out.writeInt(this.width);
        out.writeInt(this.height);
        out.writeInt(this.clusterOid);
    }

    /**
     * Reads our streamable fields. This is needed because we can no longer reflectively access the
     * internals of java.awt.Rectangle.
     */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        this.setBounds(in.readInt(), in.readInt(), in.readInt(), in.readInt());
        this.clusterOid = in.readInt();
    }

    @Override
    @ActionScript(omit=true)
    public boolean equals (Object other)
    {
        if (other instanceof Cluster) {
            return ((Cluster)other).clusterOid == clusterOid;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode ()
    {
        return clusterOid;
    }

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }

    /** Used for {@link #getKey}. */
    protected transient Integer _key;
}
