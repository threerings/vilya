//
// $Id$

package com.threerings.parlor.game.data;

import com.threerings.util.Name;

public class UserIdentifier
{
    /**
     * Implement this and set the mutha up.
     */
    public interface Ider
    {
        int getUserId (Name name);
    }

    /**
     * Returns the id of the specified user, or 0 if they're not valid.
     */
    public static int getUserId (Name name)
    {
        return (_userIder == null) ? 0 : _userIder.getUserId(name);
    }

    /**
     * Set the global user identifier to use.
     */
    public static void setIder (Ider userIder)
    {
        _userIder = userIder;
    }

    protected static Ider _userIder;
}
