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

package com.threerings.parlor.turn.server;

import com.samskivert.util.RandomUtil;

import com.threerings.util.Name;

import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.game.server.GameManager;
import com.threerings.parlor.game.server.GameManagerDelegate;
import com.threerings.parlor.turn.data.TurnGameObject;

import static com.threerings.parlor.Log.log;

/**
 * Performs the server-side turn-based game processing for a turn based game.  Game managers which
 * wish to make use of the turn services must implement {@link TurnGameManager} and either create
 * an instance of this class, or an instance of a derivation which customizes the behavior, either
 * of which would be passed to {@link PlaceManager#addDelegate} to be activated.
 */
public class TurnGameManagerDelegate extends GameManagerDelegate
{
    public TurnGameManagerDelegate ()
    {
    }

    /**
     * @deprecated use the zero-argument constructor.
     */
    @Deprecated public TurnGameManagerDelegate (TurnGameManager tgmgr)
    {
    }

    /**
     * Returns the index of the current turn holder as configured in the game object.
     *
     * @return the index into the players array of the current turn holder or <code>-1</code> if
     * there is no current turn holder.
     */
    public int getTurnHolderIndex ()
    {
        return _tgmgr.getPlayerIndex(_turnGame.getTurnHolder());
    }

    /** Test if it's the inputted player's turn. */
    public boolean isPlayersTurn (int playerIndex)
    {
        // Don't accidently match a visitor's id of -1 with the "no one's turn" state of turn -1.
        int turnHolder = getTurnHolderIndex();
        if (turnHolder < 0) {
            return false;
        }

        // It's this player's turn if the ids match
        return (turnHolder == playerIndex);
    }

    /**
     * Called to start the next turn. It calls {@link TurnGameManager#turnWillStart} to allow our
     * owning manager to perform any pre-start turn processing, sets the turn holder that was
     * configured either when the game started or when finishing up the last turn, and then calls
     * {@link TurnGameManager#turnDidStart} to allow the manager to perform any post-start turn
     * processing. This assumes that a valid turn holder has been assigned. If some pre-game
     * preparation needs to take place in a non-turn-based manner, this function should not be
     * called until it is time to start the first turn.
     */
    public void startTurn ()
    {
        // sanity check
        if (_turnIdx < 0 || _turnIdx >= _turnGame.getPlayers().length) {
            log.warning("startTurn() called with invalid turn index", "game", where(),
                "turnIdx", _turnIdx);
            // abort, abort
            return;
        }

        // get the player name and sanity-check again
        Name name = _tgmgr.getPlayerName(_turnIdx);
        if (name == null) {
            log.warning("startTurn() called with invalid player [game=" + where() +
                        ", turnIdx=" + _turnIdx + "].");
            return;
        }

        // do pre-start processing
        _tgmgr.turnWillStart();

        // and set the turn indicator accordingly
        _turnGame.setTurnHolder(name);

        // do post-start processing
        _tgmgr.turnDidStart();
    }

    /**
     * Called to end the turn. Whatever indication a game manager has that the turn has ended
     * (probably the submission of a valid move of some sort by the turn holding player), it
     * should call this function to cause this turn to end and the next to begin.
     *
     * <p>
     * If the game is no longer in play (see {@link TurnGameObject#isInPlay}) after having called
     * {@link TurnGameManager#turnDidEnd} and {@link #setNextTurnHolder}, then the next turn will
     * not automatically be started.
     *
     * <p>
     * If the game is in play, but the next turn should not be started immediately, the game
     * manager should have {@link #setNextTurnHolder} set the {@link #_turnIdx} field to
     * <code>-1</code> which will cause us to not start the next turn. To start things back up
     * again it would set {@link #_turnIdx} to the next turn holder and call {@link #startTurn}
     * itself.
     */
    public void endTurn ()
    {
        // let the manager know that the turn is over
        _tgmgr.turnDidEnd();

        // figure out who's up next
        setNextTurnHolder();

        // and start the next turn if appropriate
        if (_turnGame.isInPlay() && _turnIdx != -1) {
            startTurn();

        } else {
            // otherwise, clear out the turn holder
            _turnGame.setTurnHolder(null);
        }
    }

    @Override
    public void didInit (PlaceConfig config)
    {
        super.didInit(config);
        _tgmgr = (TurnGameManager)_plmgr;
    }

    @Override
    public void didStartup (PlaceObject plobj)
    {
        _turnGame = (TurnGameObject)plobj;
    }

    @Override
    public void playerWasReplaced (int pidx, Name oplayer, Name nplayer)
    {
        // we need to update the turn holder if the current turn holder was the player that was
        // replaced and we need to do so in a way that doesn't make everyone think that the turn
        // just changed
        if (oplayer != null && oplayer.equals(_turnGame.getTurnHolder())) {
            // small hackery: this will indicate to the client that we are replacing the turn
            // holder rather than changing the turn
            _turnGame.setTurnHolder(TurnGameObject.TURN_HOLDER_REPLACED);
            _turnGame.setTurnHolder(nplayer);
        }
    }

    /**
     * This should be called from {@link GameManager#gameDidStart} to let the turn delegate perform
     * start of game processing.
     */
    @Override
    public void gameDidStart ()
    {
        // figure out who will be first
        setFirstTurnHolder();

        // and start the first turn if we should apparently do so
        if (_turnIdx != -1) {
            startTurn();
        }
    }

    /**
     * This is called to determine which player will take the first turn. The default
     * implementation chooses a player at random.
     */
    protected void setFirstTurnHolder ()
    {
        assignTurnRandomly();
    }

    /**
     * This is called to determine which player will next hold the turn.  The default
     * implementation simply rotates through the players in order, but some games may need to mess
     * with the turn from time to time. This should update the <code>_turnIdx</code> field, not set
     * the turn holder field in the game object directly.
     */
    protected void setNextTurnHolder ()
    {
        // stick with the current player if they're the only participant
        if (_tgmgr.getPlayerCount() <= 1) {
            return;
        }

        // find the next occupied active player slot
        int size = _turnGame.getPlayers().length;
        int oturnIdx = _turnIdx;
        do {
            _turnIdx = (_turnIdx + 1) % size;
            if (_turnIdx == oturnIdx) {
                // if we've wrapped all the way around, stop where we are even if the current
                // player is not active.
                log.warning("1 or less active players. Unable to properly change turn.",
                    "game", where());
                break;
            }
        } while (!_tgmgr.isActivePlayer(_turnIdx));
    }

    /**
     * Convenience function to randomly assign the turn.
     */
    protected void assignTurnRandomly ()
    {
        int size = _turnGame.getPlayers().length;
        if (size > 0) {
            int firstPick = _turnIdx = RandomUtil.getInt(size);
            while (!_tgmgr.isActivePlayer(_turnIdx)) {
                _turnIdx = (_turnIdx + 1) % size;
                if (_turnIdx == firstPick) {
                    log.warning("No players eligible for randomly-assigned turn. Choking.",
                        "game", where());
                    return;
                }
            }
        }
    }

    /** The game manager for which we are delegating. */
    protected TurnGameManager _tgmgr;

    /** A reference to our game object. */
    protected TurnGameObject _turnGame;

    /** The player index of the current turn holder or <code>-1</code> if it's no one's turn. */
    protected int _turnIdx = -1;
}
