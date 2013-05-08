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

package com.threerings.whirled.util;

import java.util.List;

import com.google.common.collect.Lists;

import com.threerings.whirled.data.SceneUpdate;

import static com.threerings.whirled.Log.log;

/**
 * A list specialized for storing {@link SceneUpdate} objects.
 */
public class UpdateList
{
    /**
     * Adds an update to this list. The update must follow appropriately the chain of updates
     * established by the updates already in the list (meaning it must operate on one version
     * higher than the most recent update already in the list).
     */
    public void addUpdate (SceneUpdate update)
    {
        // if this is our first update, great, we let it in with no questions asked
        if (_updates.isEmpty()) {
            _updates.add(update);
            return;
        }

        // otherwise make sure this update conforms to our update sequence
        SceneUpdate last = _updates.get(_updates.size()-1);
        int expVersion = last.getSceneVersion() + last.getVersionIncrement();
        int gotVersion = update.getSceneVersion();
        if (gotVersion > expVersion) {
            log.warning("Update continuity broken, flushing list [got=" + update +
                        ", expect=" + expVersion + ", ucount=" + _updates.size() + "].");
            _updates.clear(); // flush out our old updates, fall through and add this one

        } else if (gotVersion < expVersion) {
            // we somehow got an update that's older than updates we already have?
            String errmsg = "Invalid update version [want=" + expVersion + ", got=" + update + "]";
            throw new IllegalArgumentException(errmsg);
        }

        _updates.add(update);
    }

    /**
     * Returns all of the updates that should be applied to a scene with the specified version to
     * bring it up to date. <code>null</code> is returned if the scene's version is older than the
     * oldest update in our list, in which case it cannot be brought up to date by applying updates
     * from this list.
     */
    public SceneUpdate[] getUpdates (int fromVersion)
    {
        // If we don't have updates, or our updates start from someplace later than our fromVersion,
        // we can't give updates from our known ones.
        if (_updates.size() == 0 || _updates.get(0).getSceneVersion() > fromVersion) {
            return null;
        }

        List<SceneUpdate> updates = Lists.newArrayList();
        for (SceneUpdate update : _updates) {
            if (update.getSceneVersion() >= fromVersion) {
                updates.add(update);
            }
        }
        return (updates.size() == 0) ? null : updates.toArray(new SceneUpdate[updates.size()]);
    }

    /**
     * Returns true if the supplied actual scene version is in accordance with the updates
     * contained in this list.
     */
    public boolean validate (int sceneVersion)
    {
        if (_updates.size() == 0) {
            return true;
        }
        SceneUpdate last = _updates.get(_updates.size()-1);
        return sceneVersion == (last.getSceneVersion() + last.getVersionIncrement());
    }

    protected List<SceneUpdate> _updates = Lists.newArrayList();
}
