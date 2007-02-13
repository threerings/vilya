//
// $Id: GameController.java 3804 2006-01-13 01:52:36Z ray $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

package com.threerings.parlor.game.client {

import com.threerings.presents.dobj.AttributeChangeListener;
import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlaceController;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.data.GameCodes;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.util.ParlorContext;

/**
 * The game controller manages the flow and control of a game on the
 * client side. This class serves as the root of a hierarchy of controller
 * classes that aim to provide functionality shared between various
 * similar games. The base controller provides functionality for starting
 * and ending the game and for calculating ratings adjustements when a
 * game ends normally. It also handles the basic house keeping like
 * subscription to the game object and dispatch of commands and
 * distributed object events.
 */
public /*abstract*/ class GameController extends PlaceController
    implements AttributeChangeListener
{
    protected static const log :Log = Log.getLog(GameController);

    /**
     * Initializes this game controller with the game configuration that
     * was established during the match making process. Derived classes
     * may want to override this method to initialize themselves with
     * game-specific configuration parameters but they should be sure to
     * call <code>super.init</code> in such cases.
     *
     * @param ctx the client context.
     * @param config the configuration of the game we are intended to
     * control.
     */
    override public function init (ctx :CrowdContext, config :PlaceConfig) :void
    {
        // cast our references before we call super.init() so that when
        // super.init() calls createPlaceView(), we have our casted
        // references already in place
        _pctx = (ctx as ParlorContext);
        _gconfig = (config as GameConfig);

        super.init(ctx, config);
    }

    /**
     * Adds this controller as a listener to the game object (thus derived
     * classes need not do so) and lets the game manager know that we are
     * now ready to go.
     */
    override public function willEnterPlace (plobj :PlaceObject) :void
    {
        super.willEnterPlace(plobj);

        // obtain a casted reference
        _gobj = (plobj as GameObject);

        // if this place object is not our current location we'll need to
        // add it as an auxiliary chat source
        var bobj :BodyObject = 
            (_ctx.getClient().getClientObject() as BodyObject);
        if (bobj.location != plobj.getOid()) {
            _ctx.getChatDirector().addAuxiliarySource(
                _gobj, GameCodes.GAME_CHAT_TYPE);
        }

        // and add ourselves as a listener
        _gobj.addListener(this);

        // we don't want to claim to be finished until any derived classes
        // that overrode this method have executed, so we'll queue up a
        // runnable here that will let the game manager know that we're
        // ready on the next pass through the distributed event loop
        log.info("Entering game " + _gobj.which() + ".");
        if (_gobj.getPlayerIndex(bobj.getVisibleName()) != -1) {
            // finally let the game manager know that we're ready
            // to roll
            _ctx.getClient().callLater(playerReady);
        }
    }

    /**
     * Removes our listener registration from the game object and cleans
     * house.
     */
    override public function didLeavePlace (plobj :PlaceObject) :void
    {
        super.didLeavePlace(plobj);

        _ctx.getChatDirector().removeAuxiliarySource(_gobj);

        // unlisten to the game object
        _gobj.removeListener(this);
        _gobj = null;
    }

    /**
     * Returns whether the game is over.
     */
    public function isGameOver () :Boolean
    {
        var gameOver :Boolean = (_gobj == null) ||
            (_gobj.state != GameObject.IN_PLAY);
        return (_gameOver || gameOver);
    }

    /**
     * Sets the client game over override. This is used in situations
     * where we determine that the game is over before the server has
     * informed us of such.
     */
    public function setGameOver (gameOver :Boolean) :void
    {
        _gameOver = gameOver;
    }

    /**
     * Calls {@link #gameWillReset}, ends the current game (locally, it
     * does not tell the server to end the game), and waits to receive a
     * reset notification (which is simply an event setting the game state
     * to <code>IN_PLAY</code> even though it's already set to
     * <code>IN_PLAY</code>) from the server which will start up a new
     * game.  Derived classes should override {@link #gameWillReset} to
     * perform any game-specific animations.
     */
    public function resetGame () :void
    {
        // let derived classes do their thing
        gameWillReset();

        // end the game until we receive a new board
        setGameOver(true);
    }

    /**
     * Returns the unique round identifier for the current round.
     */
    public function getRoundId () :int
    {
        return (_gobj == null) ? -1 : _gobj.roundId;
    }

    /**
     * Handles basic game controller action events. Derived classes should
     * be sure to call <code>super.handleAction</code> for events they
     * don't specifically handle.
     */
    override public function handleAction (cmd :String, arg :Object) :Boolean
    {
        return super.handleAction(cmd, arg);
    }

    /**
     * A way for controllers to display a game-related system message.
     */
    public function systemMessage (bundle :String, msg :String) :void
    {
        _ctx.getChatDirector().displayInfo(
            bundle, msg, GameCodes.GAME_CHAT_TYPE);
    }

    // from interface AttributeChangeListener
    public function attributeChanged (event :AttributeChangedEvent) :void
    {
        // deal with game state changes
        var name :String = event.getName();
        if (GameObject.STATE == name) {
            var newState :int = int(event.getValue());
            if (!stateDidChange(newState)) {
                log.warning("Game transitioned to unknown state " +
                            "[gobj=" + _gobj + ", state=" + newState + "].");
            }
        }
    }

    /**
     * Derived classes can override this method if they add additional game
     * states and should handle transitions to those states, returning true to
     * indicate they were handled and calling super for the normal game states.
     */
    protected function stateDidChange (state :int) :Boolean
    {
        switch (state) {
        case GameObject.IN_PLAY:
            gameDidStart();
            return true;
        case GameObject.GAME_OVER:
            gameDidEnd();
            return true;
        case GameObject.CANCELLED:
            gameWasCancelled();
            return true;
        }
        return false;
    }

    /**
     * Called after we've entered the game and everything has initialized
     * to notify the server that we, as a player, are ready to play.
     */
    protected function playerReady () :void
    {
        log.info("Reporting ready " + _gobj.which() + ".");
        _gobj.manager.invoke("playerReady");
    }

    /**
     * Called when the game transitions to the <code>IN_PLAY</code>
     * state. This happens when all of the players have arrived and the
     * server starts the game.
     */
    protected function gameDidStart () :void
    {
        if (_gobj == null) {
            log.info("Received gameDidStart() after leaving game room.");
            return;
        }

        // clear out our game over flag
        setGameOver(false);

        // let our delegates do their business
        applyToDelegates(function (del :GameControllerDelegate) :void {
            del.gameDidStart();
        });
    }

    /**
     * Called when the game transitions to the <code>GAME_OVER</code>
     * state. This happens when the game reaches some end condition by
     * normal means (is not cancelled or aborted).
     */
    protected function gameDidEnd () :void
    {
        // let our delegates do their business
        applyToDelegates(function (del :GameControllerDelegate) :void {
            del.gameDidEnd();
        });
    }

    /**
     * Called when the game was cancelled for some reason.
     */
    protected function gameWasCancelled () :void
    {
        // let our delegates do their business
        applyToDelegates(function (del :GameControllerDelegate) :void {
            del.gameWasCancelled();
        });
    }

    /**
     * Called to give derived classes a chance to display animations, send
     * a final packet, or do any other business they care to do when the
     * game is about to reset.
     */
    protected function gameWillReset () :void
    {
        // let our delegates do their business
        applyToDelegates(function (del :GameControllerDelegate) :void {
            del.gameWillReset();
        });
    }

    /**
     * Convenience method to determine the type of game.
     */
    protected function getGameType () :int
    {
        return _gconfig.getGameType();
    }

    /** A reference to the active parlor context. */
    protected var _pctx :ParlorContext;

    /** Our game configuration information. */
    protected var _gconfig :GameConfig;

    /** A reference to the game object for the game that we're
     * controlling. */
    protected var _gobj :GameObject;

    /** A local flag overriding the game over state for situations where
     * the client knows the game is over before the server has
     * transitioned the game object accordingly. */
    protected var _gameOver :Boolean;
}
}
