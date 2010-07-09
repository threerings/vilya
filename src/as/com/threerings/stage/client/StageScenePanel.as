// $Id: StageScenePanel.as 887 2010-01-05 22:12:02Z dhoover $
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

package com.threerings.stage.client {

import com.threerings.util.Controller;
import com.threerings.util.Log;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.media.tile.Colorizer;
import com.threerings.miso.client.MisoScenePanel;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.util.StageContext;

/**
 * Eventually responsible for rendering a stage scene - but for now it's a stub.
 */
public class StageScenePanel extends MisoScenePanel
{
    private var log :Log = Log.getLog(StageScenePanel);

    public function StageScenePanel (ctx :StageContext, ctrl :Controller, metrics :MisoSceneMetrics)
    {
        super(ctx, metrics);
        _sCtx = ctx;
    }

    public function setScene (scene :StageScene) :void
    {
        _scene = scene;
        if (_scene != null) {
            _rizer = new SceneColorizer(_sCtx.getColorPository(), _scene);
            recomputePortals();
            setSceneModel(StageMisoSceneModel.getSceneModel(scene.getSceneModel()));
        } else {
            log.warning("Zoiks! We can't display a null scene!");
            // TODO: display something to the user letting them know that
            // we're so hosed that we don't even know what time it is
        }
    }

    public function recomputePortals () :void
    {
        // TODO
    }

    public function sceneUpdated (update :SceneUpdate) :void
    {
        // TODO
    }

    override public function getColorizer (oinfo :ObjectInfo) :Colorizer
    {
        return _rizer.getColorizer(oinfo);
    }

    /** The scene we're presenting in our panel. */
    protected var _scene :StageScene;

    /** Used to recolor any tiles in our scene. */
    protected var _rizer :SceneColorizer;

    /** Our appropriately-cast stage client context. */
    protected var _sCtx :StageContext;
}
}
