//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.client {

import com.threerings.util.ArrayUtil;
import com.threerings.util.Map;
import com.threerings.util.Maps;
import com.threerings.util.Log;
import com.threerings.util.Name;

import com.threerings.presents.client.BasicDirector;
import com.threerings.presents.client.Client;
import com.threerings.presents.client.ClientEvent;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.client.InvocationService_ConfirmListener;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.data.ParlorMarshaller;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

/**
 * The parlor director manages the client side of the game configuration and matchmaking processes.
 * It is also the entity that is listening for game start notifications which it then dispatches
 * the client entity that will actually create and display the user interface for the game that
 * started.
 */
public class ParlorDirector extends BasicDirector
    implements ParlorReceiver
{
    // statically reference classes we require
    ParlorMarshaller;

    /**
     * Constructs a parlor director and provides it with the parlor context that it can use to
     * access the client services that it needs to provide its own services. Only one parlor
     * director should be active in the client at one time and it should be made available via the
     * parlor context.
     *
     * @param ctx the parlor context in use by the client.
     */
    public function ParlorDirector (ctx :ParlorContext)
    {
        super(ctx);
        _pctx = ctx;

        // register ourselves with the invocation director as a parlor notification receiver
        _pctx.getClient().getInvocationDirector().registerReceiver(new ParlorDecoder(this));
    }

    /**
     * Sets the invitation handler, which is the entity that will be notified when we receive
     * incoming invitation notifications and when invitations have been cancelled.
     *
     * @param handler our new invitation handler.
     */
    public function setInvitationHandler (handler :InvitationHandler) :void
    {
        _handler = handler;
    }

    /**
     * Adds the specified observer to the list of entities that are notified when we receive a game
     * ready notification.
     */
    public function addGameReadyObserver (observer :GameReadyObserver) :void
    {
        _grobs.push(observer);
    }

    /**
     * Removes the specified observer from the list of entities that are notified when we receive a
     * game ready notification.
     */
    public function removeGameReadyObserver (observer :GameReadyObserver) :void
    {
        ArrayUtil.removeFirst(_grobs, observer);
    }

    /**
     * Requests that the named user be invited to a game described by the supplied game config.
     *
     * @param invitee the user to invite.
     * @param config the configuration of the game to which the user is being invited.
     * @param observer the entity that will be notified if this invitation is accepted, refused or
     * countered.
     *
     * @return an invitation object that can be used to manage the outstanding invitation.
     */
    public function invite (
        invitee :Name, config :GameConfig, observer :InvitationResponseObserver) :Invitation
    {
        // create the invitation record
        var invite :Invitation = new Invitation(_pctx, _pservice, invitee, config, observer);
        // submit the invitation request to the server
        _pservice.invite(invitee, config, invite);
        // and return the invitation to the caller
        return invite;
    }

    /**
     * Requests that the specified single player game be started.
     *
     * @param config the configuration of the single player game to be started.
     * @param listener a listener to be informed of failure if the game cannot be started.
     */
    public function startSolitaire (
        config :GameConfig, listener :InvocationService_ConfirmListener) :void
    {
        _pservice.startSolitaire(config, listener);
    }

    // documentation inherited
    override public function clientDidLogoff (event :ClientEvent) :void
    {
        super.clientDidLogoff(event);
        _pservice = null;
        _pendingInvites.clear();
    }

    // documentation inherited from interface
    public function gameIsReady (gameOid :int) :void
    {
        _log.info("Handling game ready", "goid", gameOid);

        // see what our observers have to say about it
        var handled :Boolean = false;
        for (var ii :int = 0; ii < _grobs.length; ii++) {
            var grob :GameReadyObserver = (_grobs[ii] as GameReadyObserver);
            handled = grob.receivedGameReady(gameOid) || handled;
        }

        // if none of the observers took matters into their own hands, then we'll head on over to
        // the game room ourselves
        if (!handled) {
            _pctx.getLocationDirector().moveTo(gameOid);
        }
    }

    // documentation inherited from interface
    public function receivedInvite (remoteId :int, inviter :Name, config :GameConfig) :void
    {
        // create an invitation record for this invitation
        var invite :Invitation = new Invitation(_pctx, _pservice, inviter, config, null);
        invite.inviteId = remoteId;

        // put it in the pending invitations table
        _pendingInvites.put(remoteId, invite);

        try {
            // notify the invitation handler of the incoming invitation
            _handler.invitationReceived(invite);

        } catch (err :Error) {
            _log.warning("Invitation handler choked on invite notification", "invite", invite, err);
        }
    }

    // documentation inherited from interface
    public function receivedInviteResponse (remoteId :int, code :int, arg :Object) :void
    {
        // look up the invitation record for this invitation
        var invite :Invitation = (_pendingInvites.get(remoteId) as Invitation);
        if (invite == null) {
            _log.warning("Have no record of invitation for which we received a response?!",
                "remoteId", remoteId, "code", code, "arg", arg);
        } else {
            invite.receivedResponse(code, arg);
        }
    }

    // documentation inherited from interface
    public function receivedInviteCancellation (remoteId :int) :void
    {
        // TBD
    }

    /**
     * Register a new invitation in our pending invitations table. The invitation will call this
     * when it knows its invitation id.
     */
    public function registerInvitation (invite :Invitation) :void
    {
        _pendingInvites.put(invite.inviteId, invite);
    }

    /**
     * Called by an invitation when it knows it is no longer and can be cleared from the pending
     * invitations table.
     */
    public function clearInvitation (invite :Invitation) :void
    {
        _pendingInvites.remove(invite.inviteId);
    }

    // from BasicDirector
    override protected function registerServices (client :Client) :void
    {
        client.addServiceGroup(ParlorCodes.PARLOR_GROUP);
    }

    // from BasicDirector
    override protected function fetchServices (client :Client) :void
    {
        // get a handle on our parlor services
        _pservice = (client.requireService(ParlorService) as ParlorService);
        super.fetchServices(client);
    }

    /** An active parlor context. */
    protected var _pctx :ParlorContext;

    /** Provides access to parlor server side services. */
    protected var _pservice :ParlorService;

    /** The entity that has registered itself to handle incoming invitation notifications. */
    protected var _handler :InvitationHandler;

    /** A table of acknowledged (but not yet accepted or refused) invitation requests, keyed on
     * invitation id. */
    protected var _pendingInvites :Map = Maps.newMapOf(int);

    /** We notify the entities on this list when we get a game ready notification. */
    protected var _grobs :Array = new Array();

    /** For great logging. */
    private var _log :Log = Log.getLog(this);
}
}
