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

import com.threerings.miso.client.MisoScenePanel;
import com.threerings.miso.util.MisoContext;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;
import com.threerings.miso.client.SceneBlock;
import com.threerings.miso.data.MisoSceneModel;
import com.threerings.util.Iterator;
import com.threerings.util.MathUtil;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotScene;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.stage.data.StageLocation;

import as3isolib.display.IsoSprite;
import as3isolib.display.IsoView;
import as3isolib.display.scene.IsoScene;

public class StageSceneBlock extends SceneBlock
{
    public function StageSceneBlock (key :int, objScene :IsoScene, portalScene :IsoScene,
        isoView :IsoView, metrics :MisoSceneMetrics, spotScene :SpotScene)
    {
        super(key, objScene, isoView, metrics);
        _spotScene = spotScene;
        _portalScene = portalScene;
    }

    override public function resolve (ctx :MisoContext, model :MisoSceneModel,
        panel :MisoScenePanel, completeCallback :Function) :void
    {
        _tryingPortals = true;
        super.resolve(ctx, model, panel, completeCallback);

        var bx :int = getBlockX(_key);
        var by :int = getBlockY(_key);
        var iter :Iterator = _spotScene.getPortals();
        while (iter.hasNext()) {
            var portal :Portal = Portal(iter.next());
            var x :int = MisoUtil.fullToTile(StageLocation(portal.loc).x);
            var y :int = MisoUtil.fullToTile(StageLocation(portal.loc).y);
            if (x < bx || x >= bx + BLOCK_SIZE || y < by || y >= by + BLOCK_SIZE) {
                continue;
            }

            var fineX :int = MisoUtil.fullToFine(StageLocation(portal.loc).x);
            var fineY :int = MisoUtil.fullToFine(StageLocation(portal.loc).y);

            // Grab the portal image, and center it.
            var img :DisplayObject =
                StageScenePanel(panel).getPortalImage(StageLocation(portal.loc).orient);
            img.x = -img.width/2 + int(_metrics.finehwid * (fineX - fineY));
            img.y = -img.height/2 + int(_metrics.finehhei * (fineX + fineY));
            var portalSprite :PortalIsoSprite = new PortalIsoSprite(img, x, y, portal);

            if (_portSprites == null) {
                _portSprites = [];
            }
            _portSprites.push(portalSprite);
        }

        _tryingPortals = false;
        maybeLoaded();
    }

    override public function render () :void
    {
        super.render();
        for each (var sprite :IsoSprite in _portSprites) {
            _portalScene.addChild(sprite);
        }
    }

    override public function release () :void
    {
        super.release();
        for each (var sprite :IsoSprite in _portSprites) {
            _portalScene.removeChild(sprite);
        }
    }

    override protected function maybeLoaded () :void
    {
        // If we're still working on getting our portals setup, skip it...
        if (!_tryingPortals) {
            super.maybeLoaded();
        }
    }

    protected var _spotScene :SpotScene;

    protected var _tryingPortals :Boolean;

    protected var _portalScene :IsoScene;

    protected var _portSprites :Array;
}
}