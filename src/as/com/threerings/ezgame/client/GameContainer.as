package com.threerings.ezgame.client {

import flash.display.InteractiveObject;
import flash.display.Loader;

import mx.managers.IFocusManagerComponent;

import com.threerings.util.MediaContainer;

public class GameContainer extends MediaContainer
    implements IFocusManagerComponent
{
    public function GameContainer (url :String)
    {
        super(url);
    }

    override public function setFocus () :void
    {
        super.setFocus();
        if (systemManager.stage.focus == this) {
            // TODO
            //systemManager.stage.focus = InteractiveObject(Loader(_media).content);
            systemManager.stage.focus = Loader(_media);
        }
    }
}
}
