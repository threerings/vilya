//
// $Id$

package com.threerings.parlor.game.data {

import com.threerings.util.Name;

public interface UserIdentifier
{
    /**
     * Returns the id of the specified user, or 0 if they're not valid.
     */
    function getUserId (name :Name) :int;
}
}
