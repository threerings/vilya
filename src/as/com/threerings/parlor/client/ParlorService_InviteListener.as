//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

import com.threerings.util.*; // for Float, Integer, etc.

import com.threerings.parlor.client.ParlorService;
import com.threerings.parlor.client.ParlorService_InviteListener;
import com.threerings.parlor.client.ParlorService_TableListener;
import com.threerings.parlor.data.ParlorMarshaller_InviteMarshaller;
import com.threerings.parlor.data.ParlorMarshaller_TableMarshaller;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;
import com.threerings.presents.data.InvocationMarshaller_InvocationMarshaller;
import com.threerings.util.Name;

/**
 * An ActionScript version of the Java ParlorService_InviteListener interface.
 */
public interface ParlorService_InviteListener
    extends InvocationService_InvocationListener
{
    // from Java ParlorService_InviteListener
    function inviteReceived (arg1 :int) :void
}
}
