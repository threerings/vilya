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

import mx.core.Container;
import mx.core.UIComponent;

import mx.containers.HBox;
import mx.containers.Tile;

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * Provides the base from which interfaces can be built to configure games
 * prior to starting them. Derived classes would extend the base
 * configurator adding interface elements and wiring them up properly to
 * allow the user to configure an instance of their game.
 *
 * <p> Clients that use the game configurator will want to instantiate one
 * based on the class returned from the {@link GameConfig} and then
 * initialize it with a call to {@link #init}.
 */
public /*abstract*/ class FlexGameConfigurator extends GameConfigurator
{
    /**
     * Get the Container that contains the UI elements for this configurator.
     */
    public function getContainer () :Container
    {
        // mimic the style of the seats grid for consistency
        _tile.width = 375;
        _tile.styleName = "seatsGrid";
        return _tile;
    }

    /**
     * Add a control to the interface. This should be the standard way
     * that configurator controls are added, but note also that external
     * entities may add their own controls that are related to the game,
     * but do not directly alter the game config, so that all the controls
     * are added in a uniform manner and are well aligned.
     */
    public function addControl (
            label :UIComponent, control :UIComponent) :void
    {
        var item :HBox = new HBox();
        item.width = 175;
        item.setStyle("horizontalGap", 5);
        label.width = 70;
        item.addChild(label);
        control.width = 100;
        item.addChild(control);
        _tile.addChild(item);
    }

    /** The grid on which the config options are placed. */
    protected var _tile :Tile = new Tile();
}
}
