package com.threerings.stage.util {

import com.threerings.media.image.ColorPository;
import com.threerings.miso.util.MisoContext;

/**
 * A context that provides for the myriad requirements of the Stage system.
 * This is currently just a stub.
 */
public interface StageContext extends MisoContext
{
    /**
     * Returns a reference to the colorization repository.
     */
    function getColorPository () :ColorPository;
}
}
