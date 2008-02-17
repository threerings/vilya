//
// $Id$

package com.threerings.parlor.data;

import com.threerings.util.ActionScript;

/**
 * Models a parameter that can contain an integer value in a specified range.
 */
public class RangeParameter extends Parameter
{
    /** The minimum value of this parameter. */
    public int minimum;

    /** The maximum value of this parameter. */
    public int maximum;

    /** The starting value for this parameter. */
    public int start;

    @Override @ActionScript(omit=true) // documentation inherited
    public String getLabel ()
    {
        return "m.range_" + ident;
    }

    @Override // documentation inherited
    public Object getDefaultValue ()
    {
        return start;
    }
}
