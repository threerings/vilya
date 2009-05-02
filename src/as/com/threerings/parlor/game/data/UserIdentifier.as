//
// $Id$

package com.threerings.parlor.game.data {

import com.threerings.util.Name;

public class UserIdentifier
{
    /**
     * Get the user id for the specified user, or 0 if they're not valid.
     */
    public static function getUserId (name :Name) :int
    {
        return (_userIder == null) ? 0 : _userIder(name);
    }

    public static function setIder (userIder :Function) :void
    {
        _userIder = userIder;
    }

    protected static var _userIder :Function;
}
}
