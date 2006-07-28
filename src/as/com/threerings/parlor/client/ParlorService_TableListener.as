package com.threerings.parlor.client {

import com.threerings.presents.client.InvocationService_InvocationListener;

/**
 * Used to communicate responses to {@link #createTable}, {@link
 * #joinTable}, and {@link #leaveTable} requests.
 */
public interface ParlorService_TableListener
    extends InvocationService_InvocationListener
{
    function tableCreated (tableId :int) :void;
}
}
