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

package com.threerings.stage.tools.editor;

import java.util.List;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import com.google.common.collect.Lists;

import com.samskivert.swing.DimmedIcon;

import com.threerings.media.tile.Tile;
import com.threerings.media.tile.TileIcon;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.UniformTileSet;

import static com.threerings.stage.Log.log;

public class EditorToolBarPanel extends JPanel implements ActionListener
{
    public EditorToolBarPanel (TileManager tilemgr, EditorModel model)
    {
        _model = model;

        // use of flowlayout positions the toolbar and floats properly
        setLayout(new FlowLayout(FlowLayout.LEFT));

        // get our toolbar icons
        UniformTileSet tbset = tilemgr.loadTileSet(ICONS_PATH, 40, 40);

        // create the toolbar
        JToolBar toolbar = new JToolBar();

        // add all of the toolbar buttons
        _buttons = Lists.newArrayList();
        for (int ii = 0; ii < EditorModel.NUM_ACTIONS; ii++) {
            // get the button icon images
            Tile tile = tbset.getTile(ii);
            if (tile != null) {
                String cmd = EditorModel.CMD_ACTIONS[ii];
                String tip = EditorModel.TIP_ACTIONS[ii];

                // create the button
                JButton b = addButton(toolbar, cmd, tip, new TileIcon(tile));

                // add it to the set of buttons we're managing
                _buttons.add(b);

            } else {
                log.warning("Unable to load toolbar icon " +
                            "[index=" + ii + "].");
            }
        }

        // default to the first button
        setSelectedButton(_buttons.get(0));

        // add the toolbar
        add(toolbar);
    }

    protected JButton addButton (JToolBar toolbar, String cmd, String tip,
                                 TileIcon icon)
    {
        // create the button and configure accordingly
        JButton button = new JButton(new DimmedIcon(icon));
        button.setSelectedIcon(icon);
        button.addActionListener(this);
        button.setActionCommand("tbar_" + cmd);
        button.setToolTipText(tip);

        // add the button to the toolbar
        toolbar.add(button);

        return button;
    }

    protected void setSelectedButton (JButton button)
    {
        for (int ii = 0; ii < _buttons.size(); ii++) {
            JButton tb = _buttons.get(ii);
            tb.setSelected(tb == button);
        }
    }

    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.startsWith("tbar")) {

            // select the chosen mode in the toolbar
            setSelectedButton((JButton)e.getSource());

            // update the active mode in the model, stripping the
            // "tbar_" prefix from the command string
            _model.setActionMode(cmd.substring(5));

        } else {
            log.warning("Unknown action command [cmd=" + cmd + "].");
        }
    }

    /** The buttons in the tool bar. */
    protected List<JButton> _buttons;

    /** The editor data model. */
    protected EditorModel _model;

    protected static final String ICONS_PATH =
        "media/stage/tools/editor/toolbar_icons.png";
}
