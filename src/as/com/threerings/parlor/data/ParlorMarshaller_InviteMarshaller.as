package com.threerings.parlor.data {

import com.threerings.util.Integer;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

public class ParlorMarshaller_InviteMarshaller
    extends InvocationMarshaller_ListenerMarshaller
    implements InviteListener
{
    /** The method id used to dispatch {@link #inviteReceived}
     * responses. */
    public static const INVITE_RECEIVED :int = 1;

    // documentation inherited from interface
    public function inviteReceived (arg1 :int) :void
    {
        _invId = null;
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, INVITE_RECEIVED,
                           [ Integer.valueOf(arg1) ]));
    }

    // documentation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case INVITE_RECEIVED:
            (listener as InviteListener).inviteReceived(
                (args[0] as Integer).value);
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
