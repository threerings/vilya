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

import flash.display.DisplayObject;
import flash.display.Sprite;

import com.threerings.miso.client.Tickable;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;
import com.threerings.stage.data.StageLocation;

import as3isolib.display.IsoSprite;
import as3isolib.graphics.SolidColorFill;

public class CharacterIsoSprite extends IsoSprite
{
    public function CharacterIsoSprite (bodyOid :int, metrics :MisoSceneMetrics,
        disp :DisplayObject)
    {
        _metrics = metrics;
        _bodyOid = bodyOid;
        setSize(1, 1, 2);

        // We wrap the sprite so we can shift it where we need it.  Components like to line up
        //  to the middle of a tile and as3isolib likes to line up to the back of a tile.
        var wrapper :Sprite = new Sprite();
        wrapper.y = _metrics.tilehhei;
        wrapper.addChild(disp);

        _character = disp;

        sprites = [wrapper];
    }

    public function tick (tickStamp :int) :void
    {
        if (_character is Tickable) {
            Tickable(_character).tick(tickStamp);
        }
    }

    public function isMoving () :Boolean
    {
        return false; //TODO Path
    }

    public function getBodyOid () :int
    {
        return _bodyOid;
    }

    public function placeAtLoc (loc :StageLocation) :void
    {
        moveTo(MisoUtil.fullToTile(loc.x), MisoUtil.fullToTile(loc.y), 0);
        // TODO Components - handle orientation
        render();
    }

    public function move (path :Object /*TODO Path */) :void
    {
        // TODO Path
    }

    public function cancelMove () :void
    {
        // TODO Path
    }

    protected var _bodyOid :int;

    protected var _metrics :MisoSceneMetrics;

    protected var _character :DisplayObject;
}
}