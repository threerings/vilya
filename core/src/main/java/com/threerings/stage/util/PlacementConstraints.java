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

package com.threerings.stage.util;

import java.util.HashMap;
import java.util.List;

import java.awt.Rectangle;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.samskivert.util.ListUtil;

import com.threerings.util.DirectionCodes;
import com.threerings.util.DirectionUtil;
import com.threerings.util.MessageBundle;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.ObjectTileSet;
import com.threerings.media.tile.TileManager;

import com.threerings.miso.data.ObjectInfo;

import com.threerings.stage.data.StageCodes;
import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;

import static com.threerings.stage.Log.log;

/**
 * Maintains extra information on objects in a scene and checks proposed
 * placement operations for constraint violations.  When the constraints
 * object is in use, all placement operations (object additions and removals)
 * must go through the constraints object so that the object's internal state
 * remains consistent.
 */
public class PlacementConstraints
    implements DirectionCodes, StageCodes
{
    /**
     * Default constructor.
     */
    public PlacementConstraints (TileManager tilemgr, StageScene scene)
    {
        _tilemgr = tilemgr;
        _scene = scene;
        _mmodel = StageMisoSceneModel.getSceneModel(scene.getSceneModel());

        // add all the objects in the scene
        StageMisoSceneModel.ObjectVisitor visitor =
            new StageMisoSceneModel.ObjectVisitor() {
            public void visit (ObjectInfo info) {
                ObjectData data = createObjectData(info);
                if (data != null) {
                    // clone the map key, as the visit method reuses a
                    // single ObjectInfo instance for uninteresting objects
                    // in a section
                    _objectData.put(info.clone(), data);
                }
            }
        };
        _mmodel.visitObjects(visitor);
    }

    /**
     * Determines whether the constraints allow the specified object to be
     * added to the scene.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a translatable string explaining why the object can't be
     * added
     */
    public String allowAddObject (ObjectInfo info)
    {
        return allowModifyObjects(new ObjectInfo[] { info },
            new ObjectInfo[0]);
    }

    /**
     * Adds the specified object through the constraints.
     */
    public void addObject (ObjectInfo info)
    {
        ObjectData data = createObjectData(info);
        if (data != null) {
            _scene.addObject(info);
            _objectData.put(info, data);
        }
    }

    /**
     * Determines whether the constraints allow the specified object to be
     * removed from the scene.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a translatable string explaining why the object can't be
     * removed
     */
    public String allowRemoveObject (ObjectInfo info)
    {
        return allowModifyObjects(new ObjectInfo[0],
            new ObjectInfo[] { info });
    }

    /**
     * Removes the specified object through the constraints.
     */
    public void removeObject (ObjectInfo info)
    {
        _scene.removeObject(info);
        _objectData.remove(info);
    }

    /**
     * Determines whether the constraints allow the specified objects to be
     * added and removed simultaneously.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a translatable string explaining why the objects can't be
     * modified
     */
    public String allowModifyObjects (ObjectInfo[] added,
        ObjectInfo[] removed)
    {
        ObjectData[] addedData = new ObjectData[added.length];
        for (int ii = 0; ii < added.length; ii++) {
            addedData[ii] = createObjectData(added[ii]);
            if (addedData[ii] == null) {
                return INTERNAL_ERROR;
            }
        }

        ObjectData[] removedData = getObjectDataFromInfo(removed);
        if (removedData == null) {
            return INTERNAL_ERROR;
        }

        return allowModifyObjects(addedData, removedData);
    }

    /**
     * Returns an ObjectData array that corresponds to the supplied
     * ObjectInfo array.  Returns null on error.
     */
    protected ObjectData[] getObjectDataFromInfo (ObjectInfo[] info)
    {
        if (info == null) {
            return null;
        }
        ObjectData[] data = new ObjectData[info.length];
        for (int ii = 0; ii < info.length; ii++) {
            data[ii] = _objectData.get(info[ii]);
            if (data[ii] == null) {
                log.warning("Couldn't match object info up to data [info=" +
                        info[ii] + "].");
                return null;
            }
        }
        return data;
    }

    /**
     * Determines whether the constraints allow the specified objects to be
     * added and removed simultaneously.  Subclasses that wish to define
     * additional constraints should override this method.
     *
     * @return <code>null</code> if the constraints allow the operation,
     * otherwise a qualified translatable string explaining why the objects
     * can't be modified
     */
    protected String allowModifyObjects (ObjectData[] added,
        ObjectData[] removed)
    {
        DirectionType dirtype = new DirectionType();

        for (int ii = 0; ii < added.length; ii++) {
            if (added[ii].tile.hasConstraint(ObjectTileSet.ON_SURFACE) &&
                !isOnSurface(added[ii], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_on_surface");
            }

            if (getConstraintDirectionType(added[ii], ObjectTileSet.ON_WALL,
                dirtype) && !isOnWall(added[ii], added, removed, dirtype.dir, dirtype.type)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_on_wall");
            }

            if (getConstraintDirectionType(added[ii], ObjectTileSet.ATTACH,
                    dirtype) && !isAttached(added[ii], added, removed,
                        dirtype.dir, dirtype.type)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.not_attached");
            }

            int dir = getConstraintDirection(added[ii], ObjectTileSet.SPACE);
            if (dir != NONE && !hasSpace(added[ii], added, removed, dir)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.no_space");
            }

            if (hasSpaceConstrainedAdjacent(added[ii], added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.no_space_adj");
            }
        }

        for (ObjectData element : removed) {
            if (element.tile.hasConstraint(ObjectTileSet.SURFACE) &&
                hasOnSurface(element, added, removed)) {
                return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                    "m.has_on_surface");
            }

            int dir = getConstraintDirection(element, ObjectTileSet.WALL);
            if (dir != NONE) {
                if (hasOnWall(element, added, removed, dir)) {
                    return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                        "m.has_on_wall");

                } else if (hasAttached(element, added, removed, dir)) {
                    return MessageBundle.qualify(STAGE_MESSAGE_BUNDLE,
                        "m.has_attached");
                }
            }
        }

        return null;
    }

    /**
     * Determines whether the specified surface has anything on it that won't
     * be held up if the surface is removed.
     */
    protected boolean hasOnSurface (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        List<ObjectData> objects = getObjectData(data.bounds, added, removed);
        for (int ii = 0, size = objects.size(); ii < size; ii++) {
            ObjectData odata = objects.get(ii);
            if (odata.tile.hasConstraint(ObjectTileSet.ON_SURFACE) &&
                !isOnSurface(odata, added, removed)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the specified wall has anything on it that won't be
     * held up if the wall is removed.
     */
    protected boolean hasOnWall (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        DirectionType dirtype = new DirectionType();

        List<ObjectData> objects = getObjectData(data.bounds, added, removed);
        for (int ii = 0, size = objects.size(); ii < size; ii++) {
            ObjectData odata = objects.get(ii);
            if (getConstraintDirectionType(odata, ObjectTileSet.ON_WALL,
                dirtype) && !isAttached(odata, added, removed,
                    dirtype.dir, dirtype.type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the specified wall has anything attached to it that
     * won't be held up if the wall is removed.
     */
    protected boolean hasAttached (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        DirectionType dirtype = new DirectionType();

        List<ObjectData> objects = getObjectData(getAdjacentEdge(data.bounds,
            DirectionUtil.getOpposite(dir)), added, removed);
        for (int ii = 0, size = objects.size(); ii < size; ii++) {
            ObjectData odata = objects.get(ii);
            if (getConstraintDirectionType(odata, ObjectTileSet.ATTACH,
                    dirtype) && !isAttached(odata, added, removed,
                        dirtype.dir, dirtype.type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifies that the objects adjacent to the given object will still have
     * their space constraints met if the object is added.
     */
    protected boolean hasSpaceConstrainedAdjacent (ObjectData data,
        ObjectData[] added, ObjectData[] removed)
    {
        Rectangle r = data.bounds;
        // grow the ObjectData bounds 1 square in each direction
        _constrainRect.setBounds(r.x - 1, r.y - 1, r.width + 2, r.height + 2);

        List<ObjectData> objects = getObjectData(_constrainRect, added, removed);
        for (int ii = 0, size = objects.size(); ii < size; ii++) {
            ObjectData odata = objects.get(ii);
            int dir = getConstraintDirection(odata, ObjectTileSet.SPACE);
            if (dir != NONE && !hasSpace(odata, added, removed, dir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines whether the specified object has empty space in the specified
     * direction.
     */
    protected boolean hasSpace (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir)
    {
        return getObjectData(getAdjacentEdge(data.bounds, dir), added,
            removed).size() == 0;
    }

    /**
     * Determines whether the specified object is on a surface.
     */
    protected boolean isOnSurface (ObjectData data, ObjectData[] added,
        ObjectData[] removed)
    {
        return isCovered(data.bounds, added, removed, ObjectTileSet.SURFACE,
            null);
    }

    /**
     * Determines whether the specified object is on a wall facing the
     * specified direction.
     */
    protected boolean isOnWall (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir, int type)
    {
        return isCovered(data.bounds, added, removed,
            getDirectionalConstraint(ObjectTileSet.WALL, dir), (type == DirectionType.LOW) ?
                getDirectionalConstraint(ObjectTileSet.WALL, dir, DirectionType.LOW) : null);
    }

    /**
     * Determines whether the specified object is attached to another object in
     * the specified direction and at the specified height.
     */
    protected boolean isAttached (ObjectData data, ObjectData[] added,
        ObjectData[] removed, int dir, int type)
    {
        return isCovered(getAdjacentEdge(data.bounds, dir), added, removed,
            getDirectionalConstraint(ObjectTileSet.WALL, dir), (type == DirectionType.LOW) ?
            getDirectionalConstraint(ObjectTileSet.WALL, dir, DirectionType.LOW) : null);
    }

    /**
     * Given a rectangle, determines whether all of the tiles within
     * the rectangle intersect an object.  If the constraint parameter is
     * non-null, the intersected objects must have that constraint (or the
     * alternate constraint, if specified).
     */
    protected boolean isCovered (Rectangle rect, ObjectData[] added,
        ObjectData[] removed, String constraint, String altstraint)
    {
        List<ObjectData> objects = getObjectData(rect, added, removed);
        for (int y = rect.y, ymax = rect.y + rect.height; y < ymax; y++) {
            for (int x = rect.x, xmax = rect.x + rect.width; x < xmax; x++) {
                boolean covered = false;
                for (int ii = 0, size = objects.size(); ii < size; ii++) {
                    ObjectData data = objects.get(ii);
                    if (data.bounds.contains(x, y) && (constraint == null ||
                            data.tile.hasConstraint(constraint) ||
                            (altstraint != null &&
                                data.tile.hasConstraint(altstraint)))) {
                        covered = true;
                        break;
                    }
                }
                if (!covered) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Creates and returns a rectangle that covers the specified rectangle's
     * adjacent edge (the squares one tile beyond the bounds) in the specified
     * direction.
     */
    protected Rectangle getAdjacentEdge (Rectangle rect, int dir)
    {
        switch (dir) {
            case NORTH:
                return new Rectangle(rect.x - 1, rect.y, 1, rect.height);

            case EAST:
                return new Rectangle(rect.x, rect.y - 1, rect.width, 1);

            case SOUTH:
                return new Rectangle(rect.x + rect.width, rect.y, 1,
                    rect.height);

            case WEST:
                return new Rectangle(rect.x, rect.y + rect.height,
                    rect.width, 1);

            default:
                return null;
        }
    }

    /**
     * Returns the direction in which the specified object is constrained by
     * appending "[NESW]" to the given constraint prefix.  Returns
     * <code>NONE</code> if there is no such directional constraint.
     */
    protected int getConstraintDirection (ObjectData data, String prefix)
    {
        DirectionType dirtype = new DirectionType();
        return getConstraintDirectionType(data, prefix, dirtype) ?
            dirtype.dir : NONE;
    }

    /**
     * Populates the supplied {@link ObjectData} object with the direction and height of the
     * constraint identified by the given prefix.
     *
     * @return true if the object was successfully populated, false if there is
     * no such constraint
     */
    protected boolean getConstraintDirectionType (ObjectData data,
        String prefix, DirectionType dirtype)
    {
        String[] constraints = data.tile.getConstraints();
        if (constraints == null) {
            return false;
        }

        for (String constraint : constraints) {
            if (constraint.startsWith(prefix)) {
                int fromidx = prefix.length(),
                    toidx = constraint.indexOf('_', fromidx);
                dirtype.dir = DirectionUtil.fromShortString(toidx == -1 ?
                    constraint.substring(fromidx) :
                    constraint.substring(fromidx, toidx));
                dirtype.type = getConstraintWallType(constraint);
                return true;
            }
        }
        return false;
    }

    protected int getConstraintWallType (String constraint)
    {
        return constraint.endsWith(ObjectTileSet.LOW) ? DirectionType.LOW : DirectionType.NORM;
    }

    /**
     * Given a constraint prefix and a direction, returns the directional
     * constraint.
     */
    protected String getDirectionalConstraint (String prefix, int dir)
    {
        return getDirectionalConstraint(prefix, dir, DirectionType.NORM);
    }

    /**
     * Given a constraint prefix, direction, and height, returns the
     * directional constraint.
     */
    protected String getDirectionalConstraint (String prefix, int dir, int type)
    {
        return prefix + DirectionUtil.toShortString(dir) +
            (type == DirectionType.LOW ? ObjectTileSet.LOW : "");
    }

    /**
     * Finds all objects whose bounds intersect the given rectangle and
     * returns a list containing their {@link ObjectData} elements.
     *
     * @param added an array of objects to add to the search
     * @param removed an array of objects to exclude from the search
     */
    protected List<ObjectData> getObjectData (Rectangle rect, ObjectData[] added,
        ObjectData[] removed)
    {
        List<ObjectData> list = Lists.newArrayList();

        for (ObjectData data : _objectData.values()) {
            if (rect.intersects(data.bounds) && !ListUtil.contains(removed,
                    data)) {
                list.add(data);
            }
        }

        for (ObjectData element : added) {
            if (rect.intersects(element.bounds)) {
                list.add(element);
            }
        }

        return list;
    }

    /**
     * Using the tile manager, computes and returns the specified object's
     * data.
     */
    protected ObjectData createObjectData (ObjectInfo info)
    {
        try {
            ObjectTile tile = (ObjectTile)_tilemgr.getTile(info.tileId);
            Rectangle bounds = new Rectangle(info.x, info.y, tile.getBaseWidth(),
                tile.getBaseHeight());
            bounds.translate(1 - bounds.width, 1 - bounds.height);
            return new ObjectData(bounds, tile);

        } catch (Exception e) {
            log.warning("Error retrieving tile for object [info=" + info + "].", e);
            return null;
        }
    }

    /** Contains information about an object used in checking constraints. */
    protected class ObjectData
    {
        public Rectangle bounds;
        public ObjectTile tile;

        public ObjectData (Rectangle bounds, ObjectTile tile)
        {
            this.bounds = bounds;
            this.tile = tile;
        }
    }

    /** Contains the direction and height of a constraint. */
    protected static class DirectionType
    {
        public int dir;
        public int type;

        public static int NORM = 0;
        public static int LOW = 1;
    }

    /** The tile manager to use for object dimensions and constraints. */
    protected TileManager _tilemgr;

    /** The scene being checked for constraints. */
    protected StageScene _scene;

    /** The Miso scene model. */
    protected StageMisoSceneModel _mmodel;

    /** For all objects in the scene, maps {@link ObjectInfo}s to {@link ObjectData}s. */
    protected HashMap<ObjectInfo, ObjectData> _objectData = Maps.newHashMap();

    /** One rectangle we'll re-use for all constraints ops. */
    protected static final Rectangle _constrainRect = new Rectangle();
}
