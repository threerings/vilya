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

package com.threerings.whirled.zone.server;

import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.server.LocationManager;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.server.AbstractSceneMoveHandler;
import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.server.SceneRegistry;
import com.threerings.whirled.zone.client.ZoneService;
import com.threerings.whirled.zone.data.ZoneCodes;
import com.threerings.whirled.zone.data.ZoneSummary;
import com.threerings.whirled.zone.data.ZonedBodyObject;

import static com.threerings.whirled.zone.Log.log;

/**
 * Handles transitioning between zones.
 */
public class ZoneMoveHandler extends AbstractSceneMoveHandler
    implements ZoneManager.ResolutionListener
{
    public ZoneMoveHandler (LocationManager locmgr, ZoneManager zmgr, SceneRegistry screg,
                            BodyObject body, int sceneId, int sceneVer,
                            ZoneService.ZoneMoveListener listener)
    {
        super(locmgr, body, listener);
        _zmgr = zmgr;
        _screg = screg;
        _sceneId = sceneId;
        _version = sceneVer;
    }

    // from interface ZoneManager.ResolutionListener
    public void zoneWasResolved (ZoneSummary summary)
    {
        // give the zone manager a chance to veto the request
        String errmsg = _zmgr.ratifyBodyEntry(_body, summary.zoneId);
        if (errmsg != null) {
            _listener.requestFailed(errmsg);
            return;
        }
        _summary = summary;

        // now resolve the target scene
        resolveScene();
    }

    // from interface ZoneManager.ResolutionListener
    public void zoneFailedToResolve (int zoneId, Exception reason)
    {
        log.warning("Unable to resolve zone [zoneId=" + zoneId + ", reason=" + reason + "].");
        _listener.requestFailed(ZoneCodes.NO_SUCH_ZONE);
    }

    @Override
    protected void effectSceneMove (SceneManager scmgr)
        throws InvocationException
    {
        // move to the place object associated with this scene
        int ploid = scmgr.getPlaceObject().getOid();
        PlaceConfig config = _locman.moveTo(_body, ploid);

        // now that we've moved, we can update the user object with the new zone id
        ((ZonedBodyObject)_body).setZoneId(_summary.zoneId);

        // check to see if they need a newer version of the scene data
        ZoneService.ZoneMoveListener listener = (ZoneService.ZoneMoveListener)_listener;
        SceneModel model = scmgr.getScene().getSceneModel();
        if (_version != model.version) {
            SceneUpdate[] updates = null;
            if (_version < model.version) {
                updates = scmgr.getUpdates(_version);
            }
            if (updates != null) {
                listener.moveSucceededWithUpdates(ploid, config, _summary, updates);
            } else {
                listener.moveSucceededWithScene(ploid, config, _summary, model);
            }
        } else {
            listener.moveSucceeded(ploid, config, _summary);
        }

        // let the zone manager know that someone just came on in
        _zmgr.bodyDidEnterZone(_body, _summary.zoneId);
    }

    /**
     * Begins the process of resolving the target scene.
     */
    protected void resolveScene ()
    {
        _screg.resolveScene(_sceneId, this);
    }

    protected ZoneManager _zmgr;
    protected SceneRegistry _screg;
    protected int _sceneId, _version;
    protected ZoneSummary _summary;
}
