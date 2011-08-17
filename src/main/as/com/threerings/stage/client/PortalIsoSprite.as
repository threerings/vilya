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

import flash.display.Bitmap;
import flash.display.DisplayObject;
import flash.geom.Point;

import as3isolib.display.IsoSprite;

import com.threerings.miso.client.PriorityIsoDisplayObject;

import com.threerings.whirled.spot.data.Portal;

public class PortalIsoSprite extends IsoSprite
    implements PriorityIsoDisplayObject
{
    public function PortalIsoSprite (img :DisplayObject, x :int, y :int, portal :Portal)
    {
        sprites = [img];
        setSize(1, 1, 1);
        moveTo(x, y, 0);
        setRaised(false);
        _portal = portal;
    }

    public function getPriority () :int
    {
        return 0;
    }

    public function getPortal () :Portal
    {
        return _portal;
    }

    public function hitTest (stageX :int, stageY :int) :Boolean
    {
        if (sprites == null || sprites.length == 0) {
            return false;
        }

        if (sprites[0] is Bitmap) {
            if (!sprites[0].hitTestPoint(stageX, stageY, true)) {
                // Doesn't even hit the bounds...
                return false;
            }
            // Check the actual pixels...
            var pt :Point = sprites[0].globalToLocal(new Point(stageX, stageY));
            return Bitmap(sprites[0]).bitmapData.hitTest(new Point(0, 0), 0, pt);
        } else {
            return sprites[0].hitTestPoint(stageX, stageY, true);
        }
    }

    public function setRaised (raised :Boolean) :void
    {
        sprites[0].alpha = (raised ? 1.0 : 0.5);
    }

    protected var _portal :Portal;
}
}