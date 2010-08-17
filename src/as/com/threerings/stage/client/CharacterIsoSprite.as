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

import com.threerings.cast.CharacterSprite;
import com.threerings.util.DirectionCodes;
import com.threerings.media.Tickable;
import com.threerings.media.util.Path;
import com.threerings.media.util.Pathable;
import com.threerings.miso.client.PriorityIsoDisplayObject;
import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;
import com.threerings.stage.data.StageLocation;

import as3isolib.display.IsoSprite;
import as3isolib.display.scene.IsoScene;
import as3isolib.graphics.SolidColorFill;

public class CharacterIsoSprite extends IsoSprite
    implements Pathable, PriorityIsoDisplayObject
{
    public function CharacterIsoSprite (bodyOid :int, metrics :MisoSceneMetrics,
        charSprite :CharacterSprite)
    {
        _metrics = metrics;
        _bodyOid = bodyOid;
        setSize(1, 1, 2);

        usePreciseValues = true;

        // We wrap the sprite so we can shift it where we need it.  Components like to line up
        //  to the middle of a tile and as3isolib likes to line up to the back of a tile.
        var wrapper :Sprite = new Sprite();
        wrapper.y = _metrics.tilehhei;
        wrapper.addChild(charSprite);

        _character = charSprite;

        sprites = [wrapper];

        autoUpdate = true;
    }

    public function tick (tickStamp :int) :void
    {
        if (_path != null) {
            if (_pathStamp == 0) {
                _pathStamp = tickStamp
                _path.init(this, _pathStamp);
            }
            if (_path != null) {
                _path.tick(this, tickStamp);
            }
        }
        _character.tick(tickStamp);
    }

    public function isMoving () :Boolean
    {
        return _path != null;
    }

    public function getBodyOid () :int
    {
        return _bodyOid;
    }

    public function placeAtLoc (loc :StageLocation) :void
    {
        setLocation(MisoUtil.fullToTile(loc.x), MisoUtil.fullToTile(loc.y));
        setOrientation(loc.orient);
    }

    public function getX () :Number
    {
        return x;
    }

    public function getY () :Number
    {
        return y;
    }

    public function setLocation (x :Number, y :Number) :void
    {
        moveTo(x, y, 0);
    }

    public function setOrientation (orient :int) :void
    {
        _character.setOrientation(toIsoOrient(orient));
    }

    public function toIsoOrient (orient :int) :int
    {
        if (orient == DirectionCodes.NONE) {
            return orient;
        } else {
            return (orient + 2) % 8;
        }
    }

    public function fromIsoOrient (orient :int) :int
    {
        if (orient == DirectionCodes.NONE) {
            return orient;
        } else {
            return (orient + 6) % 8;
        }
    }

    public function getOrientation () :int
    {
        return fromIsoOrient(_character.getOrientation());
    }

    public function pathBeginning () :void
    {
        _character.pathBeginning();
    }

    public function pathCompleted (timestamp :int) :void
    {
        _character.pathCompleted(timestamp);
        var opath :Path = _path;
        _path = null;

        for each (var listener :Function in _pathCompleteListeners) {
            listener(this, opath, timestamp);
        }
    }

    public function addPathCompleteListener (listener :Function) :void
    {
        _pathCompleteListeners.push(listener);
    }

    public function move (path :Path) :void
    {
        // if there's a previous path, let it know that it's going away
        cancelMove();

        // save off this path
        _path = path;

        // we'll initialize it on our next tick thanks to a zero path stamp
        _pathStamp = 0;
    }

    public function cancelMove () :void
    {
        if (_path != null) {
            var oldpath :Path = _path;
            _path = null;
            oldpath.wasRemoved(this);
        }
    }

    public function getPriority () :int
    {
        return 0;
    }

    public function hitTest (stageX :int, stageY :int) :Boolean
    {
        return _character == null ? false : _character.hitTest(stageX, stageY);
    }

    protected var _bodyOid :int;

    protected var _metrics :MisoSceneMetrics;

    protected var _character :CharacterSprite;

    /** Path we're currently following, if any. */
    protected var _path :Path;

    /** The time we started moving on our path. */
    protected var _pathStamp :int;

    /** Anyone who cares when our path finishes. */
    protected var _pathCompleteListeners :Array = [];
}
}