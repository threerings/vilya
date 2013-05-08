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

import com.samskivert.util.ResultListener;

import com.threerings.io.SimpleStreamableObject;

import com.threerings.crowd.data.BodyObject;

/**
 * Extensible entry fee class that specifies entry requirements for a tourney.
 */
public abstract class EntryFee extends SimpleStreamableObject
{
    /**
     * Returns a description of the entry fee.
     */
    public abstract String getDescription ();

    /**
     * Checks if the user has the required entry fee.
     */
    public abstract boolean hasFee (BodyObject body);

    /**
     * Attempts to reserve the entry fee.
     */
    public abstract void reserveFee (BodyObject body, ResultListener<Void> listener);

    /**
     * Returns the entry fee.
     */
    public abstract void returnFee (BodyObject body);
}
