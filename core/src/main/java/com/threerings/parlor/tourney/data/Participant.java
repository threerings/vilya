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

package com.threerings.parlor.tourney.data;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DSet;

/**
 * Contains information on a particular tourney participant.
 */
public class Participant extends SimpleStreamableObject
    implements DSet.Entry, Comparable<Participant>
{
    /** The username of the participant. */
    public Name username;

    // documentation inherited from interface DSet.Entry
    public Comparable<?> getKey ()
    {
        return username;
    }

    // documentation inherited from interface Comparable
    public int compareTo (Participant op)
    {
        return username.compareTo(op.username);
    }

    @Override
    public boolean equals (Object other)
    {
        if (other instanceof Participant) {
            return ((Participant) other).username.equals(username);
        }
        return false;
    }

    @Override
    public int hashCode ()
    {
    	return username.hashCode();
    }

    @Override
    public String toString ()
    {
        return username.toString();
    }
}
