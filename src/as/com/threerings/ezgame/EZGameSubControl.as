//
// $Id: SeatingControl.as 271 2007-04-07 00:25:58Z dhoover $
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

/**
 * Dispatched when the controller changes for the game.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.CONTROL_CHANGED
 */
[Event(name="ControlChanged", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the game starts, usually after all players are present.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.GAME_STARTED
 */
[Event(name="GameStarted", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when a round starts.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.ROUND_STARTED
 */
[Event(name="RoundStarted", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the turn changes in a turn-based game.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.TURN_CHANGED
 */
[Event(name="TurnChanged", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when a round ends.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.ROUND_ENDED
 */
[Event(name="RoundEnded", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when the game ends.
 *
 * @eventType com.threerings.ezgame.StateChangedEvent.GAME_ENDED
 */
[Event(name="GameEnded", type="com.threerings.ezgame.StateChangedEvent")]

/**
 * Dispatched when an occupant enters the game.
 *
 * @eventType com.threerings.ezgame.OccupantChangedEvent.OCCUPANT_ENTERED
 */
[Event(name="OccupantEntered", type="com.threerings.ezgame.OccupantChangedEvent")]

/**
 * Dispatched when an occupant leaves the game.
 *
 * @eventType com.threerings.ezgame.OccupantChangedEvent.OCCUPANT_LEFT
 */
[Event(name="OccupantLeft", type="com.threerings.ezgame.OccupantChangedEvent")]

/**
 * Dispatched when a user chats.
 *
 * @eventType com.threerings.ezgame.UserChatEvent.TYPE
 */
[Event(name="UserChat", type="com.threerings.ezgame.UserChatEvent")]

/**
 * Access game-specific controls.
 */
public class EZGameSubControl extends AbstractSubControl
{
    public function EZGameSubControl (parent :AbstractGameControl)
    {
        super(parent);

        _seatingCtrl = createSeatingControl();
    }

    /**
     * Access the 'seating' subcontrol.
     */
    public function get seating () :EZSeatingSubControl
    {
        // TODO: this should return null for PARTY games
        return _seatingCtrl;
    }

    /**
     * Get any game-specific configurations that were set up in the lobby.
     */
    public function getConfig () :Object
    {
        return _gameConfig; 
    }

    /** 
     * Send a system chat message that will be seen by everyone in the game room,
     * even observers.
     */
    public function systemMessage (msg :String) :void
    {
        callHostCode("sendChat_v1", msg);
    }

    /**
     * If the game was not configured to auto-start, all clients must call this function to let the
     * server know that they are ready, at which point the game will be started. Once a game is
     * over, all clients can call this function again to start a new game.
     */
    public function playerReady () :void
    {
        callHostCode("playerReady_v1");
    }

    /**
     * Returns the player ids of all occupants in the game room.
     */
    public function getOccupantIds () :Array /* of playerId */
    {
        return (callHostCode("getOccupants_v1") as Array);
    }

    /**
     * Get the display name of the specified occupant.  Two players may have the same name: always
     * use playerId to purposes of identification and comparison. The name is for display
     * only. Will be null is the specified playerId is not in the game.
     */
    public function getOccupantName (playerId :int) :String
    {
        return String(callHostCode("getOccupantName_v1", playerId));
    }

    /**
     * Returns this client's player id.
     */
    public function getMyId () :int
    {
        return int(callHostCode("getMyId_v1"));
    }
    
    /**
     * Returns true if we are in control of this game. False if another client is in control.
     */
    public function amInControl () :Boolean
    {
        return getControllerId() == getMyId();
    }
    
    /**
     * Returns the player id of the client that is in control of this game.
     */
    public function getControllerId () :int
    {
        return int(callHostCode("getControllerId_v1"));
    }
    
    /**
     * Returns the player id of the current turn holder, or 0 if it's nobody's turn.
     */
    public function getTurnHolder () :int
    {
        return int(callHostCode("getTurnHolder_v1"));
    }

    /**
     * Returns the current round number. Rounds start at 1 and increase if the game calls {@link
     * #endRound} with a next round timeout. Between rounds, it returns a negative number,
     * corresponding to the negation of the round that just ended.
     */
    public function getRound () :int
    {
        return int(callHostCode("getRound_v1"));
    }

    /**
     * A convenience method to just check if it's our turn.
     */
    public function isMyTurn () :Boolean
    {
        return Boolean(callHostCode("isMyTurn_v1"));
    }

    /**
     * Is the game currently in play?
     */
    public function isInPlay () :Boolean
    {
        return Boolean(callHostCode("isInPlay_v1"));
    }

    /**
     * Start the next player's turn. If a playerId is specified, that player's turn will be
     * next. Otherwise the turn will progress to the next natural turn holder (following
     * seating order) or be assigned randomly if the game is just starting.
     */
    public function startNextTurn (nextPlayerId :int = 0) :void
    {
        callHostCode("startNextTurn_v1", nextPlayerId);
    }

    /**
     * Ends the current round. If nextRoundDelay is greater than zero, the next round will be
     * started in the specified number of seconds, otherwise no next round will be started.  This
     * method should not be called at the end of the last round, instead <code>endGame()</code>
     * should be called.
     */
    public function endRound (nextRoundDelay :int = 0) :void
    {
        callHostCode("endRound_v1", nextRoundDelay);
    }

    /**
     * End the game. The specified player ids are winners!
     */
    public function endGame (winnerIds :Array) :void
    {
        callHostCode("endGame_v2", winnerIds);
    }

    /**
     * Requests to start the game again in the specified number of seconds. This should only be
     * used for party games. Seated table games should have each player report that they are ready
     * again and the game will automatically start.
     */
    public function restartGameIn (seconds :int) :void
    {
        callHostCode("restartGameIn_v1", seconds);
    }

    /**
     * Create the 'seating' subcontrol.
     */
    protected function createSeatingControl () :EZSeatingSubControl
    {
        return new EZSeatingSubControl(_parent, this);
    }

    override protected function populateProperties (o :Object) :void
    {
        super.populateProperties(o);

        o["controlDidChange_v1"] = controlDidChange_v1;
        o["turnDidChange_v1"] = turnDidChange_v1;
        o["gameStateChanged_v1"] = gameStateChanged_v1;
        o["roundStateChanged_v1"] = roundStateChanged_v1;
        o["occupantChanged_v1"] = occupantChanged_v1;
        o["userChat_v1"] = userChat_v1;

        _seatingCtrl.populatePropertiesFriend(o);
    }

    override protected function setHostProps (o :Object) :void
    {
        super.setHostProps(o);

        _gameConfig = o.gameConfig;

        _seatingCtrl.setHostPropsFriend(o);
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function controlDidChange_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.CONTROL_CHANGED, _parent));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function turnDidChange_v1 () :void
    {
        dispatch(new StateChangedEvent(StateChangedEvent.TURN_CHANGED, _parent));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function gameStateChanged_v1 (started :Boolean) :void
    {
        dispatch(new StateChangedEvent(started ? StateChangedEvent.GAME_STARTED :
                                       StateChangedEvent.GAME_ENDED, _parent));
    }

    /**
     * Private method to post a StateChangedEvent.
     */
    private function roundStateChanged_v1 (started :Boolean) :void
    {
        dispatch(new StateChangedEvent(started ? StateChangedEvent.ROUND_STARTED :
                                       StateChangedEvent.ROUND_ENDED, _parent));
    }

    /**
     * Private method to post a OccupantEvent.
     */
    private function occupantChanged_v1 (occupantId :int, player :Boolean, enter :Boolean) :void
    {
        dispatch(new OccupantChangedEvent(
                     enter ? OccupantChangedEvent.OCCUPANT_ENTERED :
                     OccupantChangedEvent.OCCUPANT_LEFT, _parent, occupantId, player));
    }

    /**
     * Private method to post a UserChatEvent.
     */
    private function userChat_v1 (speaker :int, message :String) :void
    {
        dispatch(new UserChatEvent(_parent, speaker, message));
    }

    /** Contains any custom game configuration data. */
    protected var _gameConfig :Object = {};

    /** The seating sub-control. */
    protected var _seatingCtrl :EZSeatingSubControl;
}
}
