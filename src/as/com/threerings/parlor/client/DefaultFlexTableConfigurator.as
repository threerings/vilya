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

package com.threerings.parlor.client {

import mx.containers.Grid;

import mx.controls.CheckBox;
import mx.controls.HSlider;
import mx.controls.Label;

import com.threerings.flex.GridUtil;
import com.threerings.flex.LabeledSlider;

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
            allowWatchable :Boolean = true, playersXlate :String = "Players: ", 
            watchableXlate :String = "Watchable: ", privateXlate :String = "Private: ")
    {
        var partyGame :Boolean = minPlayers < 0 && maxPlayers < 0;

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

        } else {
            _config.desiredPlayerCount = desiredPlayers;
        }

        // create up the checkbox for private games, if applicable
        if (partyGame) {
            _privateCheck = new CheckBox();
            _privateCheck.selected = false;
        } else if (allowWatchable) {
            _watchableCheck = new CheckBox();
            // default to watchable, if the game allows it.
            _watchableCheck.selected = true;
        }

        _playersXlate = playersXlate;
        _watchableXlate = watchableXlate;
        _privateXlate = privateXlate;
    }

    // documentation inherited
    override protected function createConfigInterface () :void
    {
        super.createConfigInterface();

        var gconf :FlexGameConfigurator =
            (_gameConfigurator as FlexGameConfigurator);

        if (_playerSlider != null) {
            var playerLabel :Label = new Label();
            playerLabel.text = _playersXlate;
            playerLabel.styleName = "lobbyLabel";

            gconf.addControl(playerLabel, new LabeledSlider(_playerSlider));
        }

        if (_watchableCheck != null) {
            var watchableLabel :Label = new Label();
            watchableLabel.text = _watchableXlate;
            watchableLabel.styleName = "lobbyLabel";
            gconf.addControl(watchableLabel, _watchableCheck);
        } else if (_privateCheck != null) {
            var privateLabel :Label = new Label();
            privateLabel.text = _privateXlate;
            privateLabel.styleName = "lobbyLabel";
            gconf.addControl(privateLabel, _privateCheck);
        }
    }

    // documentation inherited
    override public function isEmpty () :Boolean
    {
        return (_playerSlider == null) && (_watchableCheck == null);
    }

    // documentation inherited
    override protected function flushTableConfig() :void
    {
        super.flushTableConfig();

        if (_playerSlider != null) {
            _config.desiredPlayerCount = _playerSlider.value;
        }
        // TODO - it is wacky for the TableConfig.privateTable to mean two different things.  
        // It should be extended to have separate privateTable and watchableTable options.
        if (_watchableCheck != null) {
            _config.privateTable = !_watchableCheck.selected;
        } else if (_privateCheck != null) {
            _config.privateTable = _privateCheck.selected;
        }
    }

    /** A slider for configuring the number of players at the table. */
    protected var _playerSlider :HSlider;

    /** A checkbox to allow the table creator to specify if the table is watchable */
    protected var _watchableCheck :CheckBox;

    /** A checkbox to allow the table creator to specifiy if the table is private */
    protected var _privateCheck :CheckBox;

    /** Translation strings passed in by the caller */
    protected var _playersXlate :String;
    protected var _watchableXlate :String;
    protected var _privateXlate :String;
}
}
