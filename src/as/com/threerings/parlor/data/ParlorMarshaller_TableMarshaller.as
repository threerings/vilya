package com.threerings.parlor.data {

import com.threerings.util.Integer;

import com.threerings.presents.dobj.InvocationResponseEvent;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

import com.threerings.parlor.client.ParlorService_TableListener;

public class ParlorMarshaller_TableMarshaller
    extends InvocationMarshaller_ListenerMarshaller
    implements ParlorService_TableListener
{
    /** The method id used to dispatch {@link #tableCreated}
     * responses. */
    public static const TABLE_CREATED :int = 1;

    // documentation inherited from interface
    public function tableCreated (arg1 :int) :void
    {
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, TABLE_CREATED, 
                           [ Integer.valueOf(arg1) ]));
    }

    // documentation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case TABLE_CREATED:
            (listener as ParlorService_TableListener).tableCreated(
                (args[0] as Integer).value);
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
