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

package com.threerings.stage.server;

import com.google.inject.Injector;

import com.threerings.resource.ResourceManager;

import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.bundle.BundledTileSetRepository;

import com.threerings.whirled.server.WhirledServer;

import com.threerings.stage.data.StageCodes;

import static com.threerings.stage.Log.log;

/**
 * Extends the Whirled server to provide services needed by the Stage system.
 */
public abstract class StageServer extends WhirledServer
{
    /** Configures dependencies needed by the Stage services. */
    public static class StageModule extends WhirledModule
    {
        @Override protected void configure () {
            super.configure();
            // nada (yet)
        }
    }

    /** A resource manager with which we can load resources in the same manner that the client does
     * (for resources that are used on both the server and client). */
    public ResourceManager rsrcmgr;

    /** Provides access to our tile repository. */
    public static TileManager tilemgr;

    @Override // from WhirledServer
    public void init (Injector injector)
        throws Exception
    {
        super.init(injector);

        // create the resource manager
        rsrcmgr = new ResourceManager("rsrc");
        rsrcmgr.initBundles(null, getResourceConfig(), null);

        // create our tile manager and repository
        tilemgr = new TileManager(null);
        tilemgr.setTileSetRepository(
            new BundledTileSetRepository(rsrcmgr, null, StageCodes.TILESET_RSRC_SET));

        log.info("Stage server initialized.");
    }

    /**
     * Returns the path to the configuration file for the resource manager that will be created for
     * use by the server. This is a resource path (meaning it should be relative to the resource
     * prefix (which is <code>rsrc</code>).
     */
    protected String getResourceConfig ()
    {
        return "config/resource/manager.properties";
    }
}
