//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2011 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.stage.client {

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.stage.data.StageLocation;
import com.threerings.stage.util.StageContext;
import com.threerings.util.Log;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.client.SpotSceneController;

public class StageSceneController extends SpotSceneController
{
    public var log :Log = Log.getLog(StageSceneController);

    /**
     * Called when the user clicks on a location within the scene.
     */
    public function handleLocationClicked (source :Object, loc :StageLocation) :void
    {
        log.warning("handleLocationClicked(" + source + ", " + loc + ")");
    }

    /**
     * Handles a cluster clicked event.
     *
     * @param tuple the cluster that was clicked and the screen coords of the click.
     */
    public function handleClusterClicked (source :Object, clusterClick :ClusterClickedInfo) :void
    {
        log.warning("handleClusterClicked(" + source + ", " + clusterClick + ")");
    }

    override protected function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        return new StageScenePanel(StageContext(ctx), this, new MisoSceneMetrics());
    }

    override protected function sceneUpdated (update :SceneUpdate) :void
    {
        super.sceneUpdated(update);

        // let the scene panel know to rethink everything
        (StageScenePanel(_view)).sceneUpdated(update);
    }
}
}
