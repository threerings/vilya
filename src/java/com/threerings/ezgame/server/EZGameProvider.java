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

package com.threerings.ezgame.server;

import com.threerings.ezgame.client.EZGameService;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.InvocationProvider;

/**
 * Defines the server-side of the {@link EZGameService}.
 */
public interface EZGameProvider extends InvocationProvider
{
    /**
     * Handles a {@link EZGameService#addToCollection} request.
     */
    public void addToCollection (ClientObject caller, String arg1, byte[][] arg2, boolean arg3, InvocationService.InvocationListener arg4)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#checkDictionaryWord} request.
     */
    public void checkDictionaryWord (ClientObject caller, String arg1, String arg2, InvocationService.ResultListener arg3)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#endGame} request.
     */
    public void endGame (ClientObject caller, int[] arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#endTurn} request.
     */
    public void endTurn (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#getCookie} request.
     */
    public void getCookie (ClientObject caller, int arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#getDictionaryLetterSet} request.
     */
    public void getDictionaryLetterSet (ClientObject caller, String arg1, int arg2, InvocationService.ResultListener arg3)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#getFromCollection} request.
     */
    public void getFromCollection (ClientObject caller, String arg1, boolean arg2, int arg3, String arg4, int arg5, InvocationService.ConfirmListener arg6)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#mergeCollection} request.
     */
    public void mergeCollection (ClientObject caller, String arg1, String arg2, InvocationService.InvocationListener arg3)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#sendMessage} request.
     */
    public void sendMessage (ClientObject caller, String arg1, Object arg2, int arg3, InvocationService.InvocationListener arg4)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#setCookie} request.
     */
    public void setCookie (ClientObject caller, byte[] arg1, InvocationService.InvocationListener arg2)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#setProperty} request.
     */
    public void setProperty (ClientObject caller, String arg1, Object arg2, int arg3, boolean arg4, Object arg5, InvocationService.InvocationListener arg6)
        throws InvocationException;

    /**
     * Handles a {@link EZGameService#setTicker} request.
     */
    public void setTicker (ClientObject caller, String arg1, int arg2, InvocationService.InvocationListener arg3)
        throws InvocationException;
}
