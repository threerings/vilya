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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.samskivert.swing.HGroupLayout;

import com.threerings.whirled.spot.data.Portal;
import com.threerings.whirled.spot.tools.EditablePortal;

import com.threerings.stage.data.StageScene;
import com.threerings.stage.tools.editor.util.EditorContext;

/**
 * The <code>PortalDialog</code> is used to present the user with a dialog
 * allowing them to enter the information associated with an
 * <code>EditablePortal</code>.  The dialog is used both to set up a new
 * portal and to edit an existing portal.
 */
public class PortalDialog extends EditorDialog
{
    /**
     * Constructs the portal dialog.
     */
    public PortalDialog (EditorContext ctx, EditorScenePanel panel)
    {
        super("Edit Portal", ctx, panel);
    }

    @Override
    public void addComponents(JComponent top){
        // add the dialog instruction text
        top.add(new JLabel("Enter settings for this portal:"));

        // create a panel to contain the portal name info
        JPanel sub = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        sub.add(new JLabel("Portal name:", SwingConstants.RIGHT));

        // create and add the portal name text entry field
        sub.add(_portalText = new JTextField());

        // add the portal name info to the top-level panel
        top.add(sub);

        // create a check box to allow making this the default
        // entrance portal
        _entrance = new JCheckBox("Default Entrance");
        _entrance.addActionListener(this);
        _entrance.setActionCommand("entrance");
        top.add(_entrance);
    }

    /**
     * Prepare the dialog for display.  This method should be called
     * before <code>display()</code> is called.
     *
     * @param port the portal to edit.
     */
    public void prepare (StageScene scene, EditablePortal port)
    {
        _port = port;
        _scene = scene;

        // if the location is already a portal, fill the text entry field
        // with the current scene name, else clear it
        String text = port.name;
        _portalText.setText(text);

        // select the text edit field
        _portalText.setCaretPosition(0);
        _portalText.moveCaretPosition(text.length());

        // select the default entrance check box appropriately
        Portal entry = _scene.getDefaultEntrance();
        _entrance.setSelected(entry == null ||
                              entry.portalId == _port.portalId);

        // request the keyboard focus so that the destination scene
        // name can be typed immediately
        _portalText.requestFocusInWindow();
    }

    /**
     * Handle action events on the dialog user interface elements.
     */
    @Override
    public void actionPerformed (ActionEvent e)
    {
        if (e.getActionCommand().equals("entrance")) {
            _entrance.setSelected(_entrance.isSelected());
        } else {
            super.actionPerformed(e);
        }
    }

    /**
     * Handles the user submitting the dialog via the "OK" button.
     */
    @Override
    public void accepted ()
    {
        // get the destination scene name
        _port.name = _portalText.getText();

        // update the scene's default entrance
        if (_entrance.isSelected()) {
            _scene.setDefaultEntrance(_port);

        } else if (_scene.getDefaultEntrance() == _port) {
            _scene.setDefaultEntrance(null);
        }
    }

    @Override
    protected void processKeyEvent (KeyEvent e)
    {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_ENTER: accepted(); break;
        case KeyEvent.VK_ESCAPE: setVisible(false); break;
        }
    }

    /** The scene. */
    protected StageScene _scene;

    /** The portal name text entry field. */
    protected JTextField _portalText;

    /** The portal default entrance check box. */
    protected JCheckBox _entrance;

    /** The location object denoting the portal location. */
    protected EditablePortal _port;

    /** The combo box listing the direction orientations. */
    protected JComboBox _orientcombo;
}
