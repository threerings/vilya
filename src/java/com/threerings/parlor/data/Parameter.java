//
// $Id$

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

    /** Generates a string representation of this instance. */
    public String toString ()
    {
        return StringUtil.fieldsToString(this);
    }
}
