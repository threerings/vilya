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

package com.threerings.ezgame.data;

import com.threerings.util.ActionScript;

/**
 * Models a paramter that can be used to load the contents of a file into a byte array or string
 * and ship it to the server.
 */
@ActionScript(omit=true)
public class FileParameter extends Parameter
{
    /** Whether or not the contents of the file should be supplied as binary data. If false, the
     * file will be loaded as text in the platform default encoding . */
    public boolean binary = false;

    @Override // documentation inherited
    public String getLabel ()
    {
        return "m.file_" + ident;
    }

    @Override // documentation inherited
    public Object getDefaultValue ()
    {
        if (binary) {
            return new byte[0];
        } else {
            return "";
        }
    }
}
