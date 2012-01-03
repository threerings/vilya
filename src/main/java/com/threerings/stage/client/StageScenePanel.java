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

package com.threerings.stage.client;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import com.samskivert.util.Tuple;

import com.samskivert.swing.Controller;
import com.samskivert.swing.ControllerProvider;
import com.samskivert.swing.util.SwingUtil;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.media.tile.ObjectTile;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.UniformTileSet;

import com.threerings.miso.client.MisoScenePanel;
import com.threerings.miso.client.SceneObject;
import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.util.MisoUtil;

import com.threerings.whirled.data.SceneUpdate;
import com.threerings.whirled.spot.data.Cluster;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SceneLocation;

import com.threerings.stage.data.StageLocation;
import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.util.StageContext;
import com.threerings.stage.util.StageSceneUtil;

import static com.threerings.stage.Log.log;

/**
 * Extends the basic Miso scene panel with Stage fun stuff like portals, clusters and locations.
 */
public class StageScenePanel extends MisoScenePanel
    implements ControllerProvider, KeyListener, PlaceView
{
    /** An action command generated when the user clicks on a location within the scene. */
    public static final String LOCATION_CLICKED = "LocationClicked";

    /** An action command generated when a cluster is clicked. */
    public static final String CLUSTER_CLICKED = "ClusterClicked";

    /** Show flag that indicates we should show all clusters. */
    public static final int SHOW_CLUSTERS = (1 << 1);

    /** Show flag that indicates we should render known land plots
     * (expensive, don't turn this on willy nilly). */
    public static final int SHOW_PLOTS = (1 << 2);

    /**
     * Constructs a stage scene view panel.
     */
    public StageScenePanel (StageContext ctx, Controller ctrl)
    {
        super(ctx, StageSceneUtil.getMetrics());

        // keep these around for later
        _ctx = ctx;
        _ctrl = ctrl;
        _ctrl.setControlledPanel(this);

        // no layout manager
        setLayout(null);
    }

    /**
     * Get the tileset colorizer in use in this scene.
     */
    public SceneColorizer getColorizer ()
    {
        return _rizer;
    }

    @Override
    protected TileSet.Colorizer getColorizer (ObjectInfo oinfo)
    {
        return _rizer.getColorizer(oinfo);
    }

    /**
     * Returns the scene being displayed by this panel. Do not modify it.
     */
    public StageScene getScene ()
    {
        return _scene;
    }

    /**
     * Sets the scene managed by the panel.
     */
    public void setScene (StageScene scene)
    {
        _scene = scene;
        if (_scene != null) {
            _rizer = new SceneColorizer(_ctx.getColorPository(), scene);
            recomputePortals();
            setSceneModel(StageMisoSceneModel.getSceneModel(scene.getSceneModel()));
        } else {
            log.warning("Zoiks! We can't display a null scene!");
            // TODO: display something to the user letting them know that
            // we're so hosed that we don't even know what time it is
        }
    }

    /**
     * Called when we have received a scene update from the server.
     */
    public void sceneUpdated (SceneUpdate update)
    {
        // recompute the portals as those may well have changed
        recomputePortals();

        // we go ahead and completely replace our scene model which will reload the whole good
        // goddamned business; it is a little shocking to the user, but it's guaranteed to work
        refreshScene();
    }

    /**
     * Computes a set of display objects for the portals in this scene.
     */
    protected void recomputePortals ()
    {
        // create scene objects for our portals
        UniformTileSet ots = loadPortalTileSet();

        _portobjs.clear();
        for (Iterator<Portal> iter = _scene.getPortals(); iter.hasNext(); ) {
            Portal portal = iter.next();
            StageLocation loc = (StageLocation) portal.loc;
            Point p = getScreenCoords(loc.x, loc.y);
            int tx = MisoUtil.fullToTile(loc.x);
            int ty = MisoUtil.fullToTile(loc.y);
            Point ts = MisoUtil.tileToScreen(_metrics, tx, ty, new Point());

//             log.info("Added portal", "portal", portal, "screen", StringUtil.toString(p), "tile",
//                StringUtil.coordsToString(tx, ty), "tscreen", StringUtil.toString(ts));

            ObjectInfo info = new ObjectInfo(0, tx, ty);
            info.action = "portal:" + portal.portalId;

            // TODO: cache me
            ObjectTile tile = new PortalObjectTile(
                ts.x + _metrics.tilehwid - p.x + (PORTAL_ICON_WIDTH / 2),
                ts.y + _metrics.tilehei - p.y + (PORTAL_ICON_HEIGHT / 2));
            tile.setImage(ots.getTileMirage(loc.orient));

            _portobjs.add(new SceneObject(this, info, tile) {
                @Override
                public boolean setHovered (boolean hovered) {
                    ((PortalObjectTile)this.tile).hovered = hovered;
                    return isResponsive();
                }
            });
        }
    }

    @Override
    protected void recomputeVisible ()
    {
        super.recomputeVisible();

        // add our visible portal objects to the list of visible objects
        for (int ii = 0, ll = _portobjs.size(); ii < ll; ii++) {
            SceneObject pobj = _portobjs.get(ii);
            if (pobj.bounds != null && _vbounds.intersects(pobj.bounds)) {
                _vizobjs.add(pobj);
            }
        }
    }

    // documentation inherited from interface ControllerProvider
    public Controller getController ()
    {
        return _ctrl;
    }

    // documentation inherited from interface KeyListener
    public void keyPressed (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            // display all tooltips
            setShowFlags(SHOW_TIPS, true);
        }
    }

    // documentation inherited from interface KeyListener
    public void keyReleased (KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_ALT) {
            // stop displaying all tooltips
            setShowFlags(SHOW_TIPS, defaultShowTips());
        }
    }

    // documentation inherited from interface PlaceView
    public void willEnterPlace (PlaceObject plobj)
    {
    }

    // documentation inherited from interface PlaceView
    public void didLeavePlace (PlaceObject plobj)
    {
    }

    /**
     * Returns true if we should always show the object tooltips by
     * default, false if they should only be shown while the 'Alt' key is
     * depressed.
     */
    protected boolean defaultShowTips ()
    {
        return false;
    }

    // documentation inherited
    public void keyTyped (KeyEvent e)
    {
        // nothing
    }

    @Override
    protected boolean handleMousePressed (Object hobject, MouseEvent event)
    {
        // let our parent have a crack at the old mouse press
        if (super.handleMousePressed(hobject, event)) {
            return true;
        }

        // if the hover object is a cluster, we clicked it!
        if (event.getButton() == MouseEvent.BUTTON1) {
            if (hobject instanceof Cluster) {
                Object actarg = new Tuple<Object, Point>(hobject, event.getPoint());
                Controller.postAction(this, CLUSTER_CLICKED, actarg);
            } else {
                // post an action indicating that we've clicked on a location
                Point lc = MisoUtil.screenToFull(_metrics, event.getX(), event.getY(), new Point());
                Controller.postAction(this, LOCATION_CLICKED,
                                      new StageLocation(lc.x, lc.y, (byte)0));
            }
            return true;
        }
        return false;
    }

    /**
     * Called when our show flags have changed.
     */
    @Override
    protected void showFlagsDidChange (int oldflags)
    {
        super.showFlagsDidChange(oldflags);

        if ((oldflags & SHOW_CLUSTERS) != (_showFlags & SHOW_CLUSTERS)) {
            // dirty every cluster rectangle
            for (Shape shape : _clusters.values()) {
                dirtyCluster(shape);
            }
        }
    }

    /**
     * Called when a real cluster is created or updated in the scene.
     */
    protected void clusterUpdated (Cluster cluster)
    {
        // compute a screen rectangle that contains all possible "spots" in this cluster
        List<SceneLocation> spots = StageSceneUtil.getClusterLocs(cluster);
        Rectangle cbounds = null;
        for (int ii = 0, ll = spots.size(); ii < ll; ii++) {
            StageLocation loc = ((StageLocation) spots.get(ii).loc);
            Point sp = getScreenCoords(loc.x, loc.y);
            if (cbounds == null) {
                cbounds = new Rectangle(sp.x, sp.y, 0, 0);
            } else {
                cbounds.add(sp.x, sp.y);
            }
        }

        if (cbounds == null) {
            // if we found no one actually in this cluster, nix it
            removeCluster(cluster.clusterOid);
        } else {
            // otherwise have the view update the cluster
            updateCluster(cluster, cbounds);
        }
    }

    /**
     * Adds or updates the specified cluster in the view. Metrics will be created that allow the
     * cluster to be rendered and hovered over.
     *
     * @param cluster the cluster record to be added.
     * @param bounds the screen coordinates that bound the occupants of the cluster.
     */
    public void updateCluster (Cluster cluster, Rectangle bounds)
    {
        // dirty any old bounds
        dirtyCluster(cluster);

        // compute the screen coordinate bounds of this cluster
        Shape shape = new Ellipse2D.Float(bounds.x, bounds.y, bounds.width, bounds.height);
        _clusters.put(cluster, shape);

        // if the mouse is inside these bounds, we highlight this cluster
        Shape mshape = new Ellipse2D.Float(
            bounds.x-CLUSTER_SLOP, bounds.y-CLUSTER_SLOP,
            bounds.width+2*CLUSTER_SLOP, bounds.height+2*CLUSTER_SLOP);
        _clusterWells.put(cluster, mshape);

        // dirty our new bounds
        dirtyCluster(shape);
    }

    /**
     * Removes the specified cluster from the view.
     *
     * @return true if such a cluster existed and was removed.
     */
    public boolean removeCluster (int clusterOid)
    {
        Cluster key = new Cluster();
        key.clusterOid = clusterOid;
        _clusterWells.remove(key);
        Shape shape = _clusters.remove(key);
        if (shape == null) {
            return false;
        }

        dirtyCluster(shape);
        // clear out the hover object if this cluster was it
        if (_hobject instanceof Cluster && ((Cluster)_hobject).clusterOid == clusterOid) {
            _hobject = null;
        }
        return true;
    }

    /**
     * A place for subclasses to react to the hover object changing.
     * One of the supplied arguments may be null.
     */
    @Override
    protected void hoverObjectChanged (Object oldHover, Object newHover)
    {
        super.hoverObjectChanged(oldHover, newHover);

        if (oldHover instanceof Cluster) {
            dirtyCluster((Cluster)oldHover);
        }
        if (newHover instanceof Cluster) {
            dirtyCluster((Cluster)newHover);
        }
    }

    /**
     * Gives derived classes a chance to compute a hover object that takes
     * precedence over sprites and actionable objects. If this method
     * returns non-null, no sprite or object hover calculations will be
     * performed and the object returned will become the new hover object.
     */
    @Override
    protected Object computeOverHover (int mx, int my)
    {
        return null;
    }

    /**
     * Gives derived classes a chance to compute a hover object that is
     * used if the mouse is not hovering over a sprite or actionable
     * object. If this method is called, it means that there are no
     * sprites or objects under the mouse. Thus if it returns non-null,
     * the object returned will become the new hover object.
     */
    @Override
    protected Object computeUnderHover (int mx, int my)
    {
        if (!isResponsive()) {
            return null;
        }

        // if the current hover object is a cluster, see if we're still in that cluster
        if (_hobject instanceof Cluster) {
            Cluster cluster = (Cluster)_hobject;
            if (containsPoint(cluster, mx, my)) {
                return cluster;
            }
        }

        // otherwise, check to see if the mouse is in some new cluster
        for (Cluster cluster : _clusters.keySet()) {
            if (containsPoint(cluster, mx, my)) {
                return cluster;
            }
        }

        return null;
    }

    /**
     * Returns true if the specified cluster contains the supplied screen coordinate.
     */
    protected boolean containsPoint (Cluster cluster, int mx, int my)
    {
        Shape shape = _clusterWells.get(cluster);
        return (shape == null) ? false : shape.contains(mx, my);
    }

    /**
     * Dirties the supplied cluster.
     */
    protected void dirtyCluster (Cluster cluster)
    {
        if (cluster != null) {
            dirtyCluster(_clusters.get(cluster));
        }
    }

    /**
     * Dirties the supplied cluster rectangle.
     */
    protected void dirtyCluster (Shape shape)
    {
        if (shape != null) {
            Rectangle r = shape.getBounds();
            _remgr.invalidateRegion(
                r.x - (CLUSTER_PAD / 2),
                r.y - (CLUSTER_PAD / 2),
                r.width + (CLUSTER_PAD * 3 / 2),
                r.height + (CLUSTER_PAD * 3 / 2));
        }
    }

    /**
     * Returns the portal at the specified full coordinates or null if no
     * portal exists at said coordinates.
     */
    public Portal getPortal (int fullX, int fullY)
    {
        Iterator<Portal> iter = _scene.getPortals();
        while (iter.hasNext()) {
            Portal portal = iter.next();
            StageLocation loc = (StageLocation) portal.loc;
            if (loc.x == fullX && loc.y == fullY) {
                return portal;
            }
        }
        return null;
    }

    @Override
    protected void paintBaseDecorations (Graphics2D gfx, Rectangle clip)
    {
        super.paintBaseDecorations(gfx, clip);

        paintClusters(gfx, clip);
    }

    /**
     * Paints any visible clusters.
     */
    protected void paintClusters (Graphics2D gfx, Rectangle clip)
    {
        // remember how daddy's things were arranged
        Object oalias = SwingUtil.activateAntiAliasing(gfx);
        Composite ocomp = gfx.getComposite();
        Stroke ostroke = gfx.getStroke();

        // get ready to draw clusters
        gfx.setStroke(CLUSTER_STROKE);
        gfx.setColor(CLUSTER_COLOR);

        if (checkShowFlag(SHOW_CLUSTERS)
            /* || // _alwaysShowClusters.getValue() */) {
            // draw all clusters
            for (Cluster cluster : _clusters.keySet()) {
                drawCluster(gfx, clip, cluster);
            }

        } else if (_hobject instanceof Cluster) {
            // or just draw the active cluster
            drawCluster(gfx, clip, (Cluster)_hobject);
        }

        // put back daddy's things
        gfx.setComposite(ocomp);
        gfx.setStroke(ostroke);
        SwingUtil.restoreAntiAliasing(gfx, oalias);
    }

    /**
     * Draw the cluster specified by the rectangle.
     */
    protected void drawCluster (Graphics2D gfx, Rectangle clip, Cluster cluster)
    {
        Shape shape = _clusters.get(cluster);
        if ((shape != null) && shape.intersects(clip)) {
            if (_hobject == cluster) {
                gfx.setComposite(HIGHLIGHT_ALPHA);
            } else {
                gfx.setComposite(SHOWN_ALPHA);
            }
            gfx.draw(shape);
        }
    }

    /**
     * Returns true if the specified location is associated with a portal.
     */
    protected boolean isPortal (Location loc)
    {
        if (_scene == null) {
            return false;
        }
        Iterator<Portal> iter = _scene.getPortals();
        while (iter.hasNext()) {
            if (loc.equals(iter.next().loc)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Loads up the tileset used to render the portal arrows.
     */
    protected UniformTileSet loadPortalTileSet ()
    {
        return _ctx.getTileManager().loadTileSet(
            "media/stage/portal_arrows.png",
            PORTAL_ICON_WIDTH, PORTAL_ICON_HEIGHT);
    }

    /** Used to render portals as objects in a scene. */
    protected class PortalObjectTile extends ObjectTile
    {
        public boolean hovered;

        public PortalObjectTile (int ox, int oy) {
            setOrigin(ox, oy);
        }

        @Override
        public void paint (Graphics2D gfx, int x, int y) {
            Composite ocomp = gfx.getComposite();
            if (!isResponsive() || !hovered) {
                gfx.setComposite(INACTIVE_PORTAL_ALPHA);
            }
            super.paint(gfx, x, y);
            gfx.setComposite(ocomp);
        }
    }

    /** A reference to our client context. */
    protected StageContext _ctx;

    /** The controller with which we work in tandem. */
    protected Controller _ctrl;

    /** Our currently displayed scene. */
    protected StageScene _scene;

    /** Contains scene objects for our portals. */
    protected List<SceneObject> _portobjs = Lists.newArrayList();

    /** Shapes describing the clusters, indexed by cluster. */
    protected Map<Cluster, Shape> _clusters = Maps.newHashMap();

    /** Shapes describing the clusters, indexed by cluster. */
    protected Map<Cluster, Shape> _clusterWells = Maps.newHashMap();

    /** Handles scene object colorization. */
    protected SceneColorizer _rizer;

//     /** A debug hook that toggles always-on rendering of clusters. */
//     protected static RuntimeAdjust.BooleanAdjust _alwaysShowClusters =
//         new RuntimeAdjust.BooleanAdjust(
//             "Causes all clusters to always be rendered.",
//             "yohoho.miso.always_show_clusters",
//             ClientPrefs.config, false);

    /** The width of the portal icons. */
    protected static final int PORTAL_ICON_WIDTH = 48;

    /** The height of the portal icons. */
    protected static final int PORTAL_ICON_HEIGHT = 48;

    /** The distance within which the mouse must be from a location in order to highlight it. */
    protected static final int MAX_LOCATION_DIST = 25;

    /** The amount the stroke a cluster. */
    protected static final int CLUSTER_PAD = 4;

    /** The width with which to draw the cluster. */
    protected static final Stroke CLUSTER_STROKE = new BasicStroke(CLUSTER_PAD);

    /** The color used to render clusters. */
    protected static final Color CLUSTER_COLOR = Color.ORANGE;

    /** Alpha level used to hightlight locations or clusters. */
    protected static final Composite HIGHLIGHT_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f);

    /** Alpha level used to render clusters when they're not selected. */
    protected static final Composite SHOWN_ALPHA =
        AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.15f);

    /** The alpha with which to render inactive portals. */
    protected static final Composite INACTIVE_PORTAL_ALPHA = HIGHLIGHT_ALPHA;

    /** The number of pixels outside a cluster when we assume the mouse is "over" that cluster. */
    protected static final int CLUSTER_SLOP = 25;
}
