//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
//
// This library is free software; you can redistribute it and/or modify it
// under the terms of the GNU Lesser General Public License as published
// by the Free Software Foundation; either version 2.1 of the License, or
// (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA

package com.threerings.parlor.card.trick.data;

import javax.annotation.Generated;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.trick.client.TrickCardGameService;
import com.threerings.presents.data.InvocationMarshaller;

/**
 * Provides the implementation of the {@link TrickCardGameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from TrickCardGameService.java.")
public class TrickCardGameMarshaller extends InvocationMarshaller
    implements TrickCardGameService
{
    /** The method id used to dispatch {@link #playCard} requests. */
    public static final int PLAY_CARD = 1;

    // from interface TrickCardGameService
    public void playCard (Card arg1, int arg2)
    {
        sendRequest(PLAY_CARD, new Object[] {
            arg1, Integer.valueOf(arg2)
        });
    }

    /** The method id used to dispatch {@link #requestRematch} requests. */
    public static final int REQUEST_REMATCH = 2;

    // from interface TrickCardGameService
    public void requestRematch ()
    {
        sendRequest(REQUEST_REMATCH, new Object[] {
        });
    }

    /** The method id used to dispatch {@link #sendCardsToPlayer} requests. */
    public static final int SEND_CARDS_TO_PLAYER = 3;

    // from interface TrickCardGameService
    public void sendCardsToPlayer (int arg1, Card[] arg2)
    {
        sendRequest(SEND_CARDS_TO_PLAYER, new Object[] {
            Integer.valueOf(arg1), arg2
        });
    }
}
