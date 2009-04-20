//
// $Id$

package com.threerings.parlor.game.data;

import com.threerings.crowd.data.BodyObject;

public interface UserIdentifier
{
    /**
     * Returns the id of the specified user, or 0 if they're not valid.
     */
    int getUserId (BodyObject bodyObj);
}
