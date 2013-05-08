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

import java.util.TreeSet;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.google.common.collect.Sets;

import com.samskivert.util.ComparableArrayList;

import com.samskivert.swing.GroupLayout;
import com.samskivert.swing.HGroupLayout;

import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ColorPository.ColorRecord;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.RecolorableTileSet;
import com.threerings.media.tile.TileManager;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.data.ObjectInfo;
import com.threerings.miso.data.SparseMisoSceneModel.ObjectVisitor;

import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageScene;
import com.threerings.stage.tools.editor.util.EditorContext;

/**
 * The scene info panel presents the user with options to select the scene layer to edit, and
 * whether to display tile coordinates and locations in the scene view.
 */
public class SceneInfoPanel extends JPanel
    implements ActionListener
{
    /**
     * Constructs the scene info panel.
     */
    public SceneInfoPanel (EditorContext ctx, EditorModel model, EditorScenePanel svpanel)
    {
        _ctx = ctx;
        _svpanel = svpanel;

        // configure the panel
        GroupLayout gl = new HGroupLayout();
        gl.setGap(12);
        setLayout(gl);

        JPanel vert = GroupLayout.makeVStretchBox(5);

        JPanel hbox = GroupLayout.makeHBox();

        // create a panel for the name label
        hbox.add(createLabel("Scene name:", _scenename = new JTextField(10)));
        _scenename.addActionListener(this);
        _scenename.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost (FocusEvent e) {
                _scenename.postActionEvent();
            }
        });

        // create a drop-down for selecting the scene type
        ComparableArrayList<String> types = new ComparableArrayList<String>();
        ctx.enumerateSceneTypes(types);
        types.sort();
        types.add(0, "");
        hbox.add(createLabel("Scene type:", _scenetype = new JComboBox(types.toArray())));
        _scenetype.addActionListener(this);

        vert.add(hbox);

        // set up the scene colorization stuff
        hbox = GroupLayout.makeButtonBox(GroupLayout.LEFT);
        hbox.add(_colorClasses = new JComboBox());
        hbox.add(_colorIds = new JComboBox());
        vert.add(createLabel("Colorizations:", hbox), GroupLayout.FIXED);
        _colorClasses.addActionListener(this);
        _colorIds.addActionListener(this);
        _colorClasses.addPopupMenuListener(new PopupMenuListener() {
            public void popupMenuCanceled (PopupMenuEvent e) { }
            public void popupMenuWillBecomeInvisible (PopupMenuEvent e) { }
            public void popupMenuWillBecomeVisible (PopupMenuEvent e) {
                recomputeColorClasses();
            }
        });

        add(vert, GroupLayout.FIXED);
    }

    protected JPanel createLabel (String label, JComponent comp)
    {
        JPanel panel = GroupLayout.makeButtonBox(GroupLayout.CENTER);
        panel.add(new JLabel(label), GroupLayout.FIXED);
        panel.add(comp);
        return panel;
    }

    /**
     * Called when the scene in the editor changes.
     */
    public void setScene (StageScene scene)
    {
        _scene = scene;
        _scenename.setText(scene.getName());
        _scenetype.setSelectedItem(scene.getType());

        recomputeColorClasses();
    }

    // documentation inherited from interface ActionListener
    public void actionPerformed (ActionEvent e)
    {
        Object src = e.getSource();
        if (src == _scenename) {
            _scene.setName(_scenename.getText().trim());

        } else if (src == _scenetype) {
            _scene.setType((String) _scenetype.getSelectedItem());

        } else if (src == _colorClasses) {
            String cclass = (String) _colorClasses.getSelectedItem();
            configureColorIds(cclass);

        } else if (src == _colorIds) {
            setNewDefaultColor();
        }
    }

    /**
     * Prior to the color classes popup popping up, recompute the possible values.
     */
    protected void recomputeColorClasses ()
    {
        // add all possible colorization names to the list
        final TileManager tilemgr = _ctx.getTileManager();
        final TreeSet<String> set = Sets.newTreeSet();
        StageMisoSceneModel msmodel = StageMisoSceneModel.getSceneModel(_scene.getSceneModel());
        msmodel.visitObjects(new ObjectVisitor() {
            public void visit (ObjectInfo info) {
                int tsid = TileUtil.getTileSetId(info.tileId);
                TileSet tset;
                try {
                    tset = tilemgr.getTileSet(tsid);
                } catch (NoSuchTileSetException nstse) {
                    return;
                }
                String[] zations;
                if (tset instanceof RecolorableTileSet) {
                    zations = ((RecolorableTileSet) tset).getColorizations();
                } else {
                    return;
                }
                if (zations != null) {
                    for (String zation : zations) {
                        set.add(zation);
                    }
                }
            }
        });

        Object selected = _colorClasses.getSelectedItem();
        DefaultComboBoxModel model = (DefaultComboBoxModel) _colorClasses.getModel();
        model.removeAllElements();
        for (String zation : set) {
            model.addElement(zation);
        }
        if (selected != null) {
            _colorClasses.setSelectedItem(selected);
        }
    }

    /**
     * Show which color Ids are available for the currently selected colorization class, and
     * select the selected one.
     */
    protected void configureColorIds (String cclass)
    {
        _colorIds.removeActionListener(this);
        try {
            DefaultComboBoxModel model = (DefaultComboBoxModel) _colorIds.getModel();
            model.removeAllElements();
            if (cclass == null) {
                return;
            }

            ColorPository cpos = _ctx.getColorPository();

            String noChoice = "<none>";
            String choice = noChoice;

            ColorPository.ClassRecord classRec = cpos.getClassRecord(cclass);
            int pick = _scene.getDefaultColor(classRec.classId);

            ColorPository.ColorRecord[] colors = cpos.enumerateColors(cclass);
            ComparableArrayList<String> list = new ComparableArrayList<String>();
            for (ColorRecord color : colors) {
                list.insertSorted(color.name);
                if (color.colorId == pick) {
                    choice = color.name;
                }
            }

            model.addElement(noChoice);
            for (int ii=0; ii < list.size(); ii++) {
                model.addElement(list.get(ii));
            }
            _colorIds.setSelectedItem(choice);

        } finally {
            _colorIds.addActionListener(this);
        }
    }

    /**
     * Called when a _colorIds color is selected.
     */
    protected void setNewDefaultColor ()
    {
        String cclass = (String) _colorClasses.getSelectedItem();
        if (cclass == null) {
            return;
        }

        ColorPository cpos = _ctx.getColorPository();
        ColorPository.ClassRecord classRec = cpos.getClassRecord(cclass);
        ColorPository.ColorRecord[] colors = cpos.enumerateColors(cclass);
        Object selected = _colorIds.getSelectedItem();
        int pick = -1;

        for (ColorRecord color : colors) {
            if (color.name.equals(selected)) {
                pick = color.colorId;
                break;
            }
        }

        // only update the scene if our selection has actually changed
        if (_scene.getDefaultColor(classRec.classId) != pick) {
            _scene.setDefaultColor(classRec.classId, pick);
            _svpanel.setScene(_scene);
            _svpanel.repaint();
        }
    }

    /** The giver of life. */
    protected EditorContext _ctx;

    /** The scene we're controlling. */
    protected StageScene _scene;

    /** The scene name entry field. */
    protected JTextField _scenename;

    /** The scene type selector. */
    protected JComboBox _scenetype, _colorClasses, _colorIds;

    /** The scene panel, which we hackily repaint when default colors change. */
    protected EditorScenePanel _svpanel;

    /** The object grip direction button. */
    protected DirectionButton _dirbutton;
}
