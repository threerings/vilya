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

import com.threerings.io.SimpleStreamableObject;

import com.threerings.util.Name;

/**
 * Stores tournament configuration data.
 */
public class TourneyConfig extends SimpleStreamableObject
{
    /** Our unique tourney id. */
    public int tourneyId;

    /** The prize for this tourney. */
    public Prize prize;

    /** The fee to join the tourney. */
    public EntryFee entryFee;

    /** The tourney creator. */
    public Name creator;

    /** The minimum number of players needed for the tourney to start. */
    public int minPlayers;

    /** The number of minutes to wait for entrants before starting. */
    public int startsIn;

    /** The tournament logic runtime persistent data. */
    public TourneyLogicData logic;
}
