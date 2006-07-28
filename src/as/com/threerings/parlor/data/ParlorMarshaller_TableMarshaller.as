package com.threerings.parlor.data {

import com.threerings.util.Integer;

import com.threerings.presents.data.InvocationMarshaller_ListenerMarshaller;

public class ParlorMarshaller_TableMarshaller
    extends InvocationMarshaller_ListenerMarshaller
    implements TableListener
{
    /** The method id used to dispatch {@link #tableCreated}
     * responses. */
    public static const TABLE_CREATED :int = 1;

    // documentation inherited from interface
    public function tableCreated (arg1 :int) :void
    {
        _invId = null;
        omgr.postEvent(new InvocationResponseEvent(
                           callerOid, requestId, TABLE_CREATED, 
                           [ Integer.valueOf(arg1) ]));
    }

    // documentation inherited
    override public function dispatchResponse (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case TABLE_CREATED:
            (listener as TableListener).tableCreated(
                (args[0] as Integer).value);
            return;

        default:
            super.dispatchResponse(methodId, args);
            return;
        }
    }
}
}
