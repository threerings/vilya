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

import com.threerings.presents.data.InvocationMarshaller;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.CrowdServer;

import com.threerings.whirled.client.SceneService;
import com.threerings.whirled.data.SceneCodes;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;

/**
 * Handles a simple scene to scene move.
 */
public class SceneMoveHandler extends AbstractSceneMoveHandler
{
    public SceneMoveHandler (BodyObject body, int sceneVer, SceneService.SceneMoveListener listener)
    {
        super(body, listener);
        _version = sceneVer;
    }

    @Override // from AbstractSceneMoveHandler
    protected void effectSceneMove (SceneManager scmgr)
        throws InvocationException
    {
        // move to location associated with this scene
        int ploid = scmgr.getPlaceObject().getOid();
        PlaceConfig config = CrowdServer.plreg.locprov.moveTo(_body, ploid);

        // check to see if they need a newer version of the scene data
        SceneService.SceneMoveListener listener = (SceneService.SceneMoveListener)_listener;
        SceneModel model = scmgr.getScene().getSceneModel();
        if (_version != model.version) {
            SceneUpdate[] updates = null;
            if (_version < model.version) {
                updates = scmgr.getUpdates(_version);
            }
            if (updates != null) {
                listener.moveSucceededWithUpdates(ploid, config, updates);
            } else {
                listener.moveSucceededWithScene(ploid, config, model);
            }
        } else {
            listener.moveSucceeded(ploid, config);
        }
    }

    protected int _version;
}
