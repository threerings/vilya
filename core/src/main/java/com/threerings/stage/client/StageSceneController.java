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

package com.threerings.stage.client;

import java.awt.Point;

import com.samskivert.util.Tuple;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.client.SpotSceneController;
import com.threerings.whirled.spot.data.Cluster;

import com.threerings.stage.data.StageLocation;
import com.threerings.stage.util.StageContext;

import static com.threerings.stage.Log.log;

/**
 * Extends the {@link SpotSceneController} with functionality specific to
 * displaying Stage scenes.
 */
public class StageSceneController extends SpotSceneController
{
    /**
     * Called when the user clicks on a location within the scene.
     */
    public void handleLocationClicked (Object source, StageLocation loc)
    {
        log.warning("handleLocationClicked(" + source + ", " + loc + ")");
    }

    /**
     * Handles a cluster clicked event.
     *
     * @param tuple the cluster that was clicked and the screen coords of the click.
     */
    public void handleClusterClicked (Object source, Tuple<Cluster, Point> tuple)
    {
        log.warning("handleClusterClicked(" + source + ", " + tuple + ")");
    }

    @Override
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new StageScenePanel((StageContext)ctx, this);
    }

    @Override
    protected void sceneUpdated (SceneUpdate update)
    {
        super.sceneUpdated(update);

        // let the scene panel know to rethink everything
        ((StageScenePanel)_view).sceneUpdated(update);
    }
}
