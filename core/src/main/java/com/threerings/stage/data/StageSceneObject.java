//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.stage.data;

import javax.annotation.Generated;
import com.threerings.whirled.spot.data.SpotSceneObject;

/**
 * Extends the basic {@link SpotSceneObject} with data and services
 * specific to isometric stage scenes.
 */
public class StageSceneObject extends SpotSceneObject
{
    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>stageSceneService</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String STAGE_SCENE_SERVICE = "stageSceneService";

    /** The field name of the <code>lightLevel</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String LIGHT_LEVEL = "lightLevel";

    /** The field name of the <code>lightShade</code> field. */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public static final String LIGHT_SHADE = "lightShade";
    // AUTO-GENERATED: FIELDS END

    /** Provides stage scene services. */
    public StageSceneMarshaller stageSceneService;

    /** The light level in this scene. 0f being fully on, 1f fully shaded. */
    public float lightLevel = 0f;

    /** The color of the light. */
    public int lightShade = 0xFFFFFF;

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>stageSceneService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setStageSceneService (StageSceneMarshaller value)
    {
        StageSceneMarshaller ovalue = this.stageSceneService;
        requestAttributeChange(
            STAGE_SCENE_SERVICE, value, ovalue);
        this.stageSceneService = value;
    }

    /**
     * Requests that the <code>lightLevel</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setLightLevel (float value)
    {
        float ovalue = this.lightLevel;
        requestAttributeChange(
            LIGHT_LEVEL, Float.valueOf(value), Float.valueOf(ovalue));
        this.lightLevel = value;
    }

    /**
     * Requests that the <code>lightShade</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    @Generated(value={"com.threerings.presents.tools.GenDObjectTask"})
    public void setLightShade (int value)
    {
        int ovalue = this.lightShade;
        requestAttributeChange(
            LIGHT_SHADE, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.lightShade = value;
    }
    // AUTO-GENERATED: METHODS END
}
