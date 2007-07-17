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

package com.threerings.whirled.server;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;

import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.data.SceneCodes;

import com.threerings.whirled.Log;

/**
 * Handles the basics of moving a client into a new scene, which may involve resolution. Takes care
 * of annoying edge cases like the client logging off before their target scene is resolved and can
 * be extended to handle extra fun stuff.
 */
public abstract class AbstractSceneMoveHandler
    implements SceneRegistry.ResolutionListener
{
    public AbstractSceneMoveHandler (BodyObject body, InvocationService.InvocationListener listener)
    {
        _body = body;
        _listener = listener;
    }

    // from interface SceneRegistry.ResolutionListener
    public void sceneWasResolved (SceneManager scmgr)
    {
        // make sure our caller is still around; under heavy load, clients might end their session
        // while the scene is resolving
        if (!_body.isActive()) {
            Log.info("Abandoning scene move, client gone [who=" + _body.who()  +
                     ", dest=" + scmgr.where() + "].");
            InvocationMarshaller.setNoResponse(_listener);
            return;
        }

        try {
            effectSceneMove(scmgr);

        } catch (InvocationException sfe) {
            _listener.requestFailed(sfe.getMessage());

        } catch (RuntimeException re) {
            Log.logStackTrace(re);
            _listener.requestFailed(SceneCodes.INTERNAL_ERROR);
        }
    }

    // from interface SceneRegistry.ResolutionListener
    public void sceneFailedToResolve (int sceneId, Exception reason)
    {
        Log.warning("Unable to resolve scene [sceneid=" + sceneId + ", reason=" + reason + "].");
        _listener.requestFailed(SceneCodes.NO_SUCH_PLACE);
    }

    protected abstract void effectSceneMove (SceneManager scmgr)
        throws InvocationException;

    protected BodyObject _body;
    protected InvocationService.InvocationListener _listener;
}
