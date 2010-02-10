//
// $Id$

package com.threerings.parlor.data;

import com.threerings.util.ActionScript;

/**
 * Models a parameter that allows the toggling of a single value.
 */
public class ToggleParameter extends Parameter
{
    /** The starting state for this parameter. */
    public boolean start;

    @Override @ActionScript(omit=true)
    public String getLabel ()
    {
        return "m.toggle_" + ident;
    }

    @Override
    public Object getDefaultValue ()
    {
        return start;
    }
}
