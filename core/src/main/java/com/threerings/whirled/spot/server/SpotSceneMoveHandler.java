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

package com.threerings.whirled.spot.server;

import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.LocationManager;

import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneMoveHandler;
import com.threerings.whirled.spot.data.Portal;

/**
 * Moves a player between scenes, accounting for their exit and entry via portals.
 */
public class SpotSceneMoveHandler extends SceneMoveHandler
{
    public SpotSceneMoveHandler (LocationManager locman, SpotSceneManager srcmgr, BodyObject body,
                                 int sceneVer, Portal dest, SceneService.SceneMoveListener listener)
    {
        super(locman, body, sceneVer, listener);
        _srcmgr = srcmgr;
        _dest = dest;
    }

    @Override // from AbstractSceneMoveHandler
    protected void effectSceneMove (SceneManager scmgr)
        throws InvocationException
    {
        // let the source manager know that this guy is departing via the specified portal
        _srcmgr.willTraversePortal(_body, _dest);

        // let the destination scene manager know that we're coming in
        SpotSceneManager destmgr = (SpotSceneManager)scmgr;
        destmgr.mapEnteringBody(_body, _dest);

        try {
            super.effectSceneMove(destmgr);
        } catch (InvocationException ie) {
            // if anything goes haywire, clear out our entering status
            destmgr.clearEnteringBody(_body);
            throw ie;
        }
    }

    protected SpotSceneManager _srcmgr;
    protected Portal _dest;
}
