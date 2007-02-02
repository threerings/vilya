//
// $Id$
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2005 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.parlor.client {

import mx.binding.utils.BindingUtils;

import mx.containers.Grid;

import mx.controls.CheckBox;
import mx.controls.HSlider;
import mx.controls.Label;

import com.threerings.flex.GridUtil;

import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.util.ParlorContext;

import com.threerings.parlor.game.client.FlexGameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides a default implementation of a TableConfigurator for
 * a Swing interface.
 */
public class DefaultFlexTableConfigurator extends TableConfigurator
{
    /**
     * Create a TableConfigurator that allows for the specified configuration
     * parameters.
     */
    public function DefaultFlexTableConfigurator (
            desiredPlayers :int, minPlayers :int = -1, maxPlayers :int = -1,
            allowPrivate :Boolean = false)
    {
        if (minPlayers < 0) {
            minPlayers = desiredPlayers;
        }
        if (maxPlayers < 0) {
            maxPlayers = desiredPlayers;
        }

        _config.minimumPlayerCount = minPlayers;

        // create a slider for players, if applicable
        if (minPlayers != maxPlayers) {
            _playerSlider = new HSlider();
            _playerSlider.value = desiredPlayers;
            _playerSlider.minimum = minPlayers;
            _playerSlider.maximum = maxPlayers;
            _playerSlider.liveDragging = true;
            _playerSlider.snapInterval = 1;
            _playerSlider.showDataTip = false;

        } else {
            _config.desiredPlayerCount = desiredPlayers;
        }

        // create up the checkbox for private games, if applicable
        if (allowPrivate) {
            _privateCheck = new CheckBox();
        }
    }

    // documentation inherited
    override protected function createConfigInterface () :void
    {
        super.createConfigInterface();

        var gconf :FlexGameConfigurator =
            (_gameConfigurator as FlexGameConfigurator);

        if (_playerSlider != null) {
            // TODO: proper translation
            var playerLabel :Label = new Label();
            playerLabel.text = "Players:";

            var countLabel :Label = new Label();
            BindingUtils.bindProperty(countLabel, "text",
                _playerSlider, "value");

            var grid :Grid = new Grid();
            GridUtil.addRow(grid, countLabel, _playerSlider);

            gconf.addControl(playerLabel, grid);
        }

        if (_privateCheck != null) {
            // TODO: proper translation
            var privateLabel :Label = new Label();
            privateLabel.text = "Private:";
            gconf.addControl(privateLabel, _privateCheck);
        }
    }

    // documentation inherited
    override public function isEmpty () :Boolean
    {
        return (_playerSlider == null) && (_privateCheck == null);
    }

    // documentation inherited
    override protected function flushTableConfig() :void
    {
        super.flushTableConfig();

        if (_playerSlider != null) {
            _config.desiredPlayerCount = _playerSlider.value;
        }
        if (_privateCheck != null) {
            _config.privateTable = _privateCheck.selected;
        }
    }

    /** A slider for configuring the number of players at the table. */
    protected var _playerSlider :HSlider;

    /** A checkbox to allow the table creator to specify if the table is
     * private. */
    protected var _privateCheck :CheckBox;
}
}
