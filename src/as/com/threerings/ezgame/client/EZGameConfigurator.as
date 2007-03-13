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

import com.threerings.util.StringUtil;

import com.threerings.flex.LabeledSlider;

import com.threerings.parlor.game.client.FlexGameConfigurator;

import com.threerings.ezgame.data.EZGameConfig;

/**
 * Adds custom configuration of options specified in XML.
 */
public class EZGameConfigurator extends FlexGameConfigurator
{
    /**
     * Set a String of configData, which is hopefully XML formatted with
     * some game configuration options.
     */
    public function setXMLConfig (configData :String) :void
    {
        var log :Log = Log.getLog(this);

        var config :XML = XML(configData);
        for each (var param :XML in config..params.children()) {
            if (StringUtil.isBlank(param.@ident)) {
                log.warning("Bad configuration option: no 'ident' in " +
                    param + ", ignoring.");
                continue;
            }
            switch (param.localName().toLowerCase()) {
            case "range":
                addRangeOption(param, log);
                break;

            case "choice":
                addChoiceOption(param, log);
                break;

            case "toggle":
                addToggleOption(param, log);
                break;

            default:
                log.warning("Unknown XML configuration option: " + param +
                    ", ignoring.");
                break;
            }
        }
    }

    /**
     * Add a control that came from parsing our custom option XML.
     */
    protected function addXMLControl (ident :String, control :UIComponent) :void
    {
        var name :String = ident.replace("_", " ");
        var lbl :Label = new Label();
        lbl.text = name + ":";
        addControl(lbl, control);

        _customConfigs.push(ident, control);
    }

    /**
     * Adds a 'range' option specified in XML
     */
    protected function addRangeOption (spec :XML, log :Log) :void
    {
        var min :Number = Number(spec.@minimum);
        var max :Number = Number(spec.@maximum);
        var start :Number = Number(spec.@start);
        if (isNaN(min) || isNaN(max) || isNaN(start)) {
            log.warning("Unable to parse range specification. " +
                "Required numeric values: minimum, maximum, start " +
                "[xml=" + spec + "].");
            return;
        }

        var slider :HSlider = new HSlider();
        slider.minimum = min;
        slider.maximum = max;
        slider.value = start;
        slider.liveDragging = true;
        slider.snapInterval = 1;

        addXMLControl(spec.@ident, new LabeledSlider(slider));
    }

    /**
     * Adds a 'choice' option specified in XML
     */
    protected function addChoiceOption (spec :XML, log :Log) :void
    {
        if (spec.@choices.length == 0 || spec.@start.length == 0) {
            log.warning("Unable to parse choice specification. " +
                "Required 'choices' or 'start' is missing. " +
                "[xml=" + spec + "].");
            return;
        }

        var choiceString :String = String(spec.@choices);
        var start :String = String(spec.@start);

        var choices :Array = choiceString.split(",");
        var startDex :int = choices.indexOf(start);
        if (startDex == -1) {
            log.warning("Choice start value does not appear in list of choices "+
                "[xml=" + spec + "].");
            return;
        }

        var box :ComboBox = new ComboBox();
        box.dataProvider = choices;
        box.selectedIndex = startDex;

        addXMLControl(spec.@ident, box);
    }

    /**
     * Adds a 'toggle' option specified in XML
     */
    protected function addToggleOption (spec :XML, log :Log) :void
    {
        if (spec.@start.length == 0) {
            log.warning("Unable to parse toggle specification. " +
                "Required 'start' is missing. " +
                "[xml=" + spec + "].");
            return;
        }

        var startStr :String = String(spec.@start).toLowerCase();

        var box :CheckBox = new CheckBox();
        box.selected = ("true" == startStr);

        addXMLControl(spec.@ident, box);
    }

    override protected function flushGameConfig () :void
    {
        super.flushGameConfig();
        
        // if there were any custom XML configs, flush those as well.
        if (_customConfigs.length > 0) {
            var options :Object = {};

            for (var ii :int = 0; ii < _customConfigs.length; ii += 2) {
                var ident :String = String(_customConfigs[ii]);
                var control :UIComponent = (_customConfigs[ii + 1] as UIComponent);
                if (control is LabeledSlider) {
                    options[ident] = (control as LabeledSlider).slider.value;

                } else if (control is CheckBox) {
                    options[ident] = (control as CheckBox).selected;

                } else if (control is ComboBox) {
                    options[ident] = (control as ComboBox).value;

                } else {
                    Log.getLog(this).warning("Unknow custom config type " + control);
                }
            }

            (_config as EZGameConfig).customConfig = options;
        }
    }

    /** Contains pairs of identString, control, identString, control.. */
    protected var _customConfigs :Array = [];
}
}
