package com.threerings.ezgame {

import flash.errors.IllegalOperationError;

/**
 * Abstract base class. Do not instantiate.
 */
public class SubControl extends BaseControl
{
    public function SubControl (ctrl :EZGameControl)
    {
        super();
        if (ctrl == null || Object(this).constructor == SubControl) {
            throw new IllegalOperationError("Abstract");
        }

        _ctrl = ctrl;
    }

    protected var _ctrl :EZGameControl;
}
}
