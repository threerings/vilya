//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.puzzle.data {

import com.threerings.io.TypedArray;
import com.threerings.presents.client.Client;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.puzzle.client.PuzzleGameService;
import com.threerings.util.Integer;

/**
 * Provides the implementation of the <code>PuzzleGameService</code> interface
 * that marshalls the arguments and delivers the request to the provider
 * on the server. Also provides an implementation of the response listener
 * interfaces that marshall the response arguments and deliver them back
 * to the requesting client.
 */
public class PuzzleGameMarshaller extends InvocationMarshaller
    implements PuzzleGameService
{
    /** The method id used to dispatch <code>updateProgress</code> requests. */
    public static const UPDATE_PROGRESS :int = 1;

    // from interface PuzzleGameService
    public function updateProgress (arg1 :int, arg2 :TypedArray /* of int */) :void
    {
        sendRequest(UPDATE_PROGRESS, [
            Integer.valueOf(arg1), arg2
        ]);
    }

    /** The method id used to dispatch <code>updateProgressSync</code> requests. */
    public static const UPDATE_PROGRESS_SYNC :int = 2;

    // from interface PuzzleGameService
    public function updateProgressSync (arg1 :int, arg2 :TypedArray /* of int */, arg3 :TypedArray /* of class com.threerings.puzzle.data.Board */) :void
    {
        sendRequest(UPDATE_PROGRESS_SYNC, [
            Integer.valueOf(arg1), arg2, arg3
        ]);
    }
}
}
