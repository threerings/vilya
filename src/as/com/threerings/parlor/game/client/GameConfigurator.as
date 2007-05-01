//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.game.client {

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * Provides the base from which interfaces can be built to configure games prior to starting
 * them. Derived classes would extend the base configurator adding interface elements and wiring
 * them up properly to allow the user to configure an instance of their game.
 *
 * <p> Clients that use the game configurator will want to instantiate one based on the class
 * returned from the {@link GameConfig} and then initialize it with a call to {@link #init}.
 */
public /*abstract*/ class GameConfigurator
{
    /**
     * Initializes this game configurator, creates its user interface elements and prepares it for
     * display.
     */
    public function init (ctx :ParlorContext) :void
    {
        // save this for later
        _ctx = ctx;

        // create our interface elements
        createConfigInterface();
    }

    /**
     * The default implementation creates nothing.
     */
    protected function createConfigInterface () :void
    {
    }

    /**
     * Provides this configurator with its configuration. It should set up all of its user
     * interface elements to reflect the configuration.
     */
    public function setGameConfig (config :GameConfig) :void
    {
        _config = config;
        // set up the user interface
        gotGameConfig();
    }

    /**
     * Derived classes will likely want to override this method and configure their user interface
     * elements accordingly.
     */
    protected function gotGameConfig () :void
    {
    }

    /**
     * Obtains a configured game configuration.
     */
    public function getGameConfig () :GameConfig
    {
        // flush our changes to the config object
        flushGameConfig();
        return _config;
    }

    /**
     * Derived classes will want to override this method, flushing values from the user interface
     * to the game config object so that it is properly configured prior to being returned to the
     * {@link #getGameConfig} caller.
     */
    protected function flushGameConfig () :void
    {
    }

    /** Provides access to client services. */
    protected var _ctx :ParlorContext;

    /** Our game configuration. */
    protected var _config :GameConfig;
}
}
