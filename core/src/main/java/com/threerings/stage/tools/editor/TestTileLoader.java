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

package com.threerings.stage.tools.editor;

import java.util.Iterator;
import java.util.Map;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;

import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;

import com.google.common.collect.Maps;

import com.samskivert.util.HashIntMap;

import com.threerings.media.tile.ImageProvider;
import com.threerings.media.tile.SimpleCachingImageProvider;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileSetIDBroker;
import com.threerings.media.tile.tools.xml.ObjectTileSetRuleSet;
import com.threerings.media.tile.tools.xml.XMLTileSetParser;

import com.threerings.miso.tile.tools.xml.BaseTileSetRuleSet;

import static com.threerings.stage.Log.log;

/**
 * The TestTileLoader handles test tiles. Test tiles are tiles that an
 * artist can load in on-the-fly to see how things look in the scene editor.
 */
public class TestTileLoader implements TileSetIDBroker
{
    /**
     * Construct the TestTileLoader.
     */
    public TestTileLoader ()
    {
        // our xml parser
        _parser = new XMLTileSetParser();
        // add some rulesets
        _parser.addRuleSet("bundle/base", new BaseTileSetRuleSet());
        _parser.addRuleSet("bundle/object", new ObjectTileSetRuleSet());

        // we used to parse fringes, but we don't anymore
        //_parser.addRuleSet("bundle/fringe", new SwissArmyTileSetRuleSet());
    }

    /**
     * Check the specified directory and all its subdirectories for xml files.
     * Each directory should contain at most one xml file, each xml file
     * should specify at most one tileset. That tileset specification
     * will be used to create tilesets for all the .png files in the same
     * directory.
     *
     * @return a HashIntMap containing a {@code TileSetId -> TileSet} mapping for
     * all the tilesets we create.
     */
    public HashIntMap<TileSet> loadTestTiles ()
    {
        String directory = EditorConfig.getTestTileDirectory();
        HashIntMap<TileSet> map = new HashIntMap<TileSet>();

        // recurse test directory, making a tileset from the xml file inside
        // and cloning it for each image we find in there.
        File testdir = new File(directory);
        // make sure it's a directory
        if (!testdir.isDirectory()) {
            log.warning("Test tileset directory is not actually a directory: " +
                directory);
            return map;
        }

        // recursively load all the test tiles
        loadTestTilesFromDir(testdir, map);

        return map;
    }

    /**
     * Load xml tile sets from a directory.
     */
    protected void loadTestTilesFromDir (File directory, HashIntMap<TileSet> sets)
    {
        // first recurse
        File[] subdirs = directory.listFiles(new FileFilter() {
            public boolean accept (File f) {
                return f.isDirectory();
            }
        });
        for (File subdir : subdirs) {
            loadTestTilesFromDir(subdir, sets);
        }

        // now look for the xml file
        String[] xml = directory.list(new FilenameFilter() {
            public boolean accept (File dir, String name) {
                return name.endsWith(".xml");
            }
        });

        for (String element : xml) {
            File xmlfile = new File(directory, element);

            Map<String, TileSet> tiles = Maps.newHashMap();
            try {
                _parser.loadTileSets(xmlfile, tiles);
            } catch (IOException ioe) {
                log.warning("Error while parsing " + xmlfile.getPath(), ioe);
                continue;
            }

            Iterator<TileSet> iter = tiles.values().iterator();
            while (iter.hasNext()) {
                TileSet ts = iter.next();
                String path = new File(directory, ts.getImagePath()).getPath();

                // before we insert, make sure we can load the image
                if (null != _improv.getTileSetImage(path, null)) {
                    ts.setImageProvider(_improv);
                    ts.setImagePath(path);
                    sets.put(getTileSetID(path), ts);
                }
            }
        }
    }

    /**
     * Generate unique and completely fake tileset IDs that will be stable
     * even after a reload of test tiles.
     */
    public int getTileSetID (String tileSetPath)
    {
        Integer id = _idmap.get(tileSetPath);
        if (null == id) {
            id = Integer.valueOf(_fakeID--);
            _idmap.put(tileSetPath, id);
        }
        return id.intValue();
    }

    // documentation inherited
    public boolean tileSetMapped (String tilesetPath)
    {
        return _idmap.containsKey(tilesetPath);
    }

    /**
     * Since we're just testing, we don't save these crazy IDs.
     */
    public void commit ()
    {
        // this method does nothing. perhaps it should be called "committee".
    }

    /** The value of the next fakeID we'll hand out. */
    protected int _fakeID = Short.MAX_VALUE;

    /** A mapping of pathname -> tileset id. */
    protected Map<String, Integer> _idmap = Maps.newHashMap();

    /** Our xml parser. */
    protected XMLTileSetParser _parser;

    /** Our image provider. */
    protected ImageProvider _improv = new SimpleCachingImageProvider() {
        @Override
        protected BufferedImage loadImage (String path)
            throws IOException {
            return ImageIO.read(new File(path));
        }
   };
}
