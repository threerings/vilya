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

package com.threerings.parlor.client {

import mx.controls.CheckBox;
import mx.controls.ComboBox;
import mx.controls.Label;

import com.threerings.parlor.data.RangeParameter;
import com.threerings.parlor.data.TableConfig;
import com.threerings.parlor.data.ToggleParameter;
import com.threerings.parlor.game.client.FlexGameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

/**
 * Provides a default implementation of a TableConfigurator for
 * a Swing interface.
 */
public class DefaultFlexTableConfigurator extends TableConfigurator
{
    /**
     * Create a TableConfigurator that allows for the specified configuration parameters.
     */
    public function DefaultFlexTableConfigurator (
        players :RangeParameter, watchable :ToggleParameter = null, prvate :ToggleParameter = null)
    {
        _playersParam = players;
        _watchableParam = watchable;
        _privateParam = prvate;

        _config.minimumPlayerCount = players.minimum;

        // create a slider for players, if applicable
        if (players.minimum != players.maximum) {
            _playersBox = new ComboBox();
            var values :Array = [];
            var startDex :int = 0;
            for (var ii :int = players.minimum; ii <= players.maximum; ii++) {
                if (ii == players.start) {
                    startDex = ii;
                }
                values.push(ii);
            }
            _playersBox.dataProvider = values;
            _playersBox.selectedIndex = startDex;

        } else {
            _config.desiredPlayerCount = players.start;
        }

        if (watchable != null) {
            _watchableCheck = new CheckBox();
            _watchableCheck.selected = watchable.start;
        }

        if (prvate != null) {
            _privateCheck = new CheckBox();
            _privateCheck.selected = prvate.start;
        }
    }

    // documentation inherited
    override protected function createConfigInterface () :void
    {
        super.createConfigInterface();

        var gconf :FlexGameConfigurator = (_gameConfigurator as FlexGameConfigurator);

        if (_playersBox != null) {
            var playerLabel :Label = new Label();
            playerLabel.text = _playersParam.name;
            playerLabel.toolTip = _playersParam.tip;
            playerLabel.styleName = "lobbyLabel";
            gconf.addControl(playerLabel, _playersBox);
        }

        if (_watchableCheck != null) {
            var watchableLabel :Label = new Label();
            watchableLabel.text = _watchableParam.name;
            watchableLabel.toolTip = _watchableParam.tip;
            watchableLabel.styleName = "lobbyLabel";
            gconf.addControl(watchableLabel, _watchableCheck);
        }

        if (_privateCheck != null) {
            var privateLabel :Label = new Label();
            privateLabel.text = _privateParam.name;
            privateLabel.toolTip = _privateParam.tip;
            privateLabel.styleName = "lobbyLabel";
            gconf.addControl(privateLabel, _privateCheck);
        }
    }

    // documentation inherited
    override public function isEmpty () :Boolean
    {
        return (_playersBox == null) && (_watchableCheck == null) && (_privateCheck == null);
    }

    // documentation inherited
    override protected function flushTableConfig () :void
    {
        super.flushTableConfig();

        if (_playersBox != null) {
            _config.desiredPlayerCount = int(_playersBox.value);
        }

        // TODO: it is wacky for the TableConfig.privateTable to mean two different things; it
        // should be extended to have separate privateTable and watchableTable options.
        if (_watchableCheck != null) {
            _config.privateTable = !_watchableCheck.selected;
        }
        if (_privateCheck != null) {
            _config.privateTable = _privateCheck.selected;
        }
    }

    /** A component for configuring the number of players at the table. */
    protected var _playersBox :ComboBox;

    /** A checkbox to allow the table creator to specify if the table is watchable */
    protected var _watchableCheck :CheckBox;

    /** A checkbox to allow the table creator to specifiy if the table is private */
    protected var _privateCheck :CheckBox;

    /** Configuration passed in by the caller */
    protected var _playersParam :RangeParameter;
    protected var _watchableParam :ToggleParameter;
    protected var _privateParam :ToggleParameter;
}
}
