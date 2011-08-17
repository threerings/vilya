//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.card.trick.data {

import com.threerings.io.TypedArray;

import com.threerings.util.Integer;

import com.threerings.presents.data.InvocationMarshaller;

import com.threerings.parlor.card.data.Card;
import com.threerings.parlor.card.trick.client.TrickCardGameService;

/**
 * Provides the implementation of the <code>TrickCardGameService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class TrickCardGameMarshaller extends InvocationMarshaller
    implements TrickCardGameService
{
    /** The method id used to dispatch <code>playCard</code> requests. */
    public static const PLAY_CARD :int = 1;

    // from interface TrickCardGameService
    public function playCard (arg1 :Card, arg2 :int) :void
    {
        sendRequest(PLAY_CARD, [
            arg1, Integer.valueOf(arg2)
        ]);
    }

    /** The method id used to dispatch <code>requestRematch</code> requests. */
    public static const REQUEST_REMATCH :int = 2;

    // from interface TrickCardGameService
    public function requestRematch () :void
    {
        sendRequest(REQUEST_REMATCH, [
            
        ]);
    }

    /** The method id used to dispatch <code>sendCardsToPlayer</code> requests. */
    public static const SEND_CARDS_TO_PLAYER :int = 3;

    // from interface TrickCardGameService
    public function sendCardsToPlayer (arg1 :int, arg2 :TypedArray /* of class com.threerings.parlor.card.data.Card */) :void
    {
        sendRequest(SEND_CARDS_TO_PLAYER, [
            Integer.valueOf(arg1), arg2
        ]);
    }
}
}
