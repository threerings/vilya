package com.threerings.parlor.client {

import com.threerings.presents.client.InvocationService_InvocationListener;

/**
 * Used to communicate responses to {@link #invite} requests.
 */
public interface ParlorService_InviteListener
    extends InvocationService_InvocationListener
{
    /**
     * Called in response to a successful {@link #invite} request.
     */
    function inviteReceived (inviteId :int) :void;
}
}
