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

package com.threerings.whirled.spot.data;

import javax.annotation.Generated;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.OidList;

import com.threerings.crowd.chat.data.SpeakObject;
import com.threerings.crowd.chat.data.UserMessage;

/**
 * Used to dispatch chat in clusters.
 */
public class ClusterObject extends DObject
    implements SpeakObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>occupants</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String OCCUPANTS = "occupants";
    // AUTO-GENERATED: FIELDS END

    /**
     * Tracks the oid of the body objects that occupy this cluster.
     */
    public OidList occupants = new OidList();

    // documentation inherited
    public void applyToListeners (ListenerOp op)
    {
        for (int ii = 0, ll = occupants.size(); ii < ll; ii++) {
            op.apply(this, occupants.get(ii));
        }
    }

    // from SpeakObject
    public String getChatIdentifier (UserMessage msg)
    {
        return SpeakObject.DEFAULT_IDENTIFIER;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that <code>oid</code> be added to the <code>occupants</code>
     * oid list. The list will not change until the event is actually
     * propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToOccupants (int oid)
    {
        requestOidAdd(OCCUPANTS, occupants, oid);
    }

    /**
     * Requests that <code>oid</code> be removed from the
     * <code>occupants</code> oid list. The list will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromOccupants (int oid)
    {
        requestOidRemove(OCCUPANTS, occupants, oid);
    }
    // AUTO-GENERATED: METHODS END
}
