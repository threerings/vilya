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

package com.threerings.stage.tools.viewer;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import com.samskivert.util.RandomUtil;

import com.samskivert.swing.Controller;

import com.threerings.media.sprite.PathObserver;
import com.threerings.media.sprite.Sprite;
import com.threerings.media.util.LineSegmentPath;
import com.threerings.media.util.Path;
import com.threerings.media.util.PerformanceMonitor;
import com.threerings.media.util.PerformanceObserver;

import com.threerings.cast.CharacterDescriptor;
import com.threerings.cast.CharacterManager;
import com.threerings.cast.CharacterSprite;
import com.threerings.cast.util.CastUtil;

import com.threerings.whirled.spot.data.Location;

import com.threerings.stage.client.StageScenePanel;
import com.threerings.stage.data.StageLocation;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.util.StageContext;

import static com.threerings.stage.Log.log;

public class ViewerScenePanel extends StageScenePanel
    implements PerformanceObserver, PathObserver
{
    /**
     * Construct the panel and initialize it with a context.
     */
    public ViewerScenePanel (StageContext ctx, CharacterManager charmgr)
    {
        super(ctx, new Controller() {
        });

        _charmgr = charmgr;

        // create the character descriptors
        _descUser = CastUtil.getRandomDescriptor(
            ctx.getComponentRepository(), "female", COMP_CLASSES,
            ctx.getColorPository(), COLOR_CLASSES);
        _descDecoy = CastUtil.getRandomDescriptor(
            ctx.getComponentRepository(), "male", COMP_CLASSES,
            ctx.getColorPository(), COLOR_CLASSES);

        // create the manipulable sprite
        _sprite = createSprite(_descUser);
        setFollowsPathable(_sprite, CENTER_ON_PATHABLE);

        PerformanceMonitor.register(this, "paint", 1000);
    }

    // documentation inherited
    public void setScene (StageScene scene, Location defloc)
    {
        setScene(scene);

        // move all of our sprites to the default entrance
        _defloc = (StageLocation) defloc;
        Point defpos = getScreenCoords(_defloc.x, _defloc.y);
        _sprite.setLocation(defpos.x, defpos.y);

        if (_decoys != null) {
            for (CharacterSprite decoy : _decoys) {
                decoy.setLocation(defpos.x, defpos.y);
            }
            createDecoyPaths();
        }
    }

    /**
     * Creates a new sprite.
     */
    protected CharacterSprite createSprite (CharacterDescriptor desc)
    {
        CharacterSprite s = _charmgr.getCharacter(desc);
        if (s != null) {
            // start 'em out standing
            s.setActionSequence(CharacterSprite.STANDING);
            if (_defloc != null) {
                Point defpos = getScreenCoords(_defloc.x, _defloc.y);
                s.setLocation(defpos.x, defpos.y);
            } else {
                s.setLocation(300, 300);
            }
            s.addSpriteObserver(this);
            _spritemgr.addSprite(s);
        }

        return s;
    }

    /**
     * Creates the decoy sprites.
     */
    public void createDecoys ()
    {
        int decoys = DEFAULT_NUM_DECOYS;
        try {
            decoys = Integer.parseInt(System.getProperty("decoys"));
        } catch (Exception e) {
        }

        if (_decoys == null) {
            _decoys = new CharacterSprite[decoys];
            for (int ii = 0; ii < decoys; ii++) {
                _decoys[ii] = createSprite(_descDecoy);
            }
        }

        // if we have a scene, create paths for our decoys
        createDecoyPaths();
    }

    /**
     * Creates paths for the decoy sprites.
     */
    protected void createDecoyPaths ()
    {
        if (_decoys != null) {
            for (CharacterSprite decoy : _decoys) {
                if (decoy != null) {
                    createRandomPath(decoy);
                }
            }
        }
    }

    @Override
    public void paint (Graphics g)
    {
        super.paint(g);
        PerformanceMonitor.tick(this, "paint");
    }

    // documentation inherited
    public void checkpoint (String name, int ticks)
    {
        log.info(name + " [ticks=" + ticks + "].");
    }

    @Override
    public void mousePressed (MouseEvent e)
    {
        int x = e.getX(), y = e.getY();

        switch (e.getModifiers()) {
        case MouseEvent.BUTTON1_MASK:
            createPath(_sprite, x, y);
            break;

        case MouseEvent.BUTTON2_MASK:
            if (_decoys != null) {
                for (CharacterSprite decoy : _decoys) {
                    createPath(decoy, x, y);
                }
            }
            break;
        }
    }

    /**
     * Assigns the sprite a path leading to the given destination
     * screen coordinates.  Returns whether a path was successfully
     * assigned.
     */
    protected boolean createPath (CharacterSprite s, int x, int y)
    {
        // get the path from here to there
        LineSegmentPath path = (LineSegmentPath)getPath(s, x, y, false);
        if (path == null) {
            s.cancelMove();
            return false;
        }

        // start the sprite moving along the path
        path.setVelocity(100f/1000f);
        s.move(path);
        return true;
    }

    /**
     * Assigns a new random path to the given sprite.
     */
    protected void createRandomPath (CharacterSprite s)
    {
        int x, y;
        do {
            x = _vbounds.x + RandomUtil.getInt(_vbounds.width);
            y = _vbounds.y + RandomUtil.getInt(_vbounds.height);
        } while (!createPath(s, x, y));
    }

    // documentation inherited from interface
    public void pathCancelled (Sprite sprite, Path path)
    {
        // nothing doing
    }

    // documentation inherited from interface
    public void pathCompleted (Sprite sprite, Path path, long when)
    {
        if (sprite != _sprite) {
            // move the decoy to a new random location
            createRandomPath((CharacterSprite)sprite);
        }
    }

    /** The number of decoy characters milling about. */
    protected static final int DEFAULT_NUM_DECOYS = 10;

    /** The current scene's default entrance. */
    protected StageLocation _defloc;

    /** Provides character sprite data. */
    protected CharacterManager _charmgr;

    /** The character descriptor for the user character. */
    protected CharacterDescriptor _descUser;

    /** The character descriptor for the decoy characters. */
    protected CharacterDescriptor _descDecoy;

    /** The sprite we're manipulating within the view. */
    protected CharacterSprite _sprite;

    /** The test sprites that meander about aimlessly. */
    protected CharacterSprite[] _decoys;

    /** Defines our various character component classes. */
    protected static final String[] COMP_CLASSES = {
        "legs", "feet", "hand_left", "hand_right", "torso",
        "head", "hair", "hat", "eyepatch" };

    /** Defines the colorization classes used in the character component images. */
    protected static final String[] COLOR_CLASSES = {
        "skin", "hair", "textile_p", "textile_s" };
}
