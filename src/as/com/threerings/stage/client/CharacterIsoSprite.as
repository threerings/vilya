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

import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;
import com.threerings.stage.data.StageLocation;

import as3isolib.display.primitive.IsoBox;

public class CharacterIsoSprite extends IsoBox
{
    public function CharacterIsoSprite (bodyOid :int, metrics :MisoSceneMetrics)
    {
        _metrics = metrics;
        _bodyOid = bodyOid;
        setSize(1, 1, 2);
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
}
}