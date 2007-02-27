//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.ezgame {

import flash.events.EventDispatcher;

/**
 * HostCoordinator provides methods for determining whether the current client should consider
 * itself the authority in dealing with game state (for example, when dealing out cards, or setting
 * up the game board).
 *
 * Current hosting state can be retrieved from the /status/ property.  Please note that hosting
 * authority can be transferred between clients at any moment, so all clients should be prepared to
 * suddenly take over host responsibilities.
 *
 * Additionally, the client can register itself for notifications about changes to the /status/
 * property - see HostEvent.
 *
 * Usage example:
 *
 * <pre>
 * _coord = new HostCoordinator (_gameCtrl);
 * _coord.addEventListener (HostEvent.CLAIMED, firstHostHandler);
 * _coord.addEventListener (HostEvent.CHANGED, hostChangeHandler);
 *
 * ...
 *
 * function firstHostHandler (event : HostEvent) {
 *     if (_coord.status == HostCoordinator.STATUS_HOST) {
 *         // I'm the first host in the game - I should set up the initial board, etc.
 *     }
 * }
 *
 * function hostChangeHandler (event : HostEvent) {
 *     if (_coord.status == HostCoordinator.STATUS_HOST) {
 *         // I just became the host - do whatever is needed.
 *     }
 * }
 * </pre>
 */
public class HostCoordinator extends EventDispatcher
    implements OccupantChangedListener, PropertyChangedListener
{
    // PUBLIC INTERFACE

    /** Status constant, means that the current status is unknown, either because the data had not
     * yet arrived from the server, or because nobody claims to be host for this game. */
    public static const STATUS_UNKNOWN : String = "STATUS_UNKNOWN";

    /** Status constant, means that this client is the current host. */
    public static const STATUS_HOST : String = "STATUS_HOST";

    /** Status constant, means that some other client is the current host. */
    public static const STATUS_NOT_HOST : String = "STATUS_NOT_HOST";

    /**
     * Constructor, expects an initialized instance of EZGameControl.  If the optional showDebug
     * flag is set, it will display host info as chat messages (only useful for debugging).
     */
    public function HostCoordinator (control : EZGameControl, showDebug : Boolean = false)
    {
        _control = control;
        _control.registerListener(this);
        _showDebug = showDebug;

        if (_control.isConnected()) {
            // Try set host role if it's not already claimed (i.e. null)
            debugLog("Trying to claim host role.");
            debugLog("Current host: " + getHostId());
            tryReplaceHost(null);
        }
    }

    /**
     * Retrieves current host coordination status, as one of the STATUS_* constants.
     * If the current host is not known, or had abandoned the game, the client will silently
     * try to claim the host role while returning this status property.
     *
     * Note: do not save the value of this property. Hosting status can change at any moment: when
     * the current host quits the game, any client can suddenly become the new host.
     */
    public function get status () : String
    {
        var hostId : Number = getHostId(true);
        
        if (hostId == _control.getMyId()) {
            return STATUS_HOST;
        }
        if (hostId != 0) {
            return STATUS_NOT_HOST;
        }
        return STATUS_UNKNOWN;
    }

    // EVENT HANDLERS

    // from interface OccupantChangedListener
    public function occupantEntered (event : OccupantChangedEvent) : void
    {
        debugHostStatus ();
    }

    // from interface OccupantChangedListener
    public function occupantLeft (event : OccupantChangedEvent) : void
    {
        // If the occupant who just left was the host, every client will get a shot at trying to
        // claim their role.
        debugLog("My ID: " + _control.getMyId());
        debugLog("Occupant left: " + event.occupantId);
        debugLog("Current host: " + getHostId());

        if (getHostId() == event.occupantId) { // Elvis has left the building
            // Clear the old host value, and put myself in their place.  Only the first client will
            // succeed in doing this.
            tryReplaceHost (event.occupantId);
        }
    }

    // from interface PropertyChangedListener
    public function propertyChanged (event : PropertyChangedEvent) : void
    {
        if (event.name == HOST_NAME) {
            debugLog("Host variable changed from " + event.oldValue + " to " + event.newValue);
            this.dispatchEvent(
                new HostEvent(
                    HostEvent.CHANGED, _control, Number(event.oldValue), Number(event.newValue)));
        }
    }

    // IMPLEMENTATION DETAILS

    /**
     * Retrieves and returns current host's playerId. If the current host is unknown or
     * no longer in the game, returns 0.
     *
     * If the claimIfAvailable flag is set, and the host role is available (either because
     * no previous host has been recorded, or the currently known host had abandoned the game),
     * the current client will try to claim the host role. 
     */
    protected function getHostId (claimIfAvailable : Boolean = false) : Number
    {
        var host : Object = _control.get(HOST_NAME);
        
        if (host == null ||                                   // no host was recorded, or
            _control.getOccupantName (Number(host)) == null)  // host has left the game
        {
            if (claimIfAvailable) {
                tryReplaceHost (host);
            }
            return 0;
            
        } else {
            // otherwise, the host is present and known
            return Number(host);
        }
    }

    /**
     * Helper function: tries to set the host variable to the client's id, if the current value is
     * equal to /hostId/. If the value of null for hostId has the meaning of "set me as host only
     * if the role has not been claimed yet".
     */
    protected function tryReplaceHost (hostId : Object) : void
    {
        debugLog("Removing old host with id " + hostId);
        _control.testAndSet (HOST_NAME, _control.getMyId (), hostId);
        debugHostStatus ();
    }

    /** Debug only */
    protected function debugLog (message : String) : void
    {
        if (_showDebug) {
            _control.localChat (message);
        }
    }

    /** Debug only */
    protected function debugHostStatus () : void
    {
        debugLog((getHostId() == _control.getMyId() ? "I am" : "I'm not") +
                 " the host (id " + getHostId() + ", mine is " + _control.getMyId() + ")");
    }

    /** Magic key that stores current authoritative host */
    protected var HOST_NAME : String = "_host_name";

    /** Controller storage */
    protected var _control : EZGameControl = null;

    /** Debug flag */
    protected var _showDebug : Boolean;
}
}


