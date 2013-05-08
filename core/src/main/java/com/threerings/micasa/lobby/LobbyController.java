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

import com.threerings.util.Name;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.client.Invitation;
import com.threerings.parlor.client.InvitationHandler;
import com.threerings.parlor.client.InvitationResponseObserver;
import com.threerings.parlor.game.data.GameConfig;

import com.threerings.micasa.util.MiCasaContext;

import static com.threerings.micasa.Log.log;

public class LobbyController extends PlaceController
    implements InvitationHandler, InvitationResponseObserver
{
    @Override
    public void init (CrowdContext ctx, PlaceConfig config)
    {
        // cast our context reference
        _ctx = (MiCasaContext)ctx;
        _config = (LobbyConfig)config;

        super.init(ctx, config);

        // register ourselves as the invitation handler
        _ctx.getParlorDirector().setInvitationHandler(this);
    }

    @Override
    protected PlaceView createPlaceView (CrowdContext ctx)
    {
        return new LobbyPanel(_ctx, _config);
    }

    @Override
    public void willEnterPlace (PlaceObject plobj)
    {
        super.willEnterPlace(plobj);

        try {
            // if a system property has been specified requesting that we
            // invite another player, do so now
            String invitee = System.getProperty("invitee");
            if (invitee != null) {
                // create a game config object
                try {
                    GameConfig config = _config.getGameConfig();
                    _ctx.getParlorDirector().invite(
                        new Name(invitee), config, this);
                } catch (Exception e) {
                    log.warning("Error instantiating game config.", e);
                }
            }

        } catch (SecurityException se) {
            // nothing to see here, move it along...
        }
    }

    // documentation inherited from interface
    public void invitationReceived (Invitation invite)
    {
        log.info("Invitation received [invite=" + invite + "].");

        // accept the invitation. we're game...
        invite.accept();
    }

    // documentation inherited from interface
    public void invitationCancelled (Invitation invite)
    {
        log.info("Invitation cancelled " + invite + ".");
    }

    // documentation inherited from interface
    public void invitationAccepted (Invitation invite)
    {
        log.info("Invitation accepted " + invite + ".");
    }

    // documentation inherited from interface
    public void invitationRefused (Invitation invite, String message)
    {
        log.info("Invitation refused [invite=" + invite +
                 ", message=" + message + "].");
    }

    // documentation inherited from interface
    public void invitationCountered (Invitation invite, GameConfig config)
    {
        log.info("Invitation countered [invite=" + invite +
                 ", config=" + config + "].");
    }

    protected MiCasaContext _ctx;
    protected LobbyConfig _config;
}
