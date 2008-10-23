//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.data {

import com.threerings.presents.data.InvocationCodes;

/**
 * Contains codes used by the parlor invocation services.
 */
public class ParlorCodes extends InvocationCodes
{
    /** Defines our invocation services group. */
    public static const PARLOR_GROUP :String = "parlor";

    /** The response code for an accepted invitation. */
    public static const INVITATION_ACCEPTED :int = 0;

    /** The response code for a refused invitation. */
    public static const INVITATION_REFUSED :int = 1;

    /** The response code for a countered invitation. */
    public static const INVITATION_COUNTERED :int = 2;

    /** An error code explaining that an invitation was rejected because the invited user was not
     * online at the time the invitation was received. */
    public static const INVITEE_NOT_ONLINE :String = "m.invitee_not_online";

    /** An error code returned when a user requests to join a table that
     * doesn't exist. */
    public static const NO_SUCH_TABLE :String = "m.no_such_table";

    /** An error code returned by the table services. */
    public static const MUST_BE_CREATOR :String = "m.must_be_creator";

    /** An error code returned by the table services. */
    public static const NO_SELF_BOOT :String = "m.no_self_boot";

    /** An error code returned by the table services. */
    public static const TABLE_POSITION_OCCUPIED :String = "m.table_position_occupied";

    /** An error code returned by the table services when a user requests to create or join a table
     * but they're already sitting at another table. */
    public static const ALREADY_AT_TABLE :String = "m.already_at_table";

    /** An error code returned by the table services when a user requests to leave a table that
     * they were not sitting at in the first place. */
    public static const NOT_AT_TABLE :String = "m.not_at_table";

    /** An error code returned by the table services for a request to join a table that the
     * requester been banned from. */
    public static const BANNED_FROM_TABLE :String = "m.banned_from_table";
}
}
