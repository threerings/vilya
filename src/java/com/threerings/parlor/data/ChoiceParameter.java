//
// $Id$

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

    @Override @ActionScript(omit=true) // documentation inherited
    public String getLabel ()
    {
        return "m.choice_" + ident;
    }

    @Override // documentation inherited
    public Object getDefaultValue ()
    {
        return start;
    }
}
