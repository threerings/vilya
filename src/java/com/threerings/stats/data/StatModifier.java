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

package com.threerings.stats.data;

import java.io.Serializable;

/**
 * Encapsulates a modification to a single stat that can be serialized and sent to a different
 * server, if needed, to update stats loaded at runtime.
 */
public abstract class StatModifier<T extends Stat> implements Serializable
{
    /**
     * Creates a modifier that will operate on the supplied stat type. Note that this type may be
     * serialized and shipped between servers.
     */
    public StatModifier (Stat.Type type)
    {
        _type = type;
    }

    /**
     * Returns the {@link Stat.Type} of the stat being modified.
     */
    public Stat.Type getType ()
    {
        return _type;
    }

    /**
     * Applies the modification to the stat in question.
     */
    public abstract void modify (T stat);

    /** The type of the stat on which we're operating. */
    protected Stat.Type _type;
}
