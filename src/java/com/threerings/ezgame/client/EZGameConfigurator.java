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

package com.threerings.ezgame.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;

import java.util.logging.Level;

import org.apache.commons.io.IOUtils;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.SimpleSlider;

import com.threerings.util.MessageBundle;
import com.threerings.util.MessageManager;

import com.threerings.parlor.game.client.SwingGameConfigurator;
import com.threerings.parlor.game.data.GameAI;

import com.threerings.ezgame.data.AIParameter;
import com.threerings.ezgame.data.ChoiceParameter;
import com.threerings.ezgame.data.EZGameConfig;
import com.threerings.ezgame.data.FileParameter;
import com.threerings.ezgame.data.GameDefinition;
import com.threerings.ezgame.data.Parameter;
import com.threerings.ezgame.data.RangeParameter;
import com.threerings.ezgame.data.ToggleParameter;
import com.threerings.ezgame.util.EZGameContext;

import static com.threerings.ezgame.Log.log;

/**
 * Handles the configuration of an EZ game. This works in conjunction with the {@link EZGameConfig}
 * to provide a generic mechanism for defining and obtaining game configuration settings.
 */
public class EZGameConfigurator extends SwingGameConfigurator
{
    // documentation inherited
    protected void gotGameConfig ()
    {
        super.gotGameConfig();

        EZGameContext ctx = (EZGameContext)_ctx;
        EZGameConfig config = (EZGameConfig)_config;
        GameDefinition gamedef = config.getGameDefinition();

        // create our parameter editors
        if (_editors == null) {
            MessageBundle msgs = ctx.getMessageManager().getBundle(config.getGameIdent());
            _editors = new ParamEditor[gamedef.params.length];
            for (int ii = 0; ii < _editors.length; ii++) {
                _editors[ii] = createEditor(ctx, msgs, gamedef.params[ii]);
                addControl(new JLabel(msgs.get(gamedef.params[ii].getLabel())),
                           (JComponent) _editors[ii]);
            }
        }

        // now read our parameters
        for (int ii = 0; ii < gamedef.params.length; ii++) {
            _editors[ii].readParameter(gamedef.params[ii], config);
        }
    }

    // documentation inherited
    protected void flushGameConfig ()
    {
        super.flushGameConfig();

        EZGameConfig config = (EZGameConfig)_config;
        GameDefinition gamedef = config.getGameDefinition();
        for (int ii = 0; ii < gamedef.params.length; ii++) {
            _editors[ii].writeParameter(gamedef.params[ii], config);
        }
    }

    protected ParamEditor createEditor (EZGameContext ctx, MessageBundle msgs, Parameter param)
    {
        if (param instanceof AIParameter) {
            return new AIEditor((AIParameter)param);
        } else if (param instanceof RangeParameter) {
            return new RangeEditor((RangeParameter)param);
        } else if (param instanceof ToggleParameter) {
            return new ToggleEditor((ToggleParameter)param);
        } else if (param instanceof ChoiceParameter) {
            return new ChoiceEditor(msgs, (ChoiceParameter)param);
        } else if (param instanceof FileParameter) {
            return new FileEditor(ctx, (FileParameter)param);
        } else {
            log.warning("Unknown parameter type! " + param + ".");
            return null;
        }
    }

    /** Provides a uniform interface to our UI components. */
    protected static interface ParamEditor
    {
        public void readParameter (Parameter param, EZGameConfig config);
        public void writeParameter (Parameter param, EZGameConfig config);
    }

    protected class RangeEditor extends SimpleSlider implements ParamEditor
    {
        public RangeEditor (RangeParameter param)
        {
            super(null, param.minimum, param.maximum, param.start);
        }

        public void readParameter (Parameter param, EZGameConfig config)
        {
            setValue((Integer)config.params.get(param.ident));
        }

        public void writeParameter (Parameter param, EZGameConfig config)
        {
            config.params.put(param.ident, getValue());
        }
    }

    protected class ToggleEditor extends JPanel implements ParamEditor
    {
        public ToggleEditor (ToggleParameter param)
        {
            setLayout(new HGroupLayout(HGroupLayout.NONE, HGroupLayout.LEFT));
            add(_box = new JCheckBox(null, null, param.start));
        }

