//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.ezgame.client {

import flash.display.DisplayObject;

import flash.geom.Rectangle;

import flash.text.TextField;

import mx.core.mx_internal;
import mx.core.IFlexDisplayObject;
import mx.core.IInvalidating;

import mx.containers.VBox;

import com.threerings.flash.MediaContainer;

// TODO: there are focus issues in here that need dealing with.
//
// 1) The focus rectangle is drawn at the wrong size in scrolling games
// 2) We can get focus without having the pink focus rectangle...
// 3) When the mouse leaves the flash player and returns, this
//    damn thing doesn't seem to grip onto the focus.
// 
public class GameContainer extends VBox
{
    public function GameContainer (url :String)
    {
        rawChildren.addChild(_keyGrabber = new TextField());
        _keyGrabber.selectable = false;
        rawChildren.addChild(_game = new MediaContainer(url));

        tabEnabled = true; // turned off by Container
//        focusRect = true; // we need the focus rect
    }

    public function getMediaContainer () :MediaContainer
    {
        return _game;
    }

    override public function setActualSize (w :Number, h :Number) :void
    {
        super.setActualSize(w, h);
        _keyGrabber.width = w;
        _keyGrabber.height = h;
    }

//    override public function setFocus () :void
//    {
//        if (stage) {
//            try {
//                stage.focus = this;
//            } catch (e :Error) {
//                trace("Apparently, this might happen: " + e)
//            }
//        }
//    }

//    override protected function adjustFocusRect (obj :DisplayObject = null) :void
//    {
//        super.adjustFocusRect(obj);
//
//        // TODO: this is probably all wrong
//        var focusObj :IFlexDisplayObject =
//            IFlexDisplayObject(mx_internal::getFocusObject());
//        if (focusObj) {
//            var r :Rectangle = transform.pixelBounds;
//            focusObj.setActualSize(r.width - 2, r.height - 2);
//            focusObj.move(0, 0);
//
//            if (focusObj is IInvalidating) {
//                IInvalidating(focusObj).validateNow();
//
//            } else if (focusObj is ProgrammaticSkin) {
//                ProgrammaticSkin(focusObj).validateNow();
//            }
//        }
//    }

    protected var _game :MediaContainer;

    protected var _keyGrabber :TextField;
}
}
