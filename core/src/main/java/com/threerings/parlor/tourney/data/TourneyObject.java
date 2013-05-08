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

package com.threerings.parlor.tourney.data;

import javax.annotation.Generated;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.DSet;

/**
 * Provides information on a specific tourney.
 */
public class TourneyObject extends DObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>state</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String STATE = "state";

    /** The field name of the <code>config</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String CONFIG = "config";

    /** The field name of the <code>startsIn</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String STARTS_IN = "startsIn";

    /** The field name of the <code>tourneyService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String TOURNEY_SERVICE = "tourneyService";

    /** The field name of the <code>participants</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String PARTICIPANTS = "participants";
    // AUTO-GENERATED: FIELDS END

    /** Tourney is pending and accepting participants. */
    public static final int PENDING = 0;

    /** Tourney is currently running. */
    public static final int RUNNING = 1;

    /** Tourney has been cancelled. */
    public static final int CANCELLED = 2;

    /** Tourney is paused. */
    public static final int PAUSED = 3;

    /** Tourney has completed. */
    public static final int FINISHED = 4;

    /** The current state of the tourney. */
    public int state = PENDING;

    /** The tourney configuration. */
    public TourneyConfig config;

    /** The real-time number of minutes before it starts. */
    public int startsIn;

    /** Provides the way in which tourney participants can communicate with the server. */
    public TourneyMarshaller tourneyService;

    /** A DSet that accumulates Participant records for the players involved in this tourney. */
    public DSet<Participant> participants = new DSet<Participant>();

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>state</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setState (int value)
    {
        int ovalue = this.state;
        requestAttributeChange(
            STATE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.state = value;
    }

    /**
     * Requests that the <code>config</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setConfig (TourneyConfig value)
    {
        TourneyConfig ovalue = this.config;
        requestAttributeChange(
            CONFIG, value, ovalue);
        this.config = value;
    }

    /**
     * Requests that the <code>startsIn</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setStartsIn (int value)
    {
        int ovalue = this.startsIn;
        requestAttributeChange(
            STARTS_IN, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.startsIn = value;
    }

    /**
     * Requests that the <code>tourneyService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setTourneyService (TourneyMarshaller value)
    {
        TourneyMarshaller ovalue = this.tourneyService;
        requestAttributeChange(
            TOURNEY_SERVICE, value, ovalue);
        this.tourneyService = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>participants</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void addToParticipants (Participant elem)
    {
        requestEntryAdd(PARTICIPANTS, participants, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>participants</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void removeFromParticipants (Comparable<?> key)
    {
        requestEntryRemove(PARTICIPANTS, participants, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>participants</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void updateParticipants (Participant elem)
    {
        requestEntryUpdate(PARTICIPANTS, participants, elem);
    }

    /**
     * Requests that the <code>participants</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setParticipants (DSet<Participant> value)
    {
        requestAttributeChange(PARTICIPANTS, value, this.participants);
        DSet<Participant> clone = (value == null) ? null : value.clone();
        this.participants = clone;
    }
    // AUTO-GENERATED: METHODS END
}
