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

package com.threerings.stage.data {

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.util.Iterator;
import com.threerings.whirled.data.SceneImpl;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotScene;
import com.threerings.whirled.spot.data.SpotSceneImpl;
import com.threerings.whirled.spot.data.SpotSceneModel;

public class StageScene extends SceneImpl
    implements SpotScene
{
    public function StageScene (model :StageSceneModel, config :PlaceConfig)
    {
        super(model, config);
        _ssModel = model;
        _sdelegate = new SpotSceneImpl(SpotSceneModel.getSceneModel(_model));
    }

    /**
     * Get the default color id to use for the specified colorization class,
     * or -1 if no default is set.
     */
    public function getDefaultColor (classId :int) :int
    {
        return _ssModel.getDefaultColor(classId);
    }

    /**
     * Set the default color to use for the specified colorization class id.
     * Setting the colorId to -1 disables the default.
     */
    public function setDefaultColor (classId :int, colorId :int) :void
    {
        _ssModel.setDefaultColor(classId, colorId);
    }

    public function getZoneId () :int
    {
        return _ssModel.zoneId;
    }

    // documentation inherited from interface
    public function getPortal (portalId :int) :Portal
    {
        return _sdelegate.getPortal(portalId);
    }

    // documentation inherited from interface
    public function getPortalCount () :int
    {
        return _sdelegate.getPortalCount();
    }

    // documentation inherited from interface
    public function getPortals () :Iterator
    {
        return _sdelegate.getPortals();
    }

    // documentation inherited from interface
    public function getNextPortalId () :int
    {
        return _sdelegate.getNextPortalId();
    }

    // documentation inherited from interface
    public function getDefaultEntrance () :Portal
    {
        return _sdelegate.getDefaultEntrance();
    }

    // documentation inherited from interface
    public function addPortal (portal :Portal) :void
    {
        _sdelegate.addPortal(portal);
    }

    // documentation inherited from interface
    public function removePortal (portal :Portal) :void
    {
        _sdelegate.removePortal(portal);
    }

    // documentation inherited from interface
    public function setDefaultEntrance (portal :Portal) :void
    {
        _sdelegate.setDefaultEntrance(portal);
    }

    protected var _ssModel :StageSceneModel;

    /** Our spot scene delegate. */
    protected var _sdelegate :SpotSceneImpl;
}
}