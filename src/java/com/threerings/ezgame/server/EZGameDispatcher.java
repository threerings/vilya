//
// $Id$

package com.threerings.ezgame.server;

import com.threerings.ezgame.client.EZGameService;
import com.threerings.ezgame.data.EZGameMarshaller;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationDispatcher;
import com.threerings.presents.server.InvocationException;

/**
 * Dispatches requests to the {@link EZGameProvider}.
 */
public class EZGameDispatcher extends InvocationDispatcher
{
    /**
     * Creates a dispatcher that may be registered to dispatch invocation
     * service requests for the specified provider.
     */
    public EZGameDispatcher (EZGameProvider provider)
    {
        this.provider = provider;
    }

    // documentation inherited
    public InvocationMarshaller createMarshaller ()
    {
        return new EZGameMarshaller();
    }

    // documentation inherited
    public void dispatchRequest (
        ClientObject source, int methodId, Object[] args)
        throws InvocationException
    {
        switch (methodId) {
        case EZGameMarshaller.ADD_TO_COLLECTION:
            ((EZGameProvider)provider).addToCollection(
                source,
                (String)args[0], (byte[][])args[1], ((Boolean)args[2]).booleanValue(), (InvocationService.InvocationListener)args[3]
            );
            return;

        case EZGameMarshaller.END_GAME:
            ((EZGameProvider)provider).endGame(
                source,
                (int[])args[0], (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.END_TURN:
            ((EZGameProvider)provider).endTurn(
                source,
                ((Integer)args[0]).intValue(), (InvocationService.InvocationListener)args[1]
            );
            return;

        case EZGameMarshaller.GET_FROM_COLLECTION:
            ((EZGameProvider)provider).getFromCollection(
                source,
                (String)args[0], ((Boolean)args[1]).booleanValue(), ((Integer)args[2]).intValue(), (String)args[3], ((Integer)args[4]).intValue(), (InvocationService.ConfirmListener)args[5]
            );
            return;

        case EZGameMarshaller.MERGE_COLLECTION:
            ((EZGameProvider)provider).mergeCollection(
                source,
                (String)args[0], (String)args[1], (InvocationService.InvocationListener)args[2]
            );
            return;

        case EZGameMarshaller.SEND_MESSAGE:
            ((EZGameProvider)provider).sendMessage(
                source,
                (String)args[0], (Object)args[1], ((Integer)args[2]).intValue(), (InvocationService.InvocationListener)args[3]
            );
            return;

        case EZGameMarshaller.SET_PROPERTY:
            ((EZGameProvider)provider).setProperty(
                source,
                (String)args[0], (Object)args[1], ((Integer)args[2]).intValue(), (InvocationService.InvocationListener)args[3]
            );
            return;

        default:
            super.dispatchRequest(source, methodId, args);
            return;
        }
    }
}
