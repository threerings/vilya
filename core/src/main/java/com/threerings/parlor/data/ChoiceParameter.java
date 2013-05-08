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

import com.threerings.util.ActionScript;

/**
 * Models a parameter that allows the selection of one of a list of choices (specified as strings).
 */
public class ChoiceParameter extends Parameter
{
    /** The set of choices available for this parameter. */
    public String[] choices;

    /** The starting selection. */
    public String start;

    /**
     * Returns the translation key for the specified choice.
     */
    @ActionScript(omit=true)
    public String getChoiceLabel (int index)
    {
        return "m.choice_" + choices[index];
    }

    @Override @ActionScript(omit=true)
    public String getLabel ()
    {
        return "m.choice_" + ident;
    }

    @Override
    public Object getDefaultValue ()
    {
        return start;
    }
}
