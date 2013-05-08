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

import com.threerings.presents.data.InvocationCodes;

/**
 * Constants and codes relating to the tourney services.
 */
public interface TourneyCodes extends InvocationCodes
{
    /** Too late to join a tourney. */
    public static final String TOO_LATE = "m.too_late";

    /** User already in a tourney. */
    public static final String ALREADY_IN_TOURNEY = "m.already_in_tourney";

    /** User failed to meet the entry fee requirements. */
    public static final String FAILED_ENTRY_FEE = "m.failed_entry_fee";

    /** It's too late to leave the tourney. */
    public static final String TOO_LATE_LEAVE = "m.too_late_leave";

    /** User is not in the tourney. */
    public static final String NOT_IN_TOURNEY = "m.not_in_tourney";

    /** The tourney is already in progress. */
    public static final String ALREADY_IN_PROGRESS = "m.already_in_progress";

    /** This tourney has participating players. */
    public static final String HAS_PLAYERS = "m.has_players";

    /** The tournament was canceled. */
    public static final String CANCELLED = "m.cancelled";
}
