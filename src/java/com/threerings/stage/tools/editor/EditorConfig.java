//
// $Id$

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
