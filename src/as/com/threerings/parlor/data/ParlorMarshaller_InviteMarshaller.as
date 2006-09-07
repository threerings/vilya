package com.threerings.parlor.data {

import com.threerings.util.Integer;

import com.threerings.presents.dobj.InvocationResponseEvent;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.parlor.client.ParlorService_InviteListener;

public class ParlorMarshaller_InviteMarshaller
    extends InvocationMarshaller_ListenerMarshaller
{
    /** The method id used to dispatch {@link #inviteReceived}
     * responses. */
    public static const INVITE_RECEIVED :int = 1;

    // documentation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case INVITE_RECEIVED:
            (listener as ParlorService_InviteListener).inviteReceived(
                (args[0] as Integer).value);
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
