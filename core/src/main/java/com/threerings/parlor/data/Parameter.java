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

package com.threerings.parlor.data;

import com.samskivert.util.StringUtil;

import com.threerings.io.Streamable;

/**
 * Defines a configuration parameter for a game. Various derived classes exist that define
 * particular types of configuration parameters including choices, toggles, ranges, etc.
 */
public abstract class Parameter implements Streamable
{
    /** A string identifier that names this parameter. */
    public String ident;

    /** A human readable name for this configuration parameter. */
    public String name;

    /** A human readable tooltip to display when the mouse is hovered over this configuration
     * parameter. */
    public String tip;

    /** Returns the translation key for this parameter's label. */
    public abstract String getLabel ();

    /** Returns the default value of this parameter. */
    public abstract Object getDefaultValue ();

    @Override
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
