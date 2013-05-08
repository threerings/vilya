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
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;

import com.samskivert.swing.HGroupLayout;
import com.samskivert.swing.VGroupLayout;

import com.threerings.stage.tools.editor.util.EditorContext;
import com.threerings.stage.tools.editor.util.EditorDialogUtil;

/**
 * Basic ok cancel dialog for use by editor components.
 */
public abstract class EditorDialog extends JInternalFrame
    implements ActionListener
{
    public EditorDialog (String title, EditorContext ctx, EditorScenePanel panel)
    {
        super(title, true);
        _ctx = ctx;
        _panel = panel;
        // get a handle on the top-level panel
        JPanel top = (JPanel)getContentPane();

        // set up a layout manager for the panel
        VGroupLayout gl = new VGroupLayout(VGroupLayout.STRETCH, VGroupLayout.STRETCH, 5,
            VGroupLayout.CENTER);
        top.setLayout(gl);
        top.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        addComponents(top);
        // create our OK/Cancel buttons
        JPanel sub = HGroupLayout.makeButtonBox(HGroupLayout.CENTER);
        EditorDialogUtil.addButton(this, sub, "OK", "ok");
        EditorDialogUtil.addButton(this, sub, "Cancel", "cancel");
        top.add(sub);
        pack();
    }

    public abstract void addComponents (JComponent dialogBody);

    public void actionPerformed (ActionEvent e)
    {
        String cmd = e.getActionCommand();

        if (cmd.equals("ok")) {
            accepted();
        } else if (cmd.equals("cancel")) {
            cancelled();
        } else {
            System.err.println("Received unknown action: " + e);
            return;
        }

        // hide the dialog
        EditorDialogUtil.dismiss(this);

    }

    /**
     * Called when ok is clicked.
     */
    public abstract void accepted ();

    /** Called when cancel is clicked. */
    public void cancelled ()
    {}

    protected EditorContext _ctx;
    protected EditorScenePanel _panel;
}