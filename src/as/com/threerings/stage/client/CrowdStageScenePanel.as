//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

import flash.display.DisplayObject;

import flash.geom.Point;
import flash.geom.Rectangle;

import flash.events.Event;
import flash.events.MouseEvent;

import com.threerings.crowd.client.OccupantObserver;

import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.media.Tickable;
import com.threerings.util.Controller;
import com.threerings.util.Integer;
import com.threerings.util.Iterator;
import com.threerings.util.Log;
import com.threerings.util.Map;
import com.threerings.util.Maps;
import com.threerings.util.MathUtil;
import com.threerings.util.StringUtil;
import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.data.SceneLocation;
import com.threerings.whirled.spot.data.SpotCodes;
import com.threerings.whirled.spot.data.SpotSceneObject;
import com.threerings.whirled.util.WhirledContext;
import com.threerings.stage.data.StageSceneObject;
import com.threerings.stage.data.StageLocation;
import com.threerings.stage.util.StageContext;
import com.threerings.crowd.util.CrowdContext;
import com.threerings.presents.dobj.EntryUpdatedEvent;
import com.threerings.presents.dobj.SetAdapter;
import com.threerings.presents.dobj.SetListener;

import com.threerings.miso.util.MisoSceneMetrics;
import com.threerings.miso.util.MisoUtil;

import as3isolib.geom.Pt;

public class CrowdStageScenePanel extends StageScenePanel
    implements OccupantObserver, Tickable
{
    private var log :Log = Log.getLog(CrowdStageScenePanel);

    public function CrowdStageScenePanel (sCtx :StageContext, cCtx :CrowdContext, ctrl :Controller,
        metrics :MisoSceneMetrics)
    {
        super(sCtx, ctrl, metrics);

        _cCtx = cCtx;

        // compute our proximity radius (one visible screen diagonal);
        // also don't let it shrink when we're birded
        _proxrad = int(Math.max(_proxrad, Math.sqrt(_isoView.size.x*_isoView.size.x +
            _isoView.size.y*_isoView.size.y)));
        recomputeProximate();

        _vbounds = new Rectangle(0, 0, _isoView.size.x, _isoView.size.y);

        addEventListener(Event.ADDED_TO_STAGE, addedToStage);
        addEventListener(Event.REMOVED_FROM_STAGE, removedFromStage);
    }

    /**
     * Handles Event.ADDED_TO_STAGE.
     */
    protected function addedToStage (event :Event) :void
    {
        _ctx.getTicker().registerTickable(this);
    }

    /**
     * Handles Event.REMOVED_FROM_STAGE.
     */
    protected function removedFromStage (event :Event) :void
    {
        _ctx.getTicker().removeTickable(this);
    }

    override public function willEnterPlace (plObj :PlaceObject) :void
    {
        super.willEnterPlace(plObj);
        _scObj = StageSceneObject(plObj);
        plObj.addListener(_occupantListener);
        _cCtx.getOccupantDirector().addOccupantObserver(this);
        updateDisplayForScene();
    }

    public function tick (tickStamp :int) :void
    {
        _sprites.forEach(function (key :int, sprite :CharacterIsoSprite) :void {
            sprite.tick(tickStamp);
        });
    }

    protected function updateDisplayForScene () :void
    {
        // remove all old-scene sprites
        clearAllSprites();

        // fake an occupant entry for all scene occupants
        for each (var occupantInfo :OccupantInfo in _scObj.occupantInfo.toArray()) {
            occupantEntered(occupantInfo);
        }

        // start out with the view centered on our sprite so that we
        // resolve the appropriate scene blocks from the get go
        centerView();
    }

    protected function centerView () :void
    {
        if (_selfSprite != null) {
            var viewPt :Point =
                _isoView.isoToLocal(new Pt(_selfSprite.x, _selfSprite.y, _selfSprite.z));
            moveBy(new Point(viewPt.x - _isoView.width / 2, viewPt.y - _isoView.height / 2));
        }
    }

    /**
     * Clear all the sprites from the scene, even our own.
     */
    protected function clearAllSprites () :void
    {
        // clear the actual sprites
        //TODO clearSprites();

        // and clear all of our wacky lists
        _sprites.clear();
        _arriveLists.clear();
        _pendingRemoveSprites.clear();
    }

    override public function didLeavePlace (plObj :PlaceObject) :void
    {
        super.didLeavePlace(plObj);
        plObj.removeListener(_occupantListener);
        _cCtx.getOccupantDirector().removeOccupantObserver(this);
    }

    public function occupantEntered (info :OccupantInfo) :void
    {
        // we do mostly the same thing for entry and update
        occupantUpdated(null, info);
    }

    /**
     * Called when we arrive at our new location, all no longer proximate occupant's sprites are
     * booted and any newly proximate occupants have sprites created for them.
     */
    protected function  recomputeProximate () :void
    {
        if (_scObj == null) {
            return; // nothing doing!
        }

        for each (var sloc :SceneLocation in _scObj.occupantLocs) {
            var sprite :CharacterIsoSprite = getSprite(sloc.bodyOid);
            if (isProximateToLoc(sloc)) {
                if (sprite == null) {
                    // fake an update which will create their sprite
                    occupantMoved(sloc, sloc, false);
                }
            } else if (sprite != null) {
                removeSprite(sprite);
            }
        }
    }

    /**
     * Returns true if this occupant is proximate to ourself.
     */
    public function isProximate (info :OccupantInfo) :Boolean
    {
        var sloc :SceneLocation = locationForBody(info.getBodyOid());
        return (sloc != null && isProximateToLoc(sloc));
    }

        /**
     * Returns true if this user is close enough to us that we want to go to the trouble of
     * creating a sprite for them and all the business.
     */
    protected function isProximateToLoc (sceneLoc :SceneLocation) :Boolean
    {
        // make sure we know where we are
        var mySceneLoc :SceneLocation = locationForBody(myOid());
        if (mySceneLoc == null) {
            return false;
        }

        // if they're within one screen diagonal of us, they're proximate
        var myLoc :StageLocation = StageLocation(mySceneLoc.loc);
        var loc :StageLocation = StageLocation(sceneLoc.loc);
        var pos :Point = _isoView.isoToLocal(new Pt(MisoUtil.fullToTile(loc.x),
            MisoUtil.fullToTile(loc.y), 0));
        var myPos :Point = _isoView.isoToLocal(new Pt(MisoUtil.fullToTile(myLoc.x),
            MisoUtil.fullToTile(myLoc.y), 0));
        return int(MathUtil.distance(pos.x, pos.y, myPos.x, myPos.y)) < _proxrad;
    }

    public function occupantLeft (info :OccupantInfo) :void
    {
        // remove the occupant's sprite
        var oid :int = info.getBodyOid();
        if (oid == myOid()) {
            // never remove our own sprite!
            return;
        }

        var sprite :CharacterIsoSprite = getSprite(oid);
        if (sprite == null) {
            if (isProximate(info)) {
                log.warning("Proximate occupant left, but no sprite to remove " + info + ".");
            }
            return;
        }

        // if they are already moving, stick them in the pending remove
        // table so that we give them the boot when they arrive at their destination
        if (sprite.isMoving()) {
            _pendingRemoveSprites.put(oid, sprite);
            return;
        }

        // if the sprite is already at a portal, go ahead and remove them
        var sprLoc :Point = _isoView.localToIso(new Point(sprite.x, sprite.y));
        for (var iter :Iterator = _scene.getPortals(); iter.hasNext(); ) {
            var port :Portal = Portal(iter.next());
            var portLoc :StageLocation = StageLocation(port.loc);
            if (portLoc.x == sprLoc.x && portLoc.y == sprLoc.y) {
                removeSprite(sprite);
                return;
            }
        }

        // walk them to the default entrance; note: we have to put them in the pending table
        // *before* we call moveSprite because moveSprite might decide to immediately warp them to
        // their destination and call handleLocationArrived() directly which will expect them to be
        // in the pending table at that point
        _pendingRemoveSprites.put(oid, sprite);
        moveSpriteFromScene(sprite);
    }

    public function occupantUpdated (oldinfo :OccupantInfo, newinfo :OccupantInfo) :void
    {
        var bodyOid :int = newinfo.getBodyOid();
        var sloc :SceneLocation = locationForBody(bodyOid);
        if (sloc == null || !isProximateToLoc(sloc)) {
            return;
        }

        // look up their character sprite
        var sprite :CharacterIsoSprite = getSprite(newinfo.getBodyOid());
        if (sprite == null) {
            // ...creating one if necessary
            sprite = createAndAddSprite(OccupantInfo(newinfo), null);
            // if we couldn't create the sprite for some reason, we can stop now
            if (sprite == null) {
                return;
            }

        } else {
            // ...updating it if we've already got one
            updateOccupantSprite(sprite, newinfo);

            // make sure the sprite is no longer set to remove, in case it was
            _pendingRemoveSprites.remove(bodyOid);
        }

        // if this is the first time they entered the scene, fake a move to their starting position
        if (oldinfo == null) {
            // fake a "moved" to their starting position
            occupantMoved(sloc, sloc, false);
        }
    }

    /**
     * Performs any updates required to the occupant's character sprite given the updated occupant
     *  info.
     */
    protected function updateOccupantSprite (sprite :CharacterIsoSprite, info :OccupantInfo) :void
    {
        // Nothing by default.
    }

    /**
     * Called when an occupant has changed location. Updates clusters for this occupant and moves
     * their sprite.
     */
    protected function occupantMoved (oloc :SceneLocation, nloc :SceneLocation,
        reallyMoved :Boolean) :void
    {
        var sprite :CharacterIsoSprite = getSprite(nloc.bodyOid);

        // if their new location is not proximate, yank their sprite
        if (!isProximateToLoc(nloc)) {
            if (sprite != null) {
                // if they are visible, walk them to their new location
                if (_vbounds.intersects(sprite.screenBounds)) {
                    moveSpriteToLoc(sprite, nloc.loc);
                }
                // now queue them up for removal on arrival as appropriate
                if (sprite.isMoving()) {
                    _pendingRemoveSprites.put(nloc.bodyOid, sprite);
                } else {
                    removeSprite(sprite);
                }
            }
        }

        // now walk their sprite to its new location
        if (sprite == null) {
            var info :OccupantInfo = _cCtx.getOccupantDirector().getOccupantInfo(nloc.bodyOid);
            if (info != null) {
                sprite = createAndAddSprite(info, oloc);
            } else {
                log.warning("Can't update location of unknown '" + nloc + "' (" + who(nloc) + ").");
                return;
            }
        }

        moveSpriteToLoc(sprite, nloc.loc);

        /*TODO Portals
        // if self going to a portal, use exiting mode.
        // if not-moving someone else to a portal, don't adjust mode
        // all other cases, set normal scene mode
        if (isPortal(nloc.loc)) {
            if (isMe) {
                sprite.setExiting();
            } else if (moving) {
                sprite.setNormalSceneMode();
            }
        } else {
            sprite.setNormalSceneMode();
            }*/
    }

    /**
     * Moves the sprite to the referenced location.
     */
    protected function moveSpriteToLoc (sprite :CharacterIsoSprite, loc :Location) :void
    {
        var sloc :StageLocation = StageLocation(loc);
        // obtain the screen coordinates of the location

        // if they're not already standing where we want them...
        if (sprite.x != sloc.x || sprite.y != sloc.y || sprite.isMoving()) {
            // ...move them there; but note whether they were put on a path or just warped
            // immediately
            if (!moveSprite(sprite, sloc)) {
                return;
            }
        }

        // if they're already there or they were warped there we want to invoke the arrival
        // processing
        handleSpriteArrived(sprite, new Date().time);
    }

    /**
     * Move the specified sprite to the specified screen coordinates along a tile path.
     *
     * @return true if the sprite was warped immediately to that position, false if they were set
     * upon a path to it.
     */
    protected function moveSprite (sprite :CharacterIsoSprite, loc :StageLocation) :Boolean
    {
        var cx :int = sprite.x;
        var cy :int = sprite.y;
        var nx :int = MisoUtil.fullToTile(loc.x);
        var ny :int = MisoUtil.fullToTile(loc.y);

        // if this panel is not yet showing, warp the sprite directly
        if (!visible) {
            sprite.cancelMove();
            sprite.placeAtLoc(loc);
            return true;
        }

        // if the source and destination are both offscreen, warp the
        // sprite to where they are going
        var endsOff :Boolean = !_vbounds.contains(nx, ny);
        if (!_vbounds.intersects(sprite.screenBounds) && endsOff) {
            sprite.cancelMove();
            sprite.placeAtLoc(loc);
            return true;
        }

        // if we are obviously warping (i.e. we moved a great distance),
        // just blink into position and attempt to do so cleanly
        var dx :int = Math.abs(nx - (_vbounds.x + _vbounds.width/2));
        var dy :int = Math.abs(ny - (_vbounds.y + _vbounds.height/2));
        if ((dx > 2*_vbounds.width || dy > 2*_vbounds.height) &&
              (sprite == _selfSprite)) {
            log.info("Warp-a-saurus! " + StringUtil.toString(_vbounds) +
                " -> " + StringUtil.toCoordsString(loc.x, loc.y));
            sprite.cancelMove();
            sprite.placeAtLoc(loc);

            // delay repaint until we resolve our new blocks
            centerView();
            return true;
        }

        // if we made it this far, we can actually try to compute a path
        // for this sprite; if the end of the path is off-screen, allow "partial" paths
        var path :Object = null;//TODO Path TilePath = TilePath(getPath(yocs, nx, ny, endsOff));
        if (path == null) {
            /*TODO Path log.info("Unable to compute path from " +
                     StringUtil.toCoordsString(cx, cy) + " to " +
                     StringUtil.toCoordsString(loc.x, loc.y) + "; warping.");*/
            sprite.cancelMove();
            sprite.placeAtLoc(loc);
            return true;
        }

        // set the sprite on a path to their new location
        path.setVelocity(SPRITE_PATH_VELOCITY);
        sprite.move(path);

        return false;
    }

    /**
     * Animate a sprite leaving the scene.
     */
    protected function moveSpriteFromScene (sprite :CharacterIsoSprite) :void
    {
        moveSpriteToLoc(sprite, _scene.getDefaultEntrance().getLocation());
    }

    /**
     * A convenience method for getting our OID.
     */
    protected function myOid () :int
    {
        return _cCtx.getClient().getClientObject().getOid();
    }

    protected function who (loc :SceneLocation) :String
    {
        var oinfo :OccupantInfo = OccupantInfo(_scObj.occupantInfo.get(loc.bodyOid));
        return (oinfo == null) ? ("" + loc.bodyOid) : oinfo.username.toString();
    }

    protected function createAndAddSprite (info :OccupantInfo,
        loc :SceneLocation) :CharacterIsoSprite
    {
        var sprite :CharacterIsoSprite = createSprite(info);

        // stick them in the appropriate location
        if (loc == null) {
            loc = locationForBody(info.getBodyOid());
        }
        if (loc == null) {
            log.warning("Requested to create sprite for locationless occupant " + info + ".",
                new Error());
            return null;
        }

        // set their location and start tracking them
        sprite.placeAtLoc(StageLocation(loc.loc));

        _objScene.addChild(sprite);
        _sprites.put(info.getBodyOid(), sprite);

        if (sprite.getBodyOid() == myOid()) {
            _selfSprite = sprite;
        }

        return sprite;
    }

    override public function handleMousePressed (hobject :Object, event :MouseEvent) :Boolean
    {
        var viewPt :Point = _isoView.globalToLocal(new Point(event.stageX, event.stageY));
        var iso :Point = _isoView.localToIso(new Point(viewPt.x, viewPt.y));

        // Move ourselves to the spot clicked.
        handleLocationClicked(new StageLocation(MisoUtil.tileToFull(iso.x),
            MisoUtil.tileToFull(iso.y), 0));

        return true;
    }

    protected function displayFeedback (msg :String) :void
    {
        throw new Error("abstract");
    }

    public function handleLocationClicked (loc :StageLocation) :void
    {
        // normalize the click to the center of the clicked tile and check
        // to see if it is occupied
        var tx :int = MisoUtil.fullToTile(loc.x);
        var ty :int = MisoUtil.fullToTile(loc.y);
        loc.x = MisoUtil.tileToFull(tx, 2);
        loc.y = MisoUtil.tileToFull(ty, 2);

        if (tileOccupied(tx, ty, false)) {
            log.info("Not moving to occupied location " + loc + ".");
            displayFeedback(SpotCodes.INVALID_LOCATION);
            return;
        }

        // see if we can walk there and what orientation we'd face once we got there
        var orient :int = checkWalkToLoc(loc);
        if (orient == -1) {
            // if the location is on a passable tile, let the user know why we rejected their click
            if (canTraverse(null, tx, ty)) {
                displayFeedback("m.cant_get_there");
            }

            // TODO Path - remove this
            changeLocation(loc);

            return;
        }

        // adjust the orientation and head there
        loc.orient = orient;
        changeLocation(loc);
    }

    protected function changeLocation (loc :StageLocation) :void
    {
        throw new Error("abstract");
    }

    /**
     * Returns true if the specified tile coordinate is occupied by any sprites or if the
     * specified tile is inside an existing cluster.
     */
    protected function tileOccupied (tx :int, ty :int, countSelf :Boolean) :Boolean
    {
        var self :CharacterIsoSprite = getSprite(myOid());

        // if we or the scene object is gone, don't freak out
        if (self == null || _scObj == null) {
            return false;
        }

        // next see if any room occupant is standing here
        for each (var loc :SceneLocation in _scObj.occupantLocs.toArray()) {
            if (!countSelf && loc.bodyOid == myOid()) {
                continue;
            }
            var sloc :StageLocation = StageLocation(loc.loc);
            if (MisoUtil.fullToTile(sloc.x) == tx && MisoUtil.fullToTile(sloc.y) == ty) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks to see if we can compute a path to the specified location.
     *
     * @return the orientation the sprite would have upon arrival or -1 if
     * we cannot compute a path to the specified location.
     */
    protected function checkWalkToLoc (loc :StageLocation) :int
    {
        // obtain the screen coordinates of the location
        var sc :Point = _isoView.isoToLocal(new Pt(loc.x, loc.y, 0));
        var us :CharacterIsoSprite = getSprite(myOid());
        if (us != null) {
            //TODO Path var path :TilePath = TilePath(getPath(us, loc.x, loc.y, false));
            //TODO Path return (path == null) ? -1 : path.getFinalOrientation();
            return -1;
        } else {
            return -1;
        }
    }

    protected function createSprite (info :OccupantInfo) :CharacterIsoSprite
    {
        return new CharacterIsoSprite(info.getBodyOid(), _metrics, createCharacter(info));
    }

    protected function createCharacter (info :OccupantInfo) :DisplayObject
    {
        throw new Error("abstract");
    }

    protected function getSprite (bodyOid :int) :CharacterIsoSprite
    {
        return _sprites.get(bodyOid);
    }

    protected function removeSprite (sprite :CharacterIsoSprite) :void
    {
        _sprites.remove(sprite.getBodyOid());
    }

    protected function handleSpriteArrived (sprite :CharacterIsoSprite, tickStamp :int) :void
    {
        var oid :int = sprite.getBodyOid();

        var alist :ArrivalEntry = _arriveLists.remove(oid);

        try {
            // make sure we have a place object still
            if (_scObj == null) {
                return;
            }

            // if we were going to remove this sprite, do it now
            if (null != _pendingRemoveSprites.remove(oid)) {
                removeSprite(sprite);
                return;
            }

            // look up the location occupied by the user in question
            var loc :SceneLocation = locationForBody(oid);
            if (loc == null) {
                log.warning("Sprite completed path but no location found?",
                    "oid", oid, "sprite", sprite, "occlocs", _scObj.occupantLocs);
                return;
            }

            handleLocationArrived(sprite, loc);

            // notify any arrival listener
            if (alist != null && loc.loc.equals(alist.loc)) {
                alist.listener(sprite, loc.loc);
            }

        } finally {
            // keep track of the last time we came to rest
            if (sprite == _selfSprite) {
                _centerStamp = tickStamp;
            }
        }
    }

    /**
     * Returns the location id of the location at which the player with the specified body oid
     * resides.
     */
    protected function locationForBody (bodyOid :int) :SceneLocation
    {
        return (_scObj == null) ? null :
            SceneLocation(_scObj.occupantLocs.get(bodyOid));
    }

    /**
     * Called when a sprite has arrived at its location.
     */
    protected function handleLocationArrived (sprite :CharacterIsoSprite, loc :SceneLocation) :void
    {
        // orient them properly
        sprite.placeAtLoc(StageLocation(loc.loc));

        // if this is us, recompute our proximate sprite set
        if (sprite.getBodyOid() == myOid()) {
            recomputeProximate();
        }
    }

    /**
     * Registers a listener to be notified when the sprite associated with the specified body
     * arrives at the specified scene location. If the body is not currently on a path to that
     * location, the listener will be dropped. If the body arrives at any other location, the
     * request will be dropped. Only if the body completes their current path toward the specified
     * location will the listener be notified.
     *
     * @return true if the listener was added, false if there was no such sprite or they were not
     * moving.
     */
    public function addArrivalListener (bodyOid :int, loc :Location, listener :Function) :Boolean
    {
        var sprite :CharacterIsoSprite = getSprite(bodyOid);
        if (sprite != null && sprite.isMoving()) {
            _arriveLists.put(bodyOid, new ArrivalEntry(loc, listener));
            return true;
        }
        return false;
    }

    protected function occupantEntryUpdated (event :EntryUpdatedEvent) :void
    {
        if (event.getName() == SpotSceneObject.OCCUPANT_LOCS) {
            occupantMoved(SceneLocation(event.getOldEntry()),
                SceneLocation(event.getEntry()), true);
        }
    }

    protected var _occupantListener :SetListener = new SetAdapter(null, occupantEntryUpdated, null);

    protected var _cCtx :CrowdContext;

    /** Map bodyOid to all the sprites we're handling. */
    protected var _sprites :Map = Maps.newMapOf(int);

    /** Map bodyOid to all the sprites we're getting ready to remove. */
    protected var _pendingRemoveSprites :Map = Maps.newMapOf(int);

    /** Map bodyOid to listeners waiting for sprites to get somewhere. */
    protected var _arriveLists :Map = Maps.newMapOf(int);

    /** Timestamp for last time we moved, used for auto-recentering. */
    protected var _centerStamp :int;

    /** The sprite associated with our client. */
    protected var _selfSprite :CharacterIsoSprite;

    /** The pixel radius around our sprite which we consider sufficiently
     * proximate that we keep sprites around for occupants therein. */
    protected var _proxrad :int = 100;

    protected var _scObj :StageSceneObject;

    protected var _vbounds :Rectangle;

    /** Our sprite path velocity. */
    // if we had more frames, maybe we could only update their
    // location when their frame changed. Alas, we do not.
    protected static const SPRITE_PATH_VELOCITY :Number = 0.1;
}
}

import com.threerings.whirled.spot.data.Location;

class ArrivalEntry
{
    public var loc :Location;
    public var listener :Function;

    public function ArrivalEntry (loc :Location, listener :Function)
    {
        this.loc = loc;
        this.listener = listener;
    }
}