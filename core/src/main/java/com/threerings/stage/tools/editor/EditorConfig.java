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

package com.threerings.stage.tools.editor;

import com.samskivert.util.PrefsConfig;

/**
 * Provides access to configuration data for the editor.
 */
public class EditorConfig
{
    /** Provides access to config data for this package. */
    public static PrefsConfig config = new PrefsConfig("rsrc/config/stage/tools/editor");

    /**
     * Accessor method for getting the test tile directory.
     */
    public static String getTestTileDirectory ()
    {
        return config.getValue(TESTTILE_KEY, TESTTILE_DEF);
    }

    /**
     * Accessor method for setting the test tile directory.
     */
    public static void setTestTileDirectory (String newvalue)
    {
        config.setValue(TESTTILE_KEY, newvalue);
    }

    private static final String TESTTILE_KEY = "testtiledir";
    private static final String TESTTILE_DEF = ".";
}
