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

import java.util.Arrays;
import java.util.Comparator;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Ordering;

import com.samskivert.util.StringUtil;

import com.samskivert.swing.HGroupLayout;

import com.threerings.media.image.ColorPository;
import com.threerings.media.image.ColorPository.ColorRecord;
import com.threerings.media.tile.NoSuchTileSetException;
import com.threerings.media.tile.RecolorableTileSet;
import com.threerings.media.tile.TileSet;
import com.threerings.media.tile.TileUtil;

import com.threerings.miso.client.SceneObject;

import com.threerings.stage.tools.editor.util.EditorContext;

/**
 * Used to edit object attributes.
 */
public class ObjectEditorDialog extends EditorDialog
{
    public ObjectEditorDialog (EditorContext ctx, EditorScenePanel panel)
    {
        super("Edit object attributes", ctx, panel);
    }

    @Override
    public void addComponents (JComponent panel)
    {
        // object action editor elements
        JPanel sub = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        sub.add(new JLabel("Object action command:"), HGroupLayout.FIXED);
        sub.add(_action = new JTextField());
        _action.addActionListener(this);
        _action.setActionCommand("ok");
        panel.add(sub);

        // create the priority slider
        sub = new JPanel(new HGroupLayout(HGroupLayout.STRETCH));
        sub.add(new JLabel("Render priority:"), HGroupLayout.FIXED);
        sub.add(_priority = new JSlider(-5, 5));
        _priority.setMajorTickSpacing(5);
        _priority.setMinorTickSpacing(1);
        _priority.setPaintTicks(true);
        panel.add(sub);

        // create colorization selectors
        JPanel zations = HGroupLayout.makeButtonBox(HGroupLayout.LEFT);
        zations.add(new JLabel("Colorizations:"));
        zations.add(_primary = new JComboBox(NO_CHOICES));
        zations.add(_secondary = new JComboBox(NO_CHOICES));
        zations.add(_tertiary = new JComboBox(NO_CHOICES));
        zations.add(_quaternary = new JComboBox(NO_CHOICES));
        panel.add(zations);
    }

    /**
     * Prepare the dialog for display. This method should be called before <code>display()</code>
     * is called.
     *
     * @param scobj the object to edit.
     */
    public void prepare (SceneObject scobj)
    {
        _scobj = scobj;

        // set our title to the name of the tileset and the tile index
        String title;
        int tsid = TileUtil.getTileSetId(scobj.info.tileId);
        int tidx = TileUtil.getTileIndex(scobj.info.tileId);
        TileSet tset = null;
        try {
            tset = _ctx.getTileManager().getTileSet(tsid);
            title = tset.getName() + ": " + tidx;
        } catch (NoSuchTileSetException nstse) {
            title = "Error(" + tsid + "): " + tidx;
        }
        title += " (" + StringUtil.coordsToString(_scobj.info.x, _scobj.info.y) + ")";
        setTitle(title);

        // configure our elements
        String atext = (scobj.info.action == null ? "" : scobj.info.action);
        _action.setText(atext);
        _priority.setValue(scobj.getPriority());

        // if the object supports colorizations, configure those
        Object[] pzations = null;
        Object[] szations = null;
        Object[] tzations = null;
        Object[] qzations = null;
        if (tset != null) {
            String[] zations = null;
            if (tset instanceof RecolorableTileSet) {
                zations = ((RecolorableTileSet)tset).getColorizations();
            }
            if (zations != null) {
                pzations = computeZations(zations, 0);
                szations = computeZations(zations, 1);
                tzations = computeZations(zations, 2);
                qzations = computeZations(zations, 3);
            }
        }
        configureZations(_primary, pzations, _scobj.info.getPrimaryZation());
        configureZations(_secondary, szations, _scobj.info.getSecondaryZation());
        configureZations(_tertiary, tzations, _scobj.info.getTertiaryZation());
        configureZations(_quaternary, qzations, _scobj.info.getQuaternaryZation());

        // select the text edit field and focus it
        _action.setCaretPosition(0);
        _action.moveCaretPosition(atext.length());
        _action.requestFocusInWindow();
    }

    protected Object[] computeZations (String[] zations, int index)
    {
        if (zations.length <= index || StringUtil.isBlank(zations[index])) {
            return null;
        }
        ColorPository cpos = _ctx.getColorPository();
        ColorRecord[] crecs = cpos.enumerateColors(zations[index]);
        if (crecs == null) {
            return null;
        }
        Object[] czations = new Object[crecs.length + 1];
        czations[0] = new ZationChoice(0, "<none>");
        for (int ii = 0; ii < crecs.length; ii++) {
            czations[ii + 1] = new ZationChoice(crecs[ii].colorId, crecs[ii].name);
        }
        Arrays.sort(czations);
        return czations;
    }

    protected void configureZations (JComboBox combo, Object[] zations, int colorId)
    {
        int selidx = 0;
        combo.setEnabled(zations != null);
        if (zations != null) {
            combo.removeAllItems();
            for (int ii = 0; ii < zations.length; ii++) {
                combo.addItem(zations[ii]);
                if (((ZationChoice)zations[ii]).colorId == colorId) {
                    selidx = ii;
                }
            }
        }
        combo.setSelectedIndex(selidx);
    }

    @Override
    public void accepted ()
    {
        _scobj.info.action = _action.getText();
        byte prio = (byte)_priority.getValue();
        if (prio != _scobj.getPriority()) {
            _scobj.setPriority(prio);
        }

        int ozations = _scobj.info.zations;
        ZationChoice pchoice = (ZationChoice)_primary.getSelectedItem();
        ZationChoice schoice = (ZationChoice)_secondary.getSelectedItem();
        ZationChoice tchoice = (ZationChoice)_tertiary.getSelectedItem();
        ZationChoice qchoice = (ZationChoice)_quaternary.getSelectedItem();
        _scobj.info.setZations(pchoice.colorId, schoice.colorId, tchoice.colorId, qchoice.colorId);
        if (ozations != _scobj.info.zations) {
            _scobj.refreshObjectTile(_panel);
        }

        _panel.objectEditorDismissed();
    }

    @Override
    public void cancelled ()
    {
        _panel.objectEditorDismissed();
    }

    /** Used to display colorization choices. */
    protected static class ZationChoice
        implements Comparable<ZationChoice>
    {
        public byte colorId;
        public String name;

        public ZationChoice (int colorId, String name) {
            this.colorId = (byte)colorId;
            this.name = name;
        }

        public int compareTo (ZationChoice that) {
            return ComparisonChain.start()
                .compare(this.name, that.name, NULLS_OK)
                .compare(this.colorId, that.colorId)
                .result();
        }

        @Override
        public String toString () {
            return name;
        }

        protected static final Comparator<String> NULLS_OK = Ordering.natural().nullsFirst();
    }

    protected JTextField _action;
    protected JSlider _priority;
    protected SceneObject _scobj;
    protected JComboBox _primary, _secondary, _tertiary, _quaternary;

    protected static final ZationChoice[] NO_CHOICES = new ZationChoice[] { new ZationChoice(0,
        "<none>") };
}