        public void readParameter (Parameter param, EZGameConfig config)
        {
            _box.setSelected((Boolean)config.params.get(param.ident));
        }

        public void writeParameter (Parameter param, EZGameConfig config)
        {
            config.params.put(param.ident, _box.isSelected());
        }

        protected JCheckBox _box;
    }

    protected class ChoiceEditor extends JPanel implements ParamEditor
    {
        public ChoiceEditor (MessageBundle msgs, ChoiceParameter param)
        {
            setLayout(new HGroupLayout(HGroupLayout.NONE, HGroupLayout.LEFT));
            Choice[] choices = new Choice[param.choices.length];
            Choice selection = null;
            for (int ii = 0; ii < choices.length; ii++) {
                String choice = param.choices[ii];
                choices[ii] = new Choice();
                choices[ii].choice = choice;
                choices[ii].label = msgs.get(param.getChoiceLabel(ii));
                if (choice.equals(param.start)) {
                    selection = choices[ii];
                }
            }
            add(_combo = new JComboBox(choices));
            if (selection != null) {
                _combo.setSelectedItem(selection);
            }
        }

        public void readParameter (Parameter param, EZGameConfig config)
        {
            Choice selected = new Choice();
            selected.choice = (String)config.params.get(param.ident);
            _combo.setSelectedItem(selected);
        }

        public void writeParameter (Parameter param, EZGameConfig config)
        {
            config.params.put(param.ident, ((Choice)_combo.getSelectedItem()).choice);
        }

        protected JComboBox _combo;
    }

    protected static class Choice
    {
        public String choice;
        public String label;

        public String toString () {
            return label;
        }

        public boolean equals (Object other) {
            return (other instanceof Choice) && choice.equals(((Choice) other).choice);
        }
    }

    protected class FileEditor extends JPanel
        implements ParamEditor, ActionListener
    {
        public FileEditor (EZGameContext ctx, FileParameter param)
        {
            _ctx = ctx;
            _param = param;
            setLayout(new HGroupLayout(HGroupLayout.NONE, HGroupLayout.LEFT));
            String label = ctx.getMessageManager().getBundle(
                MessageManager.GLOBAL_BUNDLE).get("m.file_unset");
            add(_show = new JButton(label));
            _show.addActionListener(this);
        }

        public void readParameter (Parameter param, EZGameConfig config)
        {
            // nothing doing
        }

        public void writeParameter (Parameter param, EZGameConfig config)
        {
            if (_data != null) {
                config.params.put(param.ident, _data);
            }
        }

        public void actionPerformed (ActionEvent event)
        {
            if (event.getSource() == _show) {
                if (_chooser == null) {
                    _chooser = new JFileChooser();
                }

                int rv = _chooser.showOpenDialog(this);
                if (rv == JFileChooser.APPROVE_OPTION) {
                    File file = _chooser.getSelectedFile();

                    try {
                        if (_param.binary) {
                            _data = IOUtils.toByteArray(new FileInputStream(file));
                        } else {
                            _data = IOUtils.toString(new FileReader(file));
                        }
                        _show.setText(file.getName());

                    } catch (IOException ioe) {
                        String msg = MessageBundle.tcompose(
                            "m.file_read_failure", ioe.getMessage());
                        _ctx.getChatDirector().displayFeedback(null, msg);
                        log.warning("Failed to read '" + file + "': " + ioe.getMessage());
                    }
                }
            }
        }

        protected EZGameContext _ctx;
        protected FileParameter _param;
        protected JButton _show;
        protected JFileChooser _chooser;
        protected Object _data;
    }

    protected class AIEditor extends SimpleSlider implements ParamEditor
    {
        public AIEditor (AIParameter param)
        {
            super(null, 0, param.maximum, 0);
        }

        public void readParameter (Parameter param, EZGameConfig config)
        {
            setValue((config.ais == null) ? 0 : config.ais.length);
        }

        public void writeParameter (Parameter param, EZGameConfig config)
        {
            config.ais = new GameAI[getValue()];
            for (int ii = 0; ii < config.ais.length; ii++) {
                // TODO: allow specification of difficulty and personality
                config.ais[ii] = new GameAI(0, 0);
            }
        }
    }

    protected ParamEditor[] _editors;
}
