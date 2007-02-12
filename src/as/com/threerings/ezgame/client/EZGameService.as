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

package com.threerings.ezgame.client {

import flash.utils.ByteArray;
import com.threerings.ezgame.client.EZGameService;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_ConfirmListener;
import com.threerings.presents.client.InvocationService_InvocationListener;
import com.threerings.presents.client.InvocationService_ResultListener;
import com.threerings.presents.data.InvocationMarshaller_ConfirmMarshaller;
import com.threerings.presents.data.InvocationMarshaller_ResultMarshaller;

/**
 * An ActionScript version of the Java EZGameService interface.
 */
public interface EZGameService extends InvocationService
{
    // from Java interface EZGameService
    function addToCollection (arg1 :Client, arg2 :String, arg3 :Array, arg4 :Boolean, arg5 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function checkDictionaryWord (arg1 :Client, arg2 :String, arg3 :String, arg4 :InvocationService_ResultListener) :void;

    // from Java interface EZGameService
    function endGame (arg1 :Client, arg2 :Array, arg3 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function endTurn (arg1 :Client, arg2 :int, arg3 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function getCookie (arg1 :Client, arg2 :int, arg3 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function getDictionaryLetterSet (arg1 :Client, arg2 :String, arg3 :int, arg4 :InvocationService_ResultListener) :void;

    // from Java interface EZGameService
    function getFromCollection (arg1 :Client, arg2 :String, arg3 :Boolean, arg4 :int, arg5 :String, arg6 :int, arg7 :InvocationService_ConfirmListener) :void;

    // from Java interface EZGameService
    function mergeCollection (arg1 :Client, arg2 :String, arg3 :String, arg4 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function sendMessage (arg1 :Client, arg2 :String, arg3 :Object, arg4 :int, arg5 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function setCookie (arg1 :Client, arg2 :ByteArray, arg3 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function setProperty (arg1 :Client, arg2 :String, arg3 :Object, arg4 :int, arg5 :InvocationService_InvocationListener) :void;

    // from Java interface EZGameService
    function setTicker (arg1 :Client, arg2 :String, arg3 :int, arg4 :InvocationService_InvocationListener) :void;
}
}
