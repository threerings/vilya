//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.client {

import com.threerings.parlor.client.ParlorReceiver;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.InvocationDecoder;
import com.threerings.util.Name;

/**
 * Dispatches calls to a {@link ParlorReceiver} instance.
 */
public class ParlorDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static const RECEIVER_CODE :String = "5ef9ee0d359c42a9024498ee9aad119a";

    /** The method id used to dispatch {@link ParlorReceiver#gameIsReady}
     * notifications. */
    public static const GAME_IS_READY :int = 1;

    /** The method id used to dispatch {@link ParlorReceiver#receivedInvite}
     * notifications. */
    public static const RECEIVED_INVITE :int = 2;

    /** The method id used to dispatch {@link ParlorReceiver#receivedInviteCancellation}
     * notifications. */
    public static const RECEIVED_INVITE_CANCELLATION :int = 3;

    /** The method id used to dispatch {@link ParlorReceiver#receivedInviteResponse}
     * notifications. */
    public static const RECEIVED_INVITE_RESPONSE :int = 4;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public function ParlorDecoder (receiver :ParlorReceiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    override public function getReceiverCode () :String
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    override public function dispatchNotification (
            methodId :int, args :Array) :void
    {
        var prec :ParlorReceiver = (receiver as ParlorReceiver);
        switch (methodId) {
        case GAME_IS_READY:
            prec.gameIsReady(args[0] as int);
            return;

        case RECEIVED_INVITE:
            prec.receivedInvite(
                (args[0] as int), (args[1] as Name),
                (args[2] as GameConfig)
            );
            return;

        case RECEIVED_INVITE_CANCELLATION:
            prec.receivedInviteCancellation(
                (args[0] as int)
            );
            return;

        case RECEIVED_INVITE_RESPONSE:
            prec.receivedInviteResponse(
                (args[0] as int), (args[1] as int), args[2]
            );
            return;

        default:
            super.dispatchNotification(methodId, args);
            return;
        }
    }
}
}
