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

package com.threerings.parlor.game.server;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.RepeatCallTracker;
import com.samskivert.util.Tuple;

import com.threerings.util.MessageBundle;
import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.AttributeChangedEvent;
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.NamedAttributeListener;

import com.threerings.crowd.chat.server.SpeakUtil;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.PlaceManager;
import com.threerings.crowd.server.PlaceManagerDelegate;

import com.threerings.parlor.data.ParlorCodes;
import com.threerings.parlor.game.data.GameAI;
import com.threerings.parlor.game.data.GameCodes;
import com.threerings.parlor.game.data.GameConfig;
import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.game.data.UserIdentifier;
import com.threerings.parlor.server.ParlorSender;
import com.threerings.parlor.server.PlayManager;

import static com.threerings.parlor.Log.log;

/**
 * The game manager handles the server side management of a game. It manipulates the game state in
 * accordance with the logic of the game flow and generally manages the whole game playing process.
 *
 * <p> The game manager extends the place manager because games are implicitly played in a
 * location, the players of the game implicitly bodies in that location.
 */
public class GameManager extends PlaceManager
    implements ParlorCodes, GameCodes, PlayManager
{
    /**
     * Returns the configuration object for the game being managed by this manager.
     */
    public GameConfig getGameConfig ()
    {
        return _gameconfig;
    }

    /**
     * Returns the unique numeric identifier for our managed game. See {@link GameConfig#getGameId}.
     */
    public int getGameId ()
    {
        return getGameConfig().getGameId();
    }

    /**
     * A convenience method for getting the game type.
     */
    public int getMatchType ()
    {
        return _gameconfig.getMatchType();
    }

    /**
     * Adds the given player to the game at the first available player index.  This should only be
     * called before the game is started, and is most likely to be used to add players to party
     * games.
     *
     * @param player the username of the player to add to this game.
     * @return the player index at which the player was added, or <code>-1</code> if the player
     * could not be added to the game.
     */
    public int addPlayer (Name player)
    {
        // determine the first available player index
        int pidx = -1;
        for (int ii = 0; ii < getPlayerSlots(); ii++) {
            if (!_gameobj.isOccupiedPlayer(ii)) {
                pidx = ii;
                break;
            }
        }

        // sanity-check the player index
        if (pidx == -1) {
            log.warning("Couldn't find free player index for player", "game", where(),
                        "player", player, "players", _gameobj.players);
            return -1;
        }

        // proceed with the rest of the adding business
        return (!addPlayerAt(player, pidx)) ? -1 : pidx;
    }

    /**
     * Adds the given player to the game at the specified player index.  This should only be called
     * before the game is started, and is most likely to be used to add players to party games.
     *
     * @param player the username of the player to add to this game.
     * @param pidx the player index at which the player is to be added.
     * @return true if the player was added successfully, false if not.
     */
    public boolean addPlayerAt (Name player, int pidx)
    {
        // make sure the specified player index is valid
        if (pidx < 0 || pidx >= getPlayerSlots()) {
            log.warning("Attempt to add player at an invalid index", "game", where(),
                        "player", player, "pidx", pidx);
            return false;
        }

        // make sure the player index is available
        if (_gameobj.players[pidx] != null) {
            log.warning("Attempt to add player at occupied index", "game", where(),
                        "player", player, "pidx", pidx);
            return false;
        }

        // make sure the player isn't already somehow a part of the game to avoid any potential
        // badness that might ensue if we added them more than once
        if (_gameobj.getPlayerIndex(player) != -1) {
            log.warning("Attempt to add player to game that they're already playing",
                        "game", where(), "player", player);
            return false;
        }

        // get the player's body object
        BodyObject bobj = _locator.lookupBody(player);
        if (bobj == null) {
            log.warning("Unable to get body object while adding player", "game", where(),
                        "player", player);
            return false;
        }

        // fill in the player's information
        _gameobj.setPlayersAt(player, pidx);

        // increment the number of players in the game
        _playerCount++;

        // save off their oid
        _playerOids[pidx] = bobj.getOid();

        // let derived classes do what they like
        playerWasAdded(player, pidx);

        return true;
    }

    /**
     * Removes the given player from the game.  This is most likely to be used to allow players
     * involved in a party game to leave the game early-on if they realize they'd rather not play
     * for some reason.
     *
     * @param player the username of the player to remove from this game.
     * @return true if the player was successfully removed, false if not.
     */
    public boolean removePlayer (Name player)
    {
        // get the player's index in the player list
        int pidx = _gameobj.getPlayerIndex(player);

        // sanity-check the player index
        if (pidx == -1) {
            log.warning("Attempt to remove non-player from players list", "game", where(),
                        "player", player, "players", _gameobj.players);
            return false;
        }

        // remove the player from the players list
        _gameobj.setPlayersAt(null, pidx);

        // clear out the player's entry in the player oid list
        _playerOids[pidx] = 0;

        if (_AIs != null) {
            // clear out the player's entry in the AI list
            _AIs[pidx] = null;
        }

        // decrement the number of players in the game
        _playerCount--;

        // let derived classes do what they like
        playerWasRemoved(player, pidx);

        return true;
    }

    /**
     * Replaces the player at the specified index and calls {@link #playerWasReplaced} to let
     * derived classes and delegates know what's going on.
     */
    public void replacePlayer (final int pidx, final Name player)
    {
        final Name oplayer = _gameobj.players[pidx];
        _gameobj.setPlayersAt(player, pidx);

        // allow derived classes to respond
        playerWasReplaced(pidx, oplayer, player);

        // notify our delegates
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).playerWasReplaced(pidx, oplayer, player);
            }
        });
    }

    /**
     * Returns the user object for the player with the specified index or null if the player at
     * that index is not online.
     */
    public BodyObject getPlayer (int playerIdx)
    {
        // if we have their oid, use that
        int ploid = _playerOids[playerIdx];
        if (ploid > 0) {
            return (BodyObject)_omgr.getObject(ploid);
        }
        // otherwise look them up by name
        Name name = getPlayerName(playerIdx);
        return (name == null) ? null : _locator.lookupBody(name);
    }

    /**
     * Sets the specified player as an AI with the specified configuration. It is assumed that this
     * will be set soon after the player names for all AIs present in the game. (It should be done
     * before human players start trickling into the game.)
     *
     * @param pidx the player index of the AI.
     * @param ai the AI configuration.
     */
    public void setAI (final int pidx, final GameAI ai)
    {
        if (_AIs == null) {
            // create and initialize the AI configuration array
            _AIs = new GameAI[getPlayerSlots()];
        }

        // save off the AI's configuration
        _AIs[pidx] = ai;

        // let the delegates know that the player's been made an AI
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).setAI(pidx, ai);
            }
        });
    }

    /**
     * Returns the name of the player with the specified index or null if no player exists at that
     * index.
     */
    public Name getPlayerName (int index)
    {
        return (_gameobj == null) ? null : _gameobj.players[index];
    }

    /**
     * Returns the name that should be shown in the client for the player with the specified index
     * or null if no player exists at that index. This may be different than their username as
     * returned by {@link #getPlayerName}, which is the player's unique name in the server.
     */
    public Name getPlayerDisplayName (int index)
    {
        return getPlayerName(index);
    }

    /**
     * Returns the player index of the given user in the game, or <code>-1</code> if the player is
     * not involved in the game.
     */
    public int getPlayerIndex (Name username)
    {
        return (_gameobj == null) ? -1 : _gameobj.getPlayerIndex(username);
    }

    /**
     * Get the player index of the specified oid, or -1 if the oid is not a player or is a player
     * that is not presently in the game.
     */
    public int getPresentPlayerIndex (int bodyOid)
    {
        return (_playerOids == null) ? -1 : IntListUtil.indexOf(_playerOids, bodyOid);
    }

    /**
     * Returns the user object oid of the player with the specified index.
     */
    public int getPlayerOid (int index)
    {
        return (_playerOids == null) ? -1 : _playerOids[index];
    }

    /**
     * Returns the persistent user id for the supplied player name.
     */
    public int getPlayerPersistentId (Name name)
    {
        return UserIdentifier.getUserId(name);
    }

    /**
     * Convenience for getting the persistent id from a body.
     */
    public int getPlayerPersistentId (BodyObject body)
    {
        return getPlayerPersistentId(body.getVisibleName());
    }

    /**
     * Returns the number of players in the game.
     */
    public int getPlayerCount ()
    {
        return _playerCount;
    }

    /**
     * Returns the number of players allowed in this game.
     */
    public int getPlayerSlots ()
    {
        return _gameconfig.players.length;
    }

    /**
     * Returns whether the player at the specified player index is an AI.
     */
    public boolean isAI (int pidx)
    {
        return (_AIs != null && _AIs[pidx] != null);
    }

    /**
     * Returns whether the player at the specified player index is actively playing the game
     */
    public boolean isActivePlayer (int pidx)
    {
        return _gameobj.isActivePlayer(pidx) && (getPlayerOid(pidx) > 0 || isAI(pidx));
    }

    /**
     * Returns the unique session identifier for this game session.
     */
    public int getSessionId ()
    {
        return _gameobj.sessionId;
    }

    /**
     * Sends a system message to the players in the game room.
     */
    public void systemMessage (String msgbundle, String msg)
    {
        systemMessage(msgbundle, msg, false);
    }

    /**
     * Sends a system message to the players in the game room.
     *
     * @param waitForStart if true, the message will not be sent until the game has started.
     */
    public void systemMessage (String msgbundle, String msg, boolean waitForStart)
    {
        if (waitForStart && ((_gameobj == null) || (_gameobj.state == GameObject.PRE_GAME))) {
            // queue up the message.
            if (_startmsgs == null) {
                _startmsgs = Lists.newArrayList();
            }
            _startmsgs.add(Tuple.newTuple(msgbundle, msg));
            return;
        }

        // otherwise, just deliver the message
        SpeakUtil.sendInfo(_gameobj, msgbundle, msg);
    }

    /**
     * This is called when the game is ready to start (all players involved have delivered their
     * "am ready" notifications). It calls {@link #gameWillStart}, sets the necessary wheels in
     * motion and then calls {@link #gameDidStart}.  Derived classes should override one or both of
     * the calldown functions (rather than this function) if they need to do things before or after
     * the game starts.
     *
     * @return true if the game was started, false if it could not be started because it was
     * already in play or because all players have not yet reported in.
     */
    public boolean startGame ()
    {
        // complain if we're already started
        if (_gameobj.state == GameObject.IN_PLAY) {
            log.warning("Requested to start an already in-play game", "game", where(),
                        new Exception());
            return false;
        }

        // TEMP: clear out our game end tracker
        _gameEndTracker.clear();

        // make sure everyone has turned up
        if (!allPlayersReady()) {
            log.warning("Requested to start a game that is still awaiting players",
                        "game", where(), "pnames", _gameobj.players, "poids", _playerOids);
            return false;
        }

        // if we're still waiting for a call to endGame() to propagate, queue up a runnable to
        // start the game which will allow the endGame() to propagate before we start things up
        if (_committedState == GameObject.IN_PLAY) {
            if (_postponedStart) {
                // We've already tried postponing once, doesn't do us any good to throw ourselves
                // into a frenzy trying again.
                log.warning("Tried to postpone the start of a still-ending game multiple times",
                            "game", where());
                _postponedStart = false;
                return false;
            }
            log.info("Postponing start of still-ending game", "game", where());
            _postponedStart = true;
            // TEMP: track down weirdness
            final Exception firstCall = new Exception();
            // End: temp
            _omgr.postRunnable(new Runnable() {
                public void run () {
                    boolean result = startGame();
                    // TEMP: track down weirdness
                    if (!result && !_postponedStart) {
                        log.warning("First call to startGame", "game", where(), firstCall);
                    }
                    // End: temp
                }
            });
            return true;
        }

        // Ah, good, not postponing.
        _postponedStart = false;

        // let the derived class do its pre-start stuff
        gameWillStart();

        // transition the game to started
        _gameobj.setState(GameObject.IN_PLAY);

        // when our events are applied, we'll call gameDidStart()
        return true;
    }

    /**
     * Ends the game for the given player.
     */
    public void endPlayerGame (int pidx)
    {
        // go for a little transactional efficiency
        _gameobj.startTransaction();
        try {
            // end the player's game
            if (_gameobj.playerStatus != null) {
                _gameobj.setPlayerStatusAt(GameObject.PLAYER_LEFT_GAME, pidx);
            }

            // let derived classes do some business
            playerGameDidEnd(pidx);

        } finally {
            _gameobj.commitTransaction();
        }

        // if it's time to end the game, then do so
        if (shouldEndGame()) {
            endGame();
        } else {
            // otherwise report that the player was knocked out to other people in his/her room
            reportPlayerKnockedOut(pidx);
        }
    }

    /**
     * Called when the game is known to be over. This will call some calldown functions to
     * determine the winner of the game and then transition the game to the {@link
     * GameObject#GAME_OVER} state.
     */
    public void endGame ()
    {
        // TEMP: debug pending rating repeat bug
        if (_gameEndTracker.checkCall(
                "Requested to end already ended game [game=" + where() + "].")) {
            return;
        }
        // END TEMP

        if (!_gameobj.isInPlay()) {
            log.info("Refusing to end game that was not in play", "game", where());
            return;
        }

        _gameobj.startTransaction();
        try {
            // let the derived class do its pre-end stuff
            gameWillEnd();

            // determine winners and set them in the game object
            boolean[] winners = new boolean[getPlayerSlots()];
            assignWinners(winners);
            _gameobj.setWinners(winners);

            // transition to the game over state
            _gameobj.setState(GameObject.GAME_OVER);

        } finally {
            _gameobj.commitTransaction();
        }

        // wait until we hear the game state transition on the game object to invoke our game over
        // code so that we can be sure that any final events dispatched on the game object prior to
        // the call to endGame() have been dispatched
    }

    /**
     * Sets the state of the game to {@link GameObject#CANCELLED}.
     *
     * @return true if the game was cancelled, false if it was already over or cancelled.
     */
    public boolean cancelGame ()
    {
        if (_gameobj.state != GameObject.GAME_OVER && _gameobj.state != GameObject.CANCELLED) {
            _gameobj.setState(GameObject.CANCELLED);
            return true;
        }
        return false;
    }

    /**
     * Returns whether game conclusion antics such as rating updates should be performed when an
     * in-play game is ended.  Derived classes may wish to override this method to customize the
     * conditions under which the game is concluded.
     */
    public boolean shouldConcludeGame ()
    {
        return (_gameobj.state == GameObject.GAME_OVER);
    }

    /**
     * Called when the game is to be reset to its starting state in preparation for a new game
     * without actually ending the current game. It calls {@link #gameWillReset} followed by the
     * standard game start processing ({@link #gameWillStart} and {@link #gameDidStart}). Derived
     * classes should override these calldown functions (rather than this function) if they need to
     * do things before or after the game resets.
     */
    public void resetGame ()
    {
        // let the derived class do its pre-reset stuff
        gameWillReset();
        // do the standard game start processing
        gameWillStart();
        // transition to in-play which will trigger a call to gameDidStart()
        _gameobj.setState(GameObject.IN_PLAY);
    }

    /**
     * Called by the client when an occupant has arrived in the game room and has loaded their
     * bits. Most games will simply call {@link #playerReady} but games that wish to delay their
     * actual start until players take some action must report ASAP with a call to {@link
     * #occupantInRoom} to let the server know that they have arrived and will later be calling
     * {@link #playerReady} when they are ready for the game to actually start.
     */
    public void occupantInRoom (BodyObject caller)
    {
        int pidx = _gameobj.getPlayerIndex(caller.getVisibleName());
        if (pidx == -1) {
            // in general, we want all occupants to call this, but here in this base class
            // we only care about players
            return;
        }

        // make a note of this player's oid
        _playerOids[pidx] = caller.getOid();

        // this player is not necessarily ready to play yet
        _pendingOids.add(caller.getOid());
    }

    /**
     * Called by the client when the player is ready for the game to start.  This method is
     * dispatched dynamically by {@link PlaceManager#messageReceived}.
     */
    public void playerReady (BodyObject caller)
    {
        occupantInRoom(caller);

        // This player is no longer pending
        _pendingOids.remove(caller.getOid());

        // if everyone is now ready to go, get things underway
        if (allPlayersReady()) {
            playersAllHere();
        }
    }

    /**
     * Returns true if all (non-AI) players have delivered their {@link #playerReady}
     * notifications, false if they have not.
     */
    public boolean allPlayersReady ()
    {
        for (int ii = 0; ii < getPlayerSlots(); ii++) {
            if (!playerIsReady(ii)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the player at the specified slot is ready (or if there is meant to be no
     * player in that slot), false if there is meant to be a player in the specified slot and they
     * have not yet reported that they are ready.
     */
    public boolean playerIsReady (int pidx)
    {
        return (!_gameobj.isOccupiedPlayer(pidx) ||            // unoccupied slot
                (_playerOids[pidx] != 0 &&                     // player is in the room and...
                 !_pendingOids.contains(_playerOids[pidx])) || // ...has reported ready
                isAI(pidx));                                   // player is AI
    }

    // from PlayManager
    public boolean isPlayer (ClientObject client)
    {
        // players must have bodies
        if (client != null && client instanceof BodyObject) {
            BodyObject body = (BodyObject) client;

            // players must be occupants
            if (_gameobj.occupants.contains(body.getOid())) {

                // in a party game, all occupants are players
                if (getGameConfig().getMatchType() == GameConfig.PARTY) {
                    return true;
                }

                // else they must be seated
                return _gameobj.getPlayerIndex(body.getVisibleName()) >= 0;
            }
        }
        return false;
    }

    // from PlayManager
    public boolean isAgent (ClientObject client)
    {
        // agent-savvy subclasses override this
        return false;
    }

    // from PlayManager
    public BodyObject checkWritePermission (ClientObject client, int playerId)
    {
        // subclasses can be more restrictive here
        DObject player = _omgr.getObject(playerId);
        return (player instanceof BodyObject) ? (BodyObject) player : null;
    }

    @Override
    protected boolean allowManagerCall (String method)
    {
        return "playerReady".equals(method) || super.allowManagerCall(method);
    }

    /**
     * Returns true if this game requires a no-show timer. The default implementation returns true
     * for non-party games and false for party games. Derived classes may wish to change or augment
     * this behavior.
     */
    protected boolean needsNoShowTimer ()
    {
        return (getMatchType() == GameConfig.SEATED_GAME);
    }

    /**
     * Returns the time after which we consider any player that has not yet reported into the game
     * as a no-show and try to start the game anyway.
     */
    protected long getNoShowTime ()
    {
        return DEFAULT_NOSHOW_DELAY;
    }

    /**
     * Derived classes that need their AIs to be ticked periodically should override this method
     * and return true. Many AIs can act entirely in reaction to game state changes and need no
     * periodic ticking which is why ticking is disabled by default.
     *
     * @see #tickAIs
     */
    protected boolean needsAITick ()
    {
        return false;
    }

    /**
     * Called when a player was added to the game.  Derived classes may override this method to
     * perform any game-specific actions they desire, but should be sure to call
     * <code>super.playerWasAdded()</code>.
     *
     * @param player the username of the player added to the game.
     * @param pidx the player index of the player added to the game.
     */
    protected void playerWasAdded (Name player, int pidx)
    {
    }

    /**
     * Called when a player was removed from the game.  Derived classes may override this method to
     * perform any game-specific actions they desire, but should be sure to call
     * <code>super.playerWasRemoved()</code>.
     *
     * @param player the username of the player removed from the game.
     * @param pidx the player index of the player before they were removed from the game.
     */
    protected void playerWasRemoved (Name player, int pidx)
    {
    }

    /**
     * Called when a player has been replaced via a call to {@link #replacePlayer}.
     */
    protected void playerWasReplaced (int pidx, Name oldPlayer, Name newPlayer)
    {
    }

    /**
     * Report to the knocked-out player's room that they were knocked out.
     */
    protected void reportPlayerKnockedOut (int pidx)
    {
        BodyObject user = getPlayer(pidx);
        if (user == null) {
            return; // body object can be null for ai players
        }

        DObject place = _omgr.getObject(user.getPlaceOid());
        if (place != null) {
            place.postMessage(PLAYER_KNOCKED_OUT, new Object[] { new int[] { user.getOid() } });
        }
    }

    @Override
    protected void didInit ()
    {
        super.didInit();

        // save off a casted reference to our config
        _gameconfig = (GameConfig)_config;

        // start up our tick interval
        (_tickInterval = _omgr.newInterval(new Runnable() {
            public void run () {
                tick(System.currentTimeMillis());
            }
        })).schedule(TICK_DELAY, true);

        // configure our AIs
        for (int ii = 0; ii < _gameconfig.ais.length; ii++) {
            if (_gameconfig.ais[ii] != null) {
                setAI(ii, _gameconfig.ais[ii]);
            }
        }
    }

    @Override
    protected void didStartup ()
    {
        // obtain a casted reference to our game object
        _gameobj = (GameObject)_plobj;
        _gameobj.addListener(_stateListener);

        // stick the players into the game object
        _gameobj.setPlayers(_gameconfig.players);

        // set up an initial player status array
        _gameobj.setPlayerStatus(new int[getPlayerSlots()]);

        // save off the number of players so that we needn't repeatedly iterate through the player
        // name array server-side unnecessarily
        _playerCount = _gameobj.getPlayerCount();

        // instantiate a player oid array which we'll fill in later
        _playerOids = new int[getPlayerSlots()];

        // give delegates a chance to do their thing
        super.didStartup();

        // let the players of this game know that we're ready to roll (if we have a specific set of
        // players)
        for (int ii = 0; ii < getPlayerSlots(); ii++) {
            // skip non-existent players and AIs
            if (!_gameobj.isOccupiedPlayer(ii) || isAI(ii)) {
                continue;
            }

            BodyObject bobj = _locator.lookupBody(_gameobj.players[ii]);
            if (bobj == null) {
                log.warning("Unable to deliver game ready to non-existent player",
                            "game", where(), "player", _gameobj.players[ii]);
                continue;
            }

            // deliver a game ready notification to the player
            ParlorSender.gameIsReady(bobj, _gameobj.getOid());
        }

        // start up a no-show timer if needed
        if (needsNoShowTimer()) {
            (_noShowInterval = new Interval(_omgr) {
                @Override
                public void expired () {
                    checkForNoShows();
                }
            }).schedule(getNoShowTime());
        }
    }

    @Override
    protected void didShutdown ()
    {
        super.didShutdown();

        // shutdown our tick interval
        _tickInterval.cancel();
        _tickInterval = null;

        if (_gameobj != null) {
            // remove our state listener
            _gameobj.removeListener(_stateListener);
        }
    }

    @Override
    protected void bodyLeft (int bodyOid)
    {
        // first resign the player from the game
        int pidx = IntListUtil.indexOf(_playerOids, bodyOid);
        if (pidx != -1 && _gameobj.isInPlay() &&
            _gameobj.isActivePlayer(pidx)) {
            // end the player's game if they bail on an in-progress game
            endPlayerGame(pidx);
        } else if (pidx != -1 && _gameobj.state == GameObject.PRE_GAME) {
            // Don't need to stop their game, since it isn't going, but DO need to register that
            //  they've left the building.
            _playerOids[pidx] = 0;
        }

        // then complete the bodyLeft() processing which may result in a call to placeBecameEmpty()
        // which will shut the game down
        super.bodyLeft(bodyOid);
    }

    /**
     * When a game room becomes empty, we cancel the game if it's still in progress and close down
     * the game room.
     */
    @Override
    protected void placeBecameEmpty ()
    {
        super.placeBecameEmpty();

//         log.info("Game room empty. Going away.", "game", where());

        // if we're in play then move to game over
        if (_gameobj.state != GameObject.PRE_GAME && _gameobj.state != GameObject.GAME_OVER &&
                _gameobj.state != GameObject.CANCELLED) {
            _gameobj.setState(GameObject.GAME_OVER);
            shutdown(); // and shutdown directly
            return;
        }

        // otherwise, cancel the game; which will shut us down
        if (cancelGame()) {
            return;
        }

        // if we couldn't cancel (because the game was already over) shutdown directly
        shutdown();
    }

    /**
     * Called when all players have arrived in the game room. By default, this starts up the game,
     * but a manager may wish to override this and start the game according to different criterion.
     */
    protected void playersAllHere ()
    {
        // if we're a seated game and we haven't already started, start.
        if ((getMatchType() == GameConfig.SEATED_GAME) && _gameobj.state == GameObject.PRE_GAME) {
            startGame();
        }
    }

    @Override
    protected void checkShutdownInterval ()
    {
        // PlaceManager will attempt to set up an idle shutdown interval when it is first created
        // (which as a GameManager we want) and if bodies actually enter the place and then it once
        // again becomes empty. In the latter case (a game has started and finished and everyone
        // has now left) we do not want a shutdown interval because we shut ourselves down
        // immediately in that circumstance. So we only set up a shutdown interval in the pre-game
        // state.
        if (_gameobj.state == GameObject.PRE_GAME) {
            super.checkShutdownInterval();
        }
    }

    /**
     * Called after the no-show delay has expired following the delivery of notifications to all
     * players that the game is ready.  <em>Note:</em> this is not called for party games. Those
     * games have a human who decides when to start the game.
     */
    protected void checkForNoShows ()
    {
        // nothing to worry about if we're already started
        if (_gameobj.state != GameObject.PRE_GAME) {
            return;
        }

        // if there's no one in the room, go ahead and clear it out
        if (_plobj.occupants.size() == 0) {
            log.info("Cancelling total no-show", "game", where(), "players", _gameobj.players,
                     "poids", _playerOids);
            placeBecameEmpty();

        } else {
            // do the right thing if we have any no-show players
            for (int ii = 0; ii < getPlayerSlots(); ii++) {
                if (!playerIsReady(ii)) {
                    handlePartialNoShow();
                    return;
                }
            }
        }
    }

    /**
     * This is called when some, but not all, players failed to show up for a game. The default
     * implementation simply cancels the game.
     */
    protected void handlePartialNoShow ()
    {
        // mark the no-show players; this will cause allPlayersReady() to think that everyone has
        // arrived, but still allow us to tell who has not shown up in gameDidStart()
        int humansHere = 0;
        for (int ii = 0; ii < _playerOids.length; ii++) {
            if (_playerOids[ii] == 0) {
                _playerOids[ii] = -1;
            } else if (!isAI(ii)) {
                humansHere++;
            }
        }

        if ((humansHere == 0) && !startWithoutHumans()) {
            // if there are no human players in the game, just cancel it
            log.info("Canceling no-show game", "game", where(), "players", _playerOids);
            cancelGame();

        } else {
            // go ahead and report that everyone is ready (which will start the game);
            // gameDidStart() will take care of giving the boot to anyone who isn't around
            log.info("Forcing start of partial no-show game", "game", where(),
                     "players", _playerOids);
            playersAllHere();
        }
    }

    /**
     * @return true if we should start the game even without any humans.  Default implementation
     * always returns false.
     */
    protected boolean startWithoutHumans ()
    {
        return false;
    }

    /**
     * Called when the game is about to start, but before the game start notification has been
     * delivered to the players. Derived classes should override this if they need to perform some
     * pre-start activities, but should be sure to call <code>super.gameWillStart()</code>.
     */
    protected void gameWillStart ()
    {
        // update our session id
        _gameobj.setSessionId(_gameobj.sessionId + 1);

        // let our delegates do their business
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameWillStart();
            }
        });
    }

    /**
     * Called when the game state changes. This happens after the attribute change event has
     * propagated.
     *
     * @param state the new game state.
     * @param oldState the previous game state.
     */
    protected void stateDidChange (int state, int oldState)
    {
        switch (state) {
        case GameObject.IN_PLAY:
            gameDidStart();
            break;

        case GameObject.GAME_OVER:
            // we do some jiggery pokery to allow derived game objects to have different notions of
            // what it means to be in play
            _gameobj.state = oldState;
            boolean wasInPlay = _gameobj.isInPlay();
            _gameobj.state = state;

            // now call gameDidEnd() only if the game was previously in play
            if (wasInPlay) {
                gameDidEnd();
            }
            break;

        case GameObject.CANCELLED:
            // let the manager do anything it cares to
            gameWasCancelled();

            // and shutdown if there's no one here
            if (_plobj.occupants.size() == 0) {
                shutdown();
            }
            break;
        }
    }

    /**
     * Called after the game start notification was dispatched.  Derived classes can override this
     * to put whatever wheels they might need into motion now that the game is started (if anything
     * other than transitioning the game to {@link GameObject#IN_PLAY} is necessary), but should be
     * sure to call <code>super.gameDidStart()</code>.
     */
    protected void gameDidStart ()
    {
        // clear out our no-show timer if it's still running
        if (_noShowInterval != null) {
            _noShowInterval.cancel();
        }

        // let our delegates do their business
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameDidStart();
            }
        });

        // inform the players of any pending messages.
        if (_startmsgs != null) {
            for (Tuple<String,String> mtup : _startmsgs) {
                systemMessage(mtup.left, /* bundle */ mtup.right /* message */);
            }
            _startmsgs = null;
        }

        // and potentially register ourselves to receive AI ticks
        if (_AIs != null && needsAITick()) {
            startAITicker();
        }

        // any players who have not claimed that they are ready should now be given le boote royale
        for (int ii = 0; ii < _playerOids.length; ii++) {
            if (_playerOids[ii] == -1) {
                log.info("Booting no-show player", "game", where(), "player", getPlayerName(ii));
                _playerOids[ii] = 0; // unfiddle the blank oid
                endPlayerGame(ii);
            }
        }
    }

    /**
     * Starts our AI ticker if it is not already started.
     */
    protected void startAITicker ()
    {
        if (_aiTicker == null) {
            (_aiTicker = _omgr.newInterval(new Runnable() {
                public void run () {
                    tickAIs();
                }
            })).schedule(AI_TICK_DELAY, true);
        }
    }

    /**
     * Stops our AI ticker if it's running.
     */
    protected void stopAITicker ()
    {
        if (_aiTicker != null) {
            _aiTicker.cancel();
            _aiTicker = null;
        }
    }

    /**
     * Called by the AI ticker if we're registered as an AI game.
     */
    protected void tickAIs ()
    {
        for (int ii = 0; ii < _AIs.length; ii++) {
            if (_AIs[ii] != null) {
                tickAI(ii, _AIs[ii]);
            }
        }
    }

    /**
     * Called by {@link #tickAIs} to tick each AI in the game.
     */
    protected void tickAI (final int pidx, final GameAI ai)
    {
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate) delegate).tickAI(pidx, ai);
            }
        });
    }

    /**
     * Announce to everyone in the game that a player's game has ended.
     */
    protected void announcePlayerGameOver (int pidx)
    {
        systemMessage(GAME_MESSAGE_BUNDLE,
            MessageBundle.tcompose(getPlayerGameOverMessage(pidx), getPlayerDisplayName(pidx)));
    }

    /**
     * Gets the untranslated string to show when a player's game has ended.
     */
    protected String getPlayerGameOverMessage (int pidx)
    {
        return "m.player_game_over";
    }

    /**
     * Called when a player has been marked as knocked out but before the knock-out status update
     * has been sent to the players. Any status information that needs be updated in light of the
     * knocked out player can be updated here.
     */
    protected void playerGameDidEnd (int pidx)
    {
        // report that the player's game is over to anyone still in the game room
        announcePlayerGameOver(pidx);
    }

    /**
     * Called when a player leaves the game in order to determine whether the game should be ended
     * based on its current state, which will include updated player status for the player in
     * question.  The default implementation returns true if the game is in play and there is only
     * one player left.  Derived classes may wish to override this method in order to customize the
     * required end-game conditions.
     */
    protected boolean shouldEndGame ()
    {
        return (_gameobj.isInPlay() && _gameobj.getActivePlayerCount() == 1);
    }

    /**
     * Assigns the final winning status for each player to their respect player index in the
     * supplied array.  This will be called by {@link #endGame} when the game is over.  The default
     * implementation marks no players as winners.  Derived classes should override this method in
     * order to customize the winning conditions.
     */
    protected void assignWinners (boolean[] winners)
    {
        Arrays.fill(winners, false);
    }

    /**
     * Called when the game is about to end, but before the game end notification has been
     * delivered to the players.  Derived classes should override this if they need to perform some
     * pre-end activities, but should be sure to call <code>super.gameWillEnd()</code>.
     */
    protected void gameWillEnd ()
    {
        // let our delegates do their business
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameWillEnd();
            }
        });
    }

    /**
     * Called after the game has transitioned to the {@link GameObject#GAME_OVER} state. Derived
     * classes should override this to perform any post-game activities, but should be sure to call
     * <code>super.gameDidEnd()</code>.
     */
    protected void gameDidEnd ()
    {
        // remove ourselves from the AI ticker, if applicable
        stopAITicker();

        // let our delegates do their business
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameDidEnd();
            }
        });

        // clear out player readiness; everyone must report as ready again to restart the game
        Arrays.fill(_playerOids, 0);
        _pendingOids.clear();

        // report the winners and losers if appropriate
        int winnerCount = _gameobj.getWinnerCount();
        if (shouldConcludeGame() && winnerCount > 0 && !_gameobj.isDraw()) {
            reportWinnersAndLosers();
        }

        // calculate ratings and all that...
    }

    /**
     * Called to let the manager know that the game was cancelled (and may be about to be shutdown
     * if there's no one in the room). In the base framework a game will only be canceled if no one
     * shows up, so {@link #gameWillStart}, etc. will never have been called and thus {@link
     * #gameWillEnd}, etc. will not be called. However, if a game chooses to cancel itself for
     * whatever reason, no effort will be made to call {@link #endGame} and the game ending call
     * backs so that game can override this method to do anything it needs. Note that {@link
     * #didShutdown} will be called in every case and that's generally the best place to free
     * resources so this method may not be needed.
     */
    protected void gameWasCancelled ()
    {
        // nothing to do by default
        stopAITicker();
    }

    /**
     * Report winner and loser oids to each room that any of the winners/losers is in.
     */
    protected void reportWinnersAndLosers ()
    {
        int numPlayers = _playerOids.length;

        // set up 3 sets that will not need internal expanding
        ArrayIntSet winners = new ArrayIntSet(numPlayers);
        ArrayIntSet losers = new ArrayIntSet(numPlayers);
        ArrayIntSet places = new ArrayIntSet(numPlayers);

        for (int ii=0; ii < numPlayers; ii++) {
            BodyObject user = getPlayer(ii);
            if (user != null) {
                places.add(user.getPlaceOid());
                (_gameobj.isWinner(ii) ? winners : losers).add(user.getOid());
            }
        }

        Object[] args = new Object[] { winners.toIntArray(), losers.toIntArray() };

        // now send a message event to each room
        for (int ii=0, nn = places.size(); ii < nn; ii++) {
            DObject place = _omgr.getObject(places.get(ii));
            if (place != null) {
                place.postMessage(WINNERS_AND_LOSERS, args);
            }
        }
    }

    /**
     * Called when the game is about to reset, but before the board has been re-initialized or any
     * other clearing out of game data has taken place.  Derived classes should override this if
     * they need to perform some pre-reset activities.
     */
    protected void gameWillReset ()
    {
        // reinitialize the player status
        _gameobj.setPlayerStatus(new int[getPlayerSlots()]);

        // let our delegates do their business
        applyToDelegates(new DelegateOp(GameManagerDelegate.class) {
            @Override
            public void apply (PlaceManagerDelegate delegate) {
                ((GameManagerDelegate)delegate).gameWillReset();
            }
        });
    }

    /**
     * Gives game managers an opportunity to perform periodic processing that is not driven by
     * events generated by the player.
     */
    protected void tick (long tickStamp)
    {
        // nothing for now
    }

    /** Listens for game state changes. */
    protected NamedAttributeListener _stateListener = new NamedAttributeListener(GameObject.STATE) {
        @Override
        public void namedAttributeChanged (AttributeChangedEvent event) {
            stateDidChange(_committedState = event.getIntValue(),
                           ((Integer)event.getOldValue()).intValue());
        }
    };

    /** A reference to our game config. */
    protected GameConfig _gameconfig;

    /** A reference to our game object. */
    protected GameObject _gameobj;

    /** The number of players in the game. */
    protected int _playerCount;

    /** The oids of our player and AI body objects. */
    protected int[] _playerOids;

    /** The list of players that have arrived in the room, but are not ready to play. */
    protected ArrayIntSet _pendingOids = new ArrayIntSet();

    /** If AIs are present, contains their configuration, or null at human player indexes. */
    protected GameAI[] _AIs;

    /** If non-null, contains bundles and messages that should be sent as system messages once the
     * game has started. */
    protected List<Tuple<String,String>> _startmsgs;

    /** The state of the game that has been propagated to our subscribers. */
    protected int _committedState;

    /** TEMP: debugging the pending rating double release bug. */
    protected RepeatCallTracker _gameEndTracker = new RepeatCallTracker();

    /** The interval used to check for no-shows. */
    protected Interval _noShowInterval;

    /** Whether we have already postponed the start of the game. */
    protected boolean _postponedStart = false;

    /** The interval for the game manager tick. */
    protected Interval _tickInterval;

    /** The interval for the AI tick. */
    protected Interval _aiTicker;

    /** The default value returned by {@link #getNoShowTime}. */
    protected static final long DEFAULT_NOSHOW_DELAY = 30 * 1000L;

    /** The delay in milliseconds between ticking of all game managers. */
    protected static final long TICK_DELAY = 5L * 1000L;

    /** The frequency with which we dispatch AI game ticks. */
    protected static final long AI_TICK_DELAY = 3333L; // every 3 1/3 seconds
}
