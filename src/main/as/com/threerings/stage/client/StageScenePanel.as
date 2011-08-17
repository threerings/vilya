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

import flash.display.DisplayObject;

import as3isolib.display.IsoSprite;
import as3isolib.display.scene.IsoScene;

import com.threerings.util.Controller;
import com.threerings.util.Log;

import com.threerings.media.tile.Colorizer;

import com.threerings.miso.client.MisoScenePanel;
import com.threerings.miso.client.PriorityIsoDisplayObject;
import com.threerings.miso.client.SceneBlock;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoSceneMetrics;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.data.Portal;

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

    override protected function computeOverHover (mx :int, my :int) :Object
    {
        var hits :Array =
            _overPortalScene.displayListChildren.filter(
                function(val :Object, idx :int, arr :Array) :Boolean {
                    if (val is PriorityIsoDisplayObject) {
                        return PriorityIsoDisplayObject(val).hitTest(mx, my);
                    } else {
                        return false;
                    }
                });

        if (hits.length > 0) {
            return hits[0];
        } else {
            hits = _portalScene.displayListChildren.filter(
                function(val :Object, idx :int, arr :Array) :Boolean {
                    if (val is PriorityIsoDisplayObject) {
                        return PriorityIsoDisplayObject(val).hitTest(mx, my);
                    } else {
                        return false;
                    }
                });
            if (hits.length > 0) {
                return hits[0];
            } else {
                return null;
            }
        }
    }

    override protected function hoverObjectChanged (oldHover :Object, newHover :Object) :void
    {
        super.hoverObjectChanged(oldHover, newHover);

        if (oldHover is PortalIsoSprite) {
            var oldPortal :PortalIsoSprite = PortalIsoSprite(oldHover);
            oldPortal.setRaised(false);

            if (_overPortalScene.removeChild(oldPortal) != null) {
                _portalScene.addChild(oldPortal);
            }

            _portalScene.render();
            _overPortalScene.render();
        }

        if (newHover is PortalIsoSprite) {
            var newPortal :PortalIsoSprite = PortalIsoSprite(newHover);
            newPortal.setRaised(true);

            if (_portalScene.removeChild(newPortal) != null) {
                _overPortalScene.addChild(newPortal);
            }

            _portalScene.render();
            _overPortalScene.render();
        }
    }

    override protected function renderObjectScenes () :void
    {
        super.renderObjectScenes();
        _portalScene.render();
        _overPortalScene.render();
    }

    override protected function addObjectScenes () :void
    {
        _portalScene = new IsoScene();
        _isoView.addScene(_portalScene);
        super.addObjectScenes();
        _overPortalScene = new IsoScene();
        _isoView.addScene(_overPortalScene);
    }

    override protected function createSceneBlock (blockKey :int) :SceneBlock
    {
        return new StageSceneBlock(blockKey, _objScene, _portalScene, _isoView, _metrics,
            _scene);
    }

    public function sceneUpdated (update :SceneUpdate) :void
    {
        // TODO
    }

    public function getPortalImage (dir :int) :DisplayObject
    {
        throw new Error("abstract");
    }

    override public function getColorizer (oinfo :ObjectInfo) :Colorizer
    {
        return _rizer.getColorizer(oinfo);
    }


    protected var _portalScene :IsoScene;
    protected var _overPortalScene :IsoScene;

    /** The scene we're presenting in our panel. */
    protected var _scene :StageScene;

    /** Used to recolor any tiles in our scene. */
    protected var _rizer :SceneColorizer;

    /** Our appropriately-cast stage client context. */
    protected var _sCtx :StageContext;
}
}
