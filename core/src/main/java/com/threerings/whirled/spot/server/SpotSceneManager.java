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

package com.threerings.whirled.spot.server;

import java.util.HashMap;
import java.util.Iterator;

import com.google.common.collect.Maps;

import com.samskivert.util.HashIntMap;

import com.threerings.util.Name;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.chat.data.UserMessage;
import com.threerings.crowd.chat.server.SpeakUtil;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.server.PlaceRegistry;

import com.threerings.whirled.server.SceneManager;
import com.threerings.whirled.spot.data.Cluster;
import com.threerings.whirled.spot.data.ClusterObject;
import com.threerings.whirled.spot.data.ClusteredBodyObject;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SceneLocation;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotScene;
import com.threerings.whirled.spot.data.SpotSceneObject;

import static com.threerings.whirled.spot.Log.log;

/**
 * Handles the movement of bodies between locations in the scene and creates the necessary
 * distributed objects to allow bodies in clusters to chat with one another.
 */
public class SpotSceneManager extends SceneManager
    implements SpotCodes
{
    /**
     * Move the specified body to the default portal, if possible.
     */
    public static void moveBodyToDefaultPortal (PlaceRegistry plreg, BodyObject body)
    {
        SpotSceneManager mgr = (SpotSceneManager)plreg.getPlaceManager(body.getPlaceOid());
        if (mgr != null) {
            mgr.moveToDefaultPortal(body);
        }
    }

    /**
     * Assigns a starting location for an entering body. This will happen before the body is made
     * to "occupy" the scene (defined by their having an occupant info record). So when they do
     * finally occupy the scene, the client will know where to render them.
     */
    public void mapEnteringBody (BodyObject body, Portal from)
    {
        // don't save a null from portal, because it simply means "use the default entrance"
        if (from != null) {
            _enterers.put(body.getOid(), from);
        }
    }

    /**
     * Called if a body failed to enter our scene after we assigned them an entering position.
     */
    public void clearEnteringBody (BodyObject body)
    {
        _enterers.remove(body.getOid());
    }

    /**
     * This is called when a user requests to traverse a portal from this scene to another scene.
     * The manager may return an error code string that will be reported back to the caller
     * explaining the failure or <code>null</code> indicating that it is OK for the caller to
     * traverse the portal.
     */
    public String mayTraversePortal (BodyObject body, Portal portal)
    {
        return null;
    }

    /**
     * This is called to let this scene manager know that the user is about to traverse the
     * specified portal. The default implementation relocates the user to the location associated
     * with the portal. It is still possible that the traversal will fail, so don't do anything too
     * crazy.
     */
    public void willTraversePortal (BodyObject body, Portal portal)
    {
        updateLocation(body, portal.getLocation());
    }

    @Override
    protected void didStartup ()
    {
        // get a casted reference to our place object (we need to do this before calling
        // super.didStartup() because that will call sceneManagerDidResolve() which may start
        // letting people into the scene)
        _ssobj = (SpotSceneObject)_plobj;

        super.didStartup();
    }

    @Override
    protected void gotSceneData (Object extras)
    {
        super.gotSceneData(extras);

        // keep a casted reference around to our scene
        _sscene = (SpotScene)_scene;
    }

    @Override
    protected void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // clear out their location information
        _ssobj.removeFromOccupantLocs(Integer.valueOf(bodyOid));

        // clear any cluster they may occupy
        removeFromCluster(bodyOid);

        // let's make damned sure they're not in any cluster
        Iterator<ClusterRecord> cliter = _clusters.values().iterator();
        while (cliter.hasNext()) {
            ClusterRecord clrec = cliter.next();
            if (clrec.containsKey(bodyOid)) {
                log.info("Pruning departed body from cluster", "boid", bodyOid, "cluster", clrec);
                clrec.removeBody(bodyOid);
                if (clrec.size() == 0) {
                    // if we just removed the last body, destroy the cluster, need to use the
                    // iterator's removal so we don't hose ourselves
                    clrec.destroy(false);
                    cliter.remove();
                }
            }
        }
    }

    @Override
    protected void addOccupantInfo (BodyObject body, OccupantInfo info)
    {
        // assign this body a starting location in the scene
        assignStartingLocation(body);

        // then call super, so we know that our info will be ready before bodyAdded is triggered
        super.addOccupantInfo(body, info);
    }

    /**
     * Give our new body a starting location.
     */
    protected void assignStartingLocation (BodyObject body)
    {
        Portal from = _enterers.remove(body.getOid());
        Portal entry;
        if (from != null && from.targetPortalId != -1) {
            entry = _sscene.getPortal(from.targetPortalId);
            if (entry == null) {
                log.warning("Body mapped at invalid portal",
                    "where", where(), "who", body.who(), "from", from);
                entry = _sscene.getDefaultEntrance();
            }
        } else {
            entry = _sscene.getDefaultEntrance();
        }

//        log.debug("Positioning entering body", "who", body.who(), "where", entry.getOppLocation());

        // create a scene location for them located on the entrance portal but facing the opposite
        // direction
        _ssobj.addToOccupantLocs(computeEnteringLocation(body, from, entry));
    }

    /**
     * Called when the supplied body is entering our scene via the specified portal. The default
     * location is the one associated with the portal, but derived classes may wish to adjust this.
     *
     * @param from the portal the body followed to get to this scene (or null).
     * @param entry the portal referenced by the from portal's targetPortalId or the scene's
     * default entrance if the from portal did not exist or had no target portal.
     */
    protected SceneLocation computeEnteringLocation (BodyObject body, Portal from, Portal entry)
    {
        return new SceneLocation(entry.getOppLocation(), body.getOid());
    }

    /**
     * Move the specified body to the default portal, if possible.
     */
    protected void moveToDefaultPortal (BodyObject body)
    {
        SpotScene scene = (SpotScene)getScene();
        if (scene == null) {
            log.warning("No scene in moveBodyToDefaultPortal()?",
                "who", body.who(), "where", where());
            return;
        }

        try {
            Location eloc = scene.getDefaultEntrance().getLocation();
            handleChangeLoc(body, eloc);
        } catch (InvocationException ie) {
            log.warning("Could not move user to default portal",
                "where", where(), "who", body.who(), "error", ie);
        }
    }

    /**
     * Called by the {@link SpotProvider} when we receive a request by a user to occupy a
     * particular location.
     *
     * @param source the body to be moved.
     * @param loc the location to which to move the body.
     *
     * @exception InvocationException thrown with a reason code explaining the failure if there is
     * a problem processing the request.
     */
    protected void handleChangeLoc (BodyObject source, Location loc)
        throws InvocationException
    {
        // make sure they are in our scene
        if (!_ssobj.occupants.contains(source.getOid())) {
            log.warning("Refusing change loc from non-scene occupant",
                "where", where(), "who", source.who(), "loc", loc);
            throw new InvocationException(INTERNAL_ERROR);
        }

        // let our derived classes decide if this is an OK place to stand
        if (!validateLocation(source, loc)) {
            throw new InvocationException(INVALID_LOCATION);
        }

        // update the user's location information in the scene which will indicate to the client
        // that their avatar should be moved from its current position to their new position
        updateLocation(source, loc);

        // remove them from any cluster as they've departed
        removeFromCluster(source.getOid());
    }

    /**
     * Derived classes can override this method and validate that the specified body can stand in
     * the requested location. The default implementation returns <code>true</code> in all
     * circumstances; stand where ye may!
     */
    protected boolean validateLocation (BodyObject source, Location loc)
    {
        return true;
    }

    /**
     * Updates the location of the specified body.
     */
    protected void updateLocation (BodyObject source, Location loc)
    {
        SceneLocation sloc = new SceneLocation(loc, source.getOid());
        if (!_ssobj.occupantLocs.contains(sloc)) {
            // complain if they don't already have a location configured
            log.warning("Changing loc for occupant without previous loc",
                "where", where(), "who", source.who(), "nloc", loc, new Exception());
            _ssobj.addToOccupantLocs(sloc);
        } else {
            _ssobj.updateOccupantLocs(sloc);
        }
    }

    /**
     * Called by the {@link SpotProvider} when we receive a request by a user to join a particular
     * cluster.
     *
     * @param joiner the body to be moved.
     * @param targetOid the bodyOid of another user or the oid of an existing cluster; the moving
     * user will be made to join the other user's cluster.
     *
     * @exception InvocationException thrown with a reason code explaining the failure if there is
     * a problem processing the request.
     */
    protected void handleJoinCluster (BodyObject joiner, int targetOid)
        throws InvocationException
    {
        // if the cluster already exists, add this user and be done
        ClusterRecord clrec = _clusters.get(targetOid);
        if (clrec != null) {
            clrec.addBody(joiner);
            return;
        }

        // otherwise see if they sent us the user's oid
        DObject tobj = _omgr.getObject(targetOid);
        if (!(tobj instanceof BodyObject)) {
            log.info("Can't join cluster, missing target",
                "creator", joiner.who(), "targetOid", targetOid);
            throw new InvocationException(NO_SUCH_CLUSTER);
        }

        // make sure we're in the same scene as said user
        BodyObject friend = (BodyObject)tobj;
        if (friend.getPlaceOid() != joiner.getPlaceOid()) {
            log.info("Refusing cluster join from non-proximate user",
                "joiner", joiner.who(), "jloc", joiner.location, "target", friend.who(),
                "tloc", friend.location);
            throw new InvocationException(NO_SUCH_CLUSTER);
        }

        // see if the friend is already in a cluster
        clrec = getCluster(friend.getOid());
        if (clrec != null) {
            clrec.addBody(joiner);
            return;
        }

        // confirm that they can start a cluster with this unsuspecting other person
        checkCanCluster(joiner, friend);

        // otherwise we create a new cluster and add our charter members!
//        log.debug("Creating cluster", "starter", joiner.who(), "tgt", friend.who());
        clrec = createClusterRecord();
        clrec.addBody(friend);
        clrec.addBody(joiner);
    }

    /**
     * Creates the cluster record instance that we'll use to manage our cluster.
     */
    protected ClusterRecord createClusterRecord ()
    {
        return new ClusterRecord();
    }

    /**
     * Gives derived classes an opportunity to veto a user's attempt to start a cluster with
     * another user. If the attempt should be vetoed, this method should throw an {@link
     * InvocationException} indicating the reason for veto.
     */
    protected void checkCanCluster (BodyObject initiator, BodyObject target)
        throws InvocationException
    {
        // nothing to do by default
    }

    /**
     * Removes the specified user from any cluster they occupy.
     */
    protected void removeFromCluster (int bodyOid)
    {
        ClusterRecord clrec = getCluster(bodyOid);
        if (clrec != null) {
            clrec.removeBody(bodyOid);
            // If that was the last person, destroy the cluster
            if (clrec.size() == 0) {
                clrec.destroy(true);
            }
        }
    }

    /**
     * Fetches the cluster record for the specified body.
     */
    protected ClusterRecord getCluster (int bodyOid)
    {
        BodyObject bobj = (BodyObject)_omgr.getObject(bodyOid);
        if (bobj instanceof ClusteredBodyObject) {
            return _clusters.get(((ClusteredBodyObject)bobj).getClusterOid());
        } else {
            return null;
        }
    }

    /**
     * Called by the {@link SpotProvider} when we receive a cluster speak request.
     */
    @Deprecated
    protected void handleClusterSpeakRequest (
        int sourceOid, Name source, String bundle, String message, byte mode)
    {
        handleClusterMessageRequest(sourceOid, new UserMessage(source, bundle, message, mode));
    }

    /**
     * Called by the {@link SpotProvider} when we receive a cluster message request.
     */
    protected void handleClusterMessageRequest (int sourceOid, UserMessage message)
    {
        ClusterRecord clrec = getCluster(sourceOid);
        if (clrec == null) {
            log.warning("Non-clustered user requested cluster speak",
                "where", where(), "chatter", (message.speaker + " (" + sourceOid + ")"),
                "msg", message);
        } else {
            SpeakUtil.sendMessage(clrec.getClusterObject(), message);
        }
    }

    /**
     * Returns the location of the specified body or null if they have no location in this scene.
     */
    protected SceneLocation locationForBody (int bodyOid)
    {
        return _ssobj.occupantLocs.get(Integer.valueOf(bodyOid));
    }

    /**
     * Verifies that the specified cluster can be expanded to include another body.
     */
    protected boolean canAddBody (ClusterRecord clrec, BodyObject body)
    {
        return true;
    }

    /**
     * Called when a user is added to a cluster. The scene manager implementation should take this
     * opportunity to rearrange everyone in the cluster appropriately for the new size.
     */
    protected void bodyAdded (ClusterRecord clrec, BodyObject body)
    {
    }

    /**
     * Called when a user is removed from a cluster. The scene manager implementation should take
     * this opportunity to rearrange everyone in the cluster appropriately for the new size.
     */
    protected void bodyRemoved (ClusterRecord clrec, BodyObject body)
    {
    }

    /**
     * Used to manage clusters which are groups of users that can chat to one another.
     */
    protected class ClusterRecord extends HashIntMap<ClusteredBodyObject>
    {
        public ClusterRecord ()
        {
            _clobj = _omgr.registerObject(new ClusterObject());
            _clusters.put(_clobj.getOid(), this);

            // let any mapped users know about our cluster
            for (ClusteredBodyObject body : values()) {
                body.setClusterOid(_clobj.getOid());
                _clobj.addToOccupants(((BodyObject)body).getOid());
            }

            // configure our cluster record and publish it
            _cluster.clusterOid = _clobj.getOid();
            _ssobj.addToClusters(_cluster);
        }

        public boolean addBody (BodyObject body)
            throws InvocationException
        {
            if (!(body instanceof ClusteredBodyObject)) {
                log.warning("Refusing to add non-clustered body to cluster",
                    "cloid", _clobj.getOid(), "size", size(), "who", body.who());
                throw new InvocationException(INTERNAL_ERROR);
            }

            // if they're already in the cluster, do nothing
            if (containsKey(body.getOid())) {
                return false;
            }

            // make sure we can add this body
            if (!canAddBody(this, body)) {
//                 Log.debug("Cluster full, refusing growth " + this + ".");
                throw new InvocationException(CLUSTER_FULL);
            }

            // make sure our intrepid joiner is not in any another cluster
            removeFromCluster(body.getOid());

            put(body.getOid(), (ClusteredBodyObject)body);
            _ssobj.startTransaction();
            try {
                body.startTransaction();
                try {
                    bodyAdded(this, body); // do the hokey pokey
                    if (_clobj != null) {
                        ((ClusteredBodyObject)body).setClusterOid(_clobj.getOid());
                        _clobj.addToOccupants(body.getOid());
                        _ssobj.updateClusters(_cluster);
                    }

                } finally {
                    body.commitTransaction();
                }
            } finally {
                _ssobj.commitTransaction();
            }

//             log.debug("Added " + body.who() + " to "+ this + ".");
            return true;
        }

        public void removeBody (int bodyOid)
        {
            BodyObject body = (BodyObject)remove(bodyOid);
            if (body == null) {
                log.warning("Requested to remove unknown body from cluster",
                    "cloid", _clobj.getOid(), "size", size(), "who", bodyOid);
                return;
            }

            if (body.isActive()) {
                body.startTransaction();
            }
            try {
                _ssobj.startTransaction();
                try {
                    ((ClusteredBodyObject)body).setClusterOid(-1);
                    bodyRemoved(this, body); // do the hokey pokey
                    if (_clobj != null) {
                        _clobj.removeFromOccupants(bodyOid);
                        _ssobj.updateClusters(_cluster);
                    }

                } finally {
                    _ssobj.commitTransaction();
                }

            } finally {
                if (body.isActive()) {
                    body.commitTransaction();
                }
            }

//             log.debug("Removed " + bodyOid + " from "+ this + ".");

        }

        public ClusterObject getClusterObject ()
        {
            return _clobj;
        }

        public Cluster getCluster ()
        {
            return _cluster;
        }

        @Override
        public String toString ()
        {
            return "[cluster=" + _cluster + ", size=" + size() + "]";
        }

        protected void destroy (boolean doRemoval)
        {
//             log.debug("Cluster empty, going away", "cloid", _clobj.getOid());
            _ssobj.removeFromClusters(_cluster.getKey());

            // if we've also been requested to remove ourself from the clusters list, do that
            if (doRemoval) {
                _clusters.remove(_clobj.getOid());
            }
            _omgr.destroyObject(_clobj.getOid());
        }

        protected ClusterObject _clobj;
        protected Cluster _cluster = new Cluster();
    }

    /** A casted reference to our place object. */
    protected SpotSceneObject _ssobj;

    /** A casted reference to our scene instance. */
    protected SpotScene _sscene;

    /** Records with information on all clusters in this scene. */
    protected HashIntMap<ClusterRecord> _clusters = new HashIntMap<ClusterRecord>();

    /** A mapping of entering bodies to portal ids. */
    protected HashMap<Integer, Portal> _enterers = Maps.newHashMap();
}
