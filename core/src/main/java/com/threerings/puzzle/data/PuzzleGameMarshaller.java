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

package com.threerings.puzzle.data;

import javax.annotation.Generated;

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.puzzle.client.PuzzleGameService;

/**
 * Provides the implementation of the {@link PuzzleGameService} interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
@Generated(value={"com.threerings.presents.tools.GenServiceTask"},
           comments="Derived from PuzzleGameService.java.")
public class PuzzleGameMarshaller extends InvocationMarshaller
    implements PuzzleGameService
{
    /** The method id used to dispatch {@link #updateProgress} requests. */
    public static final int UPDATE_PROGRESS = 1;

    // from interface PuzzleGameService
    public void updateProgress (int arg1, int[] arg2)
    {
        sendRequest(UPDATE_PROGRESS, new Object[] {
            Integer.valueOf(arg1), arg2
        });
    }

    /** The method id used to dispatch {@link #updateProgressSync} requests. */
    public static final int UPDATE_PROGRESS_SYNC = 2;

    // from interface PuzzleGameService
    public void updateProgressSync (int arg1, int[] arg2, Board[] arg3)
    {
        sendRequest(UPDATE_PROGRESS_SYNC, new Object[] {
            Integer.valueOf(arg1), arg2, arg3
        });
    }
}
