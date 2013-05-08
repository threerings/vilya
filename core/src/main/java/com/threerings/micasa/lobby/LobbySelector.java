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

package com.threerings.micasa.lobby;

import java.util.List;
import java.util.Map;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;

import com.google.common.collect.Maps;

import com.threerings.crowd.data.PlaceObject;

import com.threerings.micasa.util.MiCasaContext;

import static com.threerings.micasa.Log.log;

/**
 * The lobby selector displays a drop-down box listing the categories of
 * lobbies available on this server and when a category is selected, it
 * displays a list of the lobbies that are available in that category.  If
 * a lobby is double-clicked, it moves the client into that lobby room.
 */
public class LobbySelector extends JPanel
    implements ActionListener, LobbyService.CategoriesListener,
               LobbyService.LobbiesListener
{
    /**
     * Constructs a new lobby selector component. It will wait until it is
     * visible before issuing a get categories request to download a list
     * of categories.
     */
    public LobbySelector (MiCasaContext ctx)
    {
        setLayout(new BorderLayout());

        // keep this around for later
        _ctx = ctx;

        // create our drop-down box with a bogus entry at the moment
        String[] options = new String[] { CAT_FIRST_ITEM };
        _combo = new JComboBox(options);
        _combo.addActionListener(this);
        add(_combo, BorderLayout.NORTH);

        // and create our empty lobby list
        _loblist = new JList();
        _loblist.setCellRenderer(new LobbyCellRenderer());
        // add a mouse listener that tells us about double clicks
        MouseListener ml = new MouseAdapter() {
            @Override
            public void mouseClicked (MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Object item = _loblist.getSelectedValue();
                    enterLobby((Lobby)item);
                }
            }
        };
        _loblist.addMouseListener(ml);
        add(_loblist, BorderLayout.CENTER);
    }

    @Override
    public void addNotify ()
    {
        super.addNotify();

        // get a handle on our lobby service instance
        _lservice = _ctx.getClient().requireService(LobbyService.class);
        // and use them to look up the lobby categories
        _lservice.getCategories(this);
    }

    /**
     * Called when the user selects a category or double-clicks on a
     * lobby.
     */
    public void actionPerformed (ActionEvent evt)
    {
        if (evt.getSource() == _combo) {
            String selcat = (String)_combo.getSelectedItem();
            if (!selcat.equals(CAT_FIRST_ITEM)) {
                selectCategory(selcat);
            }
        }
    }

    // documentation inherited from interface
    public void gotCategories (String[] categories)
    {
        // append these to our "unselected" item
        for (String categorie : categories) {
            _combo.addItem(categorie);
        }
    }

    // documentation inherited from interface
    public void gotLobbies (List<Lobby> lobbies)
    {
        // create a list model for this category
        DefaultListModel model = new DefaultListModel();

        // populate it with the lobby info
        for (Lobby lobby : lobbies) {
            model.addElement(lobby);
        }

        // stick it in the table
        _catlists.put(_pendingCategory, model);

        // finally tell the lobby list to update the display (which we do
        // by setting the combo box to this category in case the luser
        // decided to try to select some new category while we weren't
        // looking)
        _combo.setSelectedItem(_pendingCategory);

        // clear out our pending category indicator
        _pendingCategory = null;
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        log.info("Request failed [reason=" + reason + "].");

        // clear out our pending category indicator in case this was a
        // failed getLobbies() request
        _pendingCategory = null;
    }

    /**
     * Fetches the list of lobbies available in a particular category (if
     * they haven't already been fetched) and displays them in the lobby
     * list.
     */
    protected void selectCategory (String category)
    {
        DefaultListModel model = _catlists.get(category);
        if (model != null) {
            _loblist.setModel(model);

        } else if (_pendingCategory == null) {
            // make a note that we're loading up this category
            _pendingCategory = category;
            // issue a request to load up the lobbies in this category
            _lservice.getLobbies(category, this);

        } else {
            log.info("Ignoring category select request because " +
                     "one is outstanding [pcat=" + _pendingCategory +
                     ", newcat=" + category + "].");
        }
    }

    /** Called when the user selects a lobby from the lobby list. */
    protected void enterLobby (Lobby lobby)
    {
        // make sure we're not already in this lobby
        PlaceObject plobj = _ctx.getLocationDirector().getPlaceObject();
        if (plobj != null && plobj.getOid() == lobby.placeOid) {
            return;
        }

        // otherwise request that we go there
        _ctx.getLocationDirector().moveTo(lobby.placeOid);

        log.info("Entering lobby " + lobby + ".");

    }

    /** Used to render Lobby instances in our lobby list. */
    protected static class LobbyCellRenderer
        extends DefaultListCellRenderer
    {
        @Override
        public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {
            // use the lobby's name rather than the value of toString()
            value = ((Lobby)value).name;
            return super.getListCellRendererComponent(
                list, value, index, isSelected, cellHasFocus);
        }
    }

    protected MiCasaContext _ctx;
    protected LobbyService _lservice;

    protected JComboBox _combo;
    protected JList _loblist;

    protected Map<String, DefaultListModel> _catlists = Maps.newHashMap();
    protected String _pendingCategory;

    protected static final String CAT_FIRST_ITEM = "<categories...>";
}
