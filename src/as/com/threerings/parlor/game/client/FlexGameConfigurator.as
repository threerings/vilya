//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.game.client {

import mx.core.Container;
import mx.core.UIComponent;

import mx.containers.Grid;
import mx.containers.GridItem;
import mx.containers.GridRow;

import com.threerings.flex.GridUtil;

import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * Provides the base from which interfaces can be built to configure games prior to starting them.
 * Derived classes would extend the base configurator adding interface elements and wiring them up
 * properly to allow the user to configure an instance of their game.
 *
 * <p> Clients that use the game configurator will want to instantiate one based on the class
 * returned from the {@link GameConfig} and then initialize it with a call to {@link #init}.
 */
public /*abstract*/ class FlexGameConfigurator extends GameConfigurator
{
    /**
     * Configures the number of columns to use when laying out our controls. This must be called
     * before any calls to {@link #addControl}.
     */
    public function setColumns (columns :int) :void
    {
        _columns = columns;
    }

    /**
     * Get the Container that contains the UI elements for this configurator.
     */
    public function getContainer () :Container
    {
        return _grid;
    }

    /**
     * Add a control to the interface. This should be the standard way that configurator controls
     * are added, but note also that external entities may add their own controls that are related
     * to the game, but do not directly alter the game config, so that all the controls are added
     * in a uniform manner and are well aligned.
     */
    public function addControl (label :UIComponent, control :UIComponent,
                                verticalAlign :String = "middle") :void
    {
        if (_gridRow == null) {
            _gridRow = new GridRow();
            _grid.addChild(_gridRow);
        }

        var item :GridItem = GridUtil.addToRow(_gridRow, label);
        item.setStyle("verticalAlign", verticalAlign);
        if (_gridRow.numChildren > 1) {
            item.setStyle("paddingLeft", 15);
        }
        item = GridUtil.addToRow(_gridRow, control);
        item.setStyle("verticalAlign", verticalAlign);

        if (_gridRow.numChildren == _columns * 2) {
            _gridRow = null;
        }
    }

    /** The grid on which the config options are placed. */
    protected var _grid :Grid = new Grid();

    /** The current row to which we're adding controls. */
    protected var _gridRow :GridRow;

    /** The number of columns in which to lay out our configuration. */
    protected var _columns :int = 2;
}
}
