package com.threerings.parlor.client {

import com.threerings.presents.client.InvocationListener;

/**
 * Used to communicate responses to {@link #createTable}, {@link
 * #joinTable}, and {@link #leaveTable} requests.
 */
public interface ParlorService_TableListener extends InvocationListener
{
    function tableCreated (tableId :int) :void;
}
}
