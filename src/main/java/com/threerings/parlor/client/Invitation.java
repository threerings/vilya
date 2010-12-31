//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.parlor.client;

import com.threerings.util.Name;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.util.ParlorContext;

import static com.threerings.parlor.Log.log;

/**
 * The invitation class is used to track information related to
 * outstanding invitations generated by or targeted to this client.
 */
public class Invitation
    implements ParlorCodes, ParlorService.InviteListener
{
    /** The unique id for this invitation (as assigned by the
     * server). This is -1 until we receive an acknowledgement from
     * the server that our invitation was delivered. */
    public int inviteId = -1;

    /** The name of the other user involved in this invitation. */
    public Name opponent;

    /** The configuration of the game to be created. */
    public GameConfig config;

    /** Constructs a new invitation record. */
    public Invitation (ParlorContext ctx, ParlorService pservice,
                       Name opponent, GameConfig config,
                       InvitationResponseObserver observer)
    {
        _ctx = ctx;
        _pservice = pservice;
        _observer = observer;
        this.opponent = opponent;
        this.config = config;
    }

    /**
     * Accepts this invitation.
     */
    public void accept ()
    {
        // generate the invocation service request
        _pservice.respond(inviteId, INVITATION_ACCEPTED, null, this);
    }

    /**
     * Refuses this invitation.
     *
     * @param message the message to deliver to the inviting user
     * explaining the reason for the refusal or null if no message is to
     * be provided.
     */
    public void refuse (String message)
    {
        // generate the invocation service request
        _pservice.respond(inviteId, INVITATION_REFUSED, message, this);
    }

    /**
     * Cancels this invitation.
     */
    public void cancel ()
    {
        // if the invitation has not yet been acknowleged by the
        // server, we make a note that it should be cancelled when we
        // do receive the acknowlegement
        if (inviteId == -1) {
            _cancelled = true;

        } else {
            // otherwise, generate the invocation service request
            _pservice.cancel(inviteId, this);
            // and remove it from the pending table
            _ctx.getParlorDirector().clearInvitation(this);
        }
    }

    /**
     * Counters this invitation with an invitation with different game
     * configuration parameters.
     *
     * @param config the updated game configuration.
     * @param observer the entity that will be notified if this
     * counter-invitation is accepted, refused or countered.
     */
    public void counter (GameConfig config, InvitationResponseObserver observer)
    {
        // update our observer (who will eventually be hearing back from
        // the other client about their counter-invitation)
        _observer = observer;

        // generate the invocation service request
        _pservice.respond(inviteId, INVITATION_COUNTERED, config, this);
    }

    // documentation inherited from interface
    public void inviteReceived (int inviteId)
    {
        // fill in our invitation id
        this.inviteId = inviteId;

        // if the invitation was cancelled before we heard back about
        // it, we need to send off a cancellation request now
        if (_cancelled) {
            _pservice.cancel(inviteId, this);
        } else {
            // otherwise, put it in the pending invites table
            _ctx.getParlorDirector().registerInvitation(this);
        }
    }

    // documentation inherited from interface
    public void requestFailed (String reason)
    {
        // let the observer know what's up
        _observer.invitationRefused(this, reason);
    }

    /**
     * Called by the parlor director when we receive a response to an
     * invitation initiated by this client.
     */
    protected void receivedResponse (int code, Object arg)
    {
        // make sure we have an observer to notify
        if (_observer == null) {
            log.warning("No observer registered for invitation " +
                        this + ".");
            return;
        }

        // notify the observer
        try {
            switch (code) {
            case INVITATION_ACCEPTED:
                _observer.invitationAccepted(this);
                break;

            case INVITATION_REFUSED:
                _observer.invitationRefused(this, (String)arg);
                break;

            case INVITATION_COUNTERED:
                _observer.invitationCountered(this, (GameConfig)arg);
                break;
            }

        } catch (Exception e) {
            log.warning("Invitation response observer choked on response " +
                        "[code=" + code + ", arg=" + arg +
                        ", invite=" + this + "].", e);
        }

        // unless the invitation was countered, we can remove it from the
        // pending table because it's resolved
        if (code != INVITATION_COUNTERED) {
            _ctx.getParlorDirector().clearInvitation(this);
        }
    }

    @Override
    public String toString ()
    {
        return "[inviteId=" + inviteId + ", opponent=" + opponent +
            ", config=" + config + ", observer=" + _observer +
            ", cancelled=" + _cancelled + "]";
    }

    /** Provides access to client services. */
    protected ParlorContext _ctx;

    /** Provides access to parlor services. */
    protected ParlorService _pservice;

    /** The entity to notify when we receive a response for this
     * invitation. */
    protected InvitationResponseObserver _observer;

    /** A flag indicating that we were requested to cancel this
     * invitation before we even heard back with an acknowledgement
     * that it was received by the server. */
    protected boolean _cancelled = false;
}
