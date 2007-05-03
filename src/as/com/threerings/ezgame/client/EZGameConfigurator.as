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

package com.threerings.ezgame.client {

import mx.core.UIComponent;
import mx.controls.CheckBox;
import mx.controls.ComboBox;
import mx.controls.HSlider;
import mx.controls.Label;

import com.threerings.util.Integer;
import com.threerings.util.StreamableHashMap;
import com.threerings.util.StringUtil;

import com.threerings.flex.LabeledSlider;

import com.threerings.parlor.game.client.FlexGameConfigurator;

import com.threerings.ezgame.data.ChoiceParameter;
import com.threerings.ezgame.data.EZGameConfig;
import com.threerings.ezgame.data.Parameter;
import com.threerings.ezgame.data.RangeParameter;
import com.threerings.ezgame.data.ToggleParameter;

/**
 * Adds custom configuration of options specified in XML.
 */
public class EZGameConfigurator extends FlexGameConfigurator
{
    // from GameConfigurator
    override protected function gotGameConfig () :void
    {
        super.gotGameConfig();

        var params :Array = (_config as EZGameConfig).getGameDefinition().params;
        if (params == null) {
            return;
        }

        for each (var param :Parameter in params) {
            if (param is RangeParameter) {
                var range :RangeParameter = (param as RangeParameter);
                var slider :HSlider = new HSlider();
                slider.minimum = range.minimum;
                slider.maximum = range.maximum;
                slider.value = range.start;
                slider.liveDragging = true;
                slider.snapInterval = 1;
                addLabeledControl(param, new LabeledSlider(slider));

            } else if (param is ChoiceParameter) {
                var choice :ChoiceParameter = (param as ChoiceParameter);
                var startDex :int = choice.choices.indexOf(choice.start);
                if (startDex == -1) {
                    Log.getLog(this).warning(
                        "Start value does not appear in list of choices [param=" + choice + "].");
                } else {
                    var combo :ComboBox = new ComboBox();
                    combo.dataProvider = choice.choices;
                    combo.selectedIndex = startDex;
                    addLabeledControl(param, combo);
                }

            } else if (param is ToggleParameter) {
                var check :CheckBox = new CheckBox();
                check.selected = (param as ToggleParameter).start;
                addLabeledControl(param, check);

            } else {
                Log.getLog(this).warning("Unknown parameter in config [param=" + param + "].");
            }
        }
    }

    override protected function flushGameConfig () :void
    {
        super.flushGameConfig();

        // if there were any custom XML configs, flush those as well.
        if (_customConfigs.length > 0) {
            var params :StreamableHashMap = new StreamableHashMap();

            for (var ii :int = 0; ii < _customConfigs.length; ii += 2) {
                var ident :String = String(_customConfigs[ii]);
                var control :UIComponent = (_customConfigs[ii + 1] as UIComponent);
                if (control is LabeledSlider) {
                    params.put(ident, (control as LabeledSlider).slider.value);

                } else if (control is CheckBox) {
                    params.put(ident, (control as CheckBox).selected);

                } else if (control is ComboBox) {
                    params.put(ident, (control as ComboBox).value);

                } else {
                    Log.getLog(this).warning("Unknow custom config type " + control);
                }
            }

            (_config as EZGameConfig).params = params;
        }
    }

    /**
     * Add a control that came from parsing our custom option XML.
     */
    protected function addLabeledControl (param :Parameter, control :UIComponent) :void
    {
        if (StringUtil.isBlank(param.name)) {
            param.name = param.ident;
        }

        var lbl :Label = new Label();
        lbl.text = param.name + ":";
        lbl.styleName = "lobbyLabel";
        lbl.toolTip = param.tip;
        control.toolTip = param.tip;

        addControl(lbl, control);
        _customConfigs.push(param.ident, control);
    }

    /** Contains pairs of identString, control, identString, control.. */
    protected var _customConfigs :Array = [];
}
}
