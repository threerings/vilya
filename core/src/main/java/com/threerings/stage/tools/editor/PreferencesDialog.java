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

import java.io.File;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.stage.tools.editor.util.EditorDialogUtil;

import static com.threerings.stage.Log.log;

/**
 * A dialog for editing preferences.
 */
public class PreferencesDialog extends JInternalFrame
    implements ActionListener
{
    /**
     * Creates a preferences dialog.
     */
    public PreferencesDialog ()
    {
        super("Editor Preferences", true);

        // set up a layout manager for the panel
        JPanel top = (JPanel)getContentPane();
        GroupLayout gl = new VGroupLayout(GroupLayout.STRETCH);
        gl.setOffAxisPolicy(GroupLayout.STRETCH);
        top.setLayout(gl);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // create the display for the test tile directory pref
        JPanel sub = new JPanel(new HGroupLayout(GroupLayout.STRETCH));
        sub.add(new JLabel("Directory to search for test tiles:"),
                GroupLayout.FIXED);
        EditorDialogUtil.addButton(this, sub,
            EditorConfig.getTestTileDirectory(), "testtiledir");
        top.add(sub);

        sub = new JPanel(new HGroupLayout());
        EditorDialogUtil.addButton(this, sub, "OK", "ok");
        top.add(sub);

        pack();
    }

    /**
     * Handle action events.
     */
    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.equals("testtiledir")) {
            changeTestTileDir((JButton) e.getSource());

        } else if (cmd.equals("ok")) {
            EditorDialogUtil.dispose(this);

        } else {
            log.warning("Unknown action command [cmd=" + cmd + "].");
        }
    }

    /**
     * Pop up a file selection box for specifying the directory to look
     * in for test tiles.
     */
    protected void changeTestTileDir (JButton button)
    {
        JFileChooser chooser;

        // figure out which
        File f = new File(button.getText());
        if (!f.exists()) {
            chooser = new JFileChooser();
        } else {
            chooser = new JFileChooser(f);
        }

        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setSelectedFile(f);

        int result = chooser.showDialog(this, "Select");
        if (JFileChooser.APPROVE_OPTION == result) {
            f = chooser.getSelectedFile();
            String newdir = f.getPath();
            button.setText(newdir);
            EditorConfig.setTestTileDirectory(newdir);
        }
    }
}
