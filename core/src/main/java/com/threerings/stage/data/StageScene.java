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

import java.util.ArrayList;
import java.util.Iterator;

import com.google.common.collect.Lists;

import com.threerings.crowd.data.PlaceConfig;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.whirled.data.SceneImpl;
import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SpotScene;
import com.threerings.whirled.spot.data.SpotSceneImpl;
import com.threerings.whirled.spot.data.SpotSceneModel;

import static com.threerings.stage.Log.log;

/**
 * The implementation of the Stage scene interface.
 */
public class StageScene extends SceneImpl
    implements SpotScene, Cloneable
{
    /**
     * Creates an instance that will obtain data from the supplied scene
     * model and place config.
     */
    public StageScene (StageSceneModel model, PlaceConfig config)
    {
        super(model, config);
        _model = model;
        _sdelegate = new SpotSceneImpl(SpotSceneModel.getSceneModel(_model));
        readInterestingObjects();
    }

    /**
     * Returns the scene type (e.g. "world", "port", "bank", etc.).
     */
    public String getType ()
    {
        return _model.type;
    }

    /**
     * Returns the zone id to which this scene belongs.
     */
    public int getZoneId ()
    {
        return _model.zoneId;
    }

    /**
     * Sets the type of this scene.
     */
    public void setType (String type)
    {
        _model.type = type;
    }

    /**
     * Get the default color id to use for the specified colorization class,
     * or -1 if no default is set.
     */
    public int getDefaultColor (int classId)
    {
        return _model.getDefaultColor(classId);
    }

    /**
     * Set the default color to use for the specified colorization class id.
     * Setting the colorId to -1 disables the default.
     */
    public void setDefaultColor (int classId, int colorId)
    {
        _model.setDefaultColor(classId, colorId);
    }

    /**
     * Iterates over all of the interesting objects in this scene.
     */
    public Iterator<ObjectInfo> enumerateObjects ()
    {
        return _objects.iterator();
    }

    /**
     * Adds a new object to this scene.
     */
    public void addObject (ObjectInfo info)
    {
        _objects.add(info);

        // add it to the underlying scene model
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(_model);
        if (mmodel != null) {
            if (!mmodel.addObject(info)) {
                log.warning("Scene model rejected object add " +
                            "[scene=" + this + ", object=" + info + "].");
            }
        }
    }

    /**
     * Removes an object from this scene.
     */
    public boolean removeObject (ObjectInfo info)
    {
        boolean removed = _objects.remove(info);

        // remove it from the underlying scene model
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(_model);
        if (mmodel != null) {
            removed = mmodel.removeObject(info) || removed;
        }

        return removed;
    }

    @Override
    public void updateReceived (SceneUpdate update)
    {
        super.updateReceived(update);

        // update our spot scene delegate
        _sdelegate.updateReceived();

        // re-read our interesting objects
        readInterestingObjects();
    }

    @Override
    public StageScene clone ()
        throws CloneNotSupportedException
    {
        // create a new scene with a clone of our model
        return new StageScene((StageSceneModel)_model.clone(), _config);
    }

    // documentation inherited from interface
    public Portal getPortal (int portalId)
    {
        return _sdelegate.getPortal(portalId);
    }

    // documentation inherited from interface
    public int getPortalCount ()
    {
        return _sdelegate.getPortalCount();
    }

    // documentation inherited from interface
    public Iterator<Portal> getPortals ()
    {
        return _sdelegate.getPortals();
    }

    // documentation inherited from interface
    public short getNextPortalId ()
    {
        return _sdelegate.getNextPortalId();
    }

    // documentation inherited from interface
    public Portal getDefaultEntrance ()
    {
        return _sdelegate.getDefaultEntrance();
    }

    // documentation inherited from interface
    public void addPortal (Portal portal)
    {
        _sdelegate.addPortal(portal);
    }

    // documentation inherited from interface
    public void removePortal (Portal portal)
    {
        _sdelegate.removePortal(portal);
    }

    // documentation inherited from interface
    public void setDefaultEntrance (Portal portal)
    {
        _sdelegate.setDefaultEntrance(portal);
    }

    protected void readInterestingObjects ()
    {
        _objects.clear();
        StageMisoSceneModel mmodel = StageMisoSceneModel.getSceneModel(_model);
        if (mmodel != null) {
            mmodel.getInterestingObjects(_objects);
        }
    }

    /** A reference to our scene model. */
    protected StageSceneModel _model;

    /** Our spot scene delegate. */
    protected SpotSceneImpl _sdelegate;

    /** A list of all interesting scene objects. */
    protected ArrayList<ObjectInfo> _objects = Lists.newArrayList();
}
