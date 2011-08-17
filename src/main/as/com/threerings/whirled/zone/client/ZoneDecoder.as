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

package com.threerings.whirled.zone.client {

import com.threerings.presents.client.InvocationDecoder;

import com.threerings.whirled.zone.client.ZoneReceiver;

/**
 * Dispatches calls to a {@link ZoneReceiver} instance.
 */
public class ZoneDecoder extends InvocationDecoder
{
    /** The generated hash code used to identify this receiver class. */
    public static const RECEIVER_CODE :String =
        "2d900cf54355111b4bb4befcdff42b82";

    /** The method id used to dispatch {@link ZoneReceiver#forcedMove}
     * notifications. */
    public static const FORCED_MOVE :int = 1;

    /**
     * Creates a decoder that may be registered to dispatch invocation
     * service notifications to the specified receiver.
     */
    public function ZoneDecoder (receiver :ZoneReceiver)
    {
        this.receiver = receiver;
    }

    // documentation inherited
    override public function getReceiverCode () :String
    {
        return RECEIVER_CODE;
    }

    // documentation inherited
    override public function dispatchNotification (methodId :int, args :Array) :void
    {
        switch (methodId) {
        case FORCED_MOVE:
            (receiver as ZoneReceiver).forcedMove(args[0] as int, args[1] as int);
            return;

        default:
            super.dispatchNotification(methodId, args);
        }
    }
}
}
