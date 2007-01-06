package com.threerings.ezgame.client {

import flash.display.DisplayObject;

import flash.geom.Rectangle;

import mx.core.mx_internal;
import mx.core.IFlexDisplayObject;
import mx.core.IInvalidating;

import mx.managers.IFocusManagerComponent;

import mx.skins.ProgrammaticSkin;

import com.threerings.util.MediaContainer;

// TODO: there are focus issues in here that need dealing with.
//
// 1) The focus rectangle is drawn at the wrong size in scrolling games
// 2) We can get focus without having the pink focus rectangle...
// 3) When the mouse leaves the flash player and returns, this
//    damn thing doesn't seem to grip onto the focus.
// 
public class GameContainer extends MediaContainer
    implements IFocusManagerComponent
{
    public function GameContainer (url :String)
    {
        super(url);

        tabEnabled = true; // turned off by Container
//        focusRect = true; // we need the focus rect
    }

    override protected function adjustFocusRect (obj :DisplayObject = null) :void
    {
        super.adjustFocusRect(obj);

        // TODO: this is probably all wrong
        var focusObj :IFlexDisplayObject =
            IFlexDisplayObject(mx_internal::getFocusObject());
        if (focusObj) {
            var r :Rectangle = transform.pixelBounds;
            focusObj.setActualSize(r.width - 2, r.height - 2);
            focusObj.move(0, 0);

            if (focusObj is IInvalidating) {
                IInvalidating(focusObj).validateNow();

            } else if (focusObj is ProgrammaticSkin) {
                ProgrammaticSkin(focusObj).validateNow();
            }
        }
    }
}
}
