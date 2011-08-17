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

import com.threerings.util.Log;
import com.threerings.util.Map;
import com.threerings.util.Maps;

import com.threerings.media.image.ClassRecord;
import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ColorRecord;
import com.threerings.media.image.Colorization;
import com.threerings.media.tile.Colorizer;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.stage.data.StageScene;

/**
 * Handles colorization of object tiles in a scene.
 */
public class SceneColorizer implements Colorizer
{
    private var log :Log = Log.getLog(SceneColorizer);

    /**
     * Creates a scene colorizer for the supplied scene.
     */
    public function SceneColorizer (cpos :ColorPository, scene :StageScene)
    {
        _cpos = cpos;
        _scene = scene;

        // enumerate the color ids for all possible colorization classes
        for each (var colClass :ClassRecord in _cpos.getClasses()) {
            _cids.put(colClass.name, _cpos.getColorIds(colClass.name));
        }
    }

    /**
     * Set an auxiliary colorizer that overrides our colorizations.
     */
    public function setAuxiliary (aux :Colorizer) :void
    {
        _aux = aux;
    }

    /**
     * Obtains a colorizer for the supplied scene object.
     */
    public function getColorizer (oinfo :ObjectInfo) :Colorizer
    {
        // if the object has no custom colorizations, return the default
        // colorizer
        if (oinfo.zations == 0) {
            return this;
        }

        // otherwise create a custom colorizer that returns this object's
        // custom colorization assignments
        return new BaseColorizer(oinfo, this, _cpos);
    }

    // documentation inherited from interface Colorizer
    public function getColorization (index :int, zation :String) :Colorization
    {
        // This method is called when an object in the scene has no colorization
        // of its own defined for a particular color class.
        if (_aux != null) {
            var c :Colorization = _aux.getColorization(index, zation);
            if (c != null) {
                return c;
            }
        }

        return _cpos.getColorizationByNameAndId(zation, getColorId(zation));
    }

    /**
     * Get the colorId to use for the specified colorization.
     */
    public function getColorId (zation :String) :int
    {
        // 1. We see if the scene contains a default color we should use.
        var rec :ClassRecord = _cpos.getClassRecordByName(zation);
        var colorId :int = _scene.getDefaultColor(rec.classId);
        if (colorId == -1) {
            // 2. If the scene does not contain a color, see if a default
            // is defined for that color class.
            var def :ColorRecord = rec.getDefault();
            if (def != null) {
                return def.colorId;
            }

            // 3. If there are no defaults whatsoever, just hash on the zoneId.
            var cids :Array= _cids.get(zation);
            if (cids == null) {
                log.warning("Zoiks, have no colorizations for '" + zation + "'.");
                return -1;
            } else {
                colorId = cids[_scene.getZoneId() % cids.length];
            }
        }
        return colorId;
    }

    /** An auxiliary colorizer which may temporarily return
     * non-standard colorizations. */
    protected var _aux :Colorizer;

    /** The entity from which we obtain colorization info. */
    protected var _cpos :ColorPository;

    /** The scene for which we're providing zations. */
    protected var _scene :StageScene;

    /** Contains our colorization class information. */
    protected var _cids :Map = Maps.newMapOf(String);
}
}

import com.threerings.media.image.ColorPository;
import com.threerings.media.image.Colorization;
import com.threerings.media.tile.Colorizer;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.stage.client.SceneColorizer;

class BaseColorizer implements Colorizer {
    public function BaseColorizer (oInfo :ObjectInfo, defColorizer :SceneColorizer,
        cpos :ColorPository)
    {
        _oInfo = oInfo;
        _defColorizer = defColorizer;
        _cpos = cpos;
    }

    public function getColorization (index :int, zation :String) :Colorization {
        var colorId :int = 0;
        switch (index) {
        case 0: colorId = _oInfo.getPrimaryZation(); break;
        case 1: colorId = _oInfo.getSecondaryZation(); break;
        case 2: colorId = _oInfo.getTertiaryZation(); break;
        case 3: colorId = _oInfo.getQuaternaryZation(); break;
        }

        if (colorId == 0) {
            return _defColorizer.getColorization(index, zation);
        } else {
            return _cpos.getColorizationByNameAndId(zation, colorId);
        }
    }

    protected var _oInfo :ObjectInfo;
    protected var _defColorizer :SceneColorizer;
    protected var _cpos :ColorPository;
}