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

package com.threerings.ezgame.server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

import com.samskivert.util.ArrayIntSet;
import com.samskivert.util.ArrayUtil;
import com.samskivert.util.CollectionUtil;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.Interval;
import com.samskivert.util.IntListUtil;
import com.samskivert.util.RandomUtil;
import com.samskivert.util.ResultListener;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.DSet;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.OccupantInfo;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.game.data.GameConfig;

import com.threerings.parlor.game.server.GameManager;

import com.threerings.parlor.turn.server.TurnGameManager;

import com.threerings.ezgame.data.EZGameMarshaller;
import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.PropertySetEvent;
import com.threerings.ezgame.data.UserCookie;

import static com.threerings.ezgame.server.Log.log;

/**
 * A manager for "ez" games.
 */
public class EZGameManager extends GameManager
    implements EZGameProvider, TurnGameManager
{
    public EZGameManager ()
    {
        addDelegate(_turnDelegate = new EZGameTurnDelegate(this));
    }

    /**
     * Configures the oids of the winners of this game. If a game manager delegate wishes to handle
     * winner assignment, it should call this method and then call {@link #enddGame}.
     */
    public void setWinners (int[] winnerOids)
    {
        _winnerOids = winnerOids;
    }

    // from TurnGameManager
    public void turnWillStart ()
    {
    }

    // from TurnGameManager
    public void turnDidStart ()
    {
    }

    // from TurnGameManager
    public void turnDidEnd ()
    {
    }

    // from EZGameProvider
    public void endTurn (ClientObject caller, int nextPlayerId,
                         InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateStateModification(caller, true);

        Name nextTurnHolder = null;
        if (nextPlayerId != 0) {
            BodyObject target = getPlayerByOid(nextPlayerId);
            if (target != null) {
                nextTurnHolder = target.getVisibleName();
            }
        }

        _turnDelegate.endTurn(nextTurnHolder);
    }

    // from EZGameProvider
    public void endRound (ClientObject caller, int nextRoundDelay,
                          InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateStateModification(caller, false);

        // let the game know that it is doing something stupid
        if (_ezObj.roundId < 0) {
            throw new InvocationException("m.round_already_ended");
        }

        // while we are between rounds, our round id is the negation of the round that just ended
        _ezObj.setRoundId(-_ezObj.roundId);

        // queue up the start of the next round if requested
        if (nextRoundDelay > 0) {
            new Interval(CrowdServer.omgr) {
                public void expired () {
                    if (_ezObj.isInPlay()) {
                        _ezObj.setRoundId(-_ezObj.roundId + 1);
                    }
                }
            }.schedule(nextRoundDelay * 1000L);
        }
    }

    // from EZGameProvider
    public void endGame (ClientObject caller, int[] winnerOids,
                         InvocationService.InvocationListener listener)
        throws InvocationException
    {
        if (!_ezObj.isInPlay()) {
            throw new InvocationException("e.already_ended");
        }
        validateStateModification(caller, false);

        setWinners(winnerOids);
        endGame();
    }

    // from EZGameProvider
    public void restartGameIn (ClientObject caller, int seconds,
                               InvocationService.InvocationListener listener)
        throws InvocationException
    {
        if (_ezObj.isInPlay()) {
            throw new InvocationException("e.game_in_play");
        }
        validateStateModification(caller, false);

        // queue up the start of the next game
        if (seconds > 0) {
            new Interval(CrowdServer.omgr) {
                public void expired () {
                    if (_ezObj.isActive() && !_ezObj.isInPlay()) {
                        startGame();
                    }
                }
            }.schedule(seconds * 1000L);
        }
    }

    // from EZGameProvider
    public void sendMessage (ClientObject caller, String msg, Object data, int playerId,
                             InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);

        if (playerId == 0) {
            _ezObj.postMessage(EZGameObject.USER_MESSAGE, msg, data);
        } else {
            sendPrivateMessage(playerId, msg, data);
        }
    }

    // from EZGameProvider
    public void setProperty (ClientObject caller, String propName, Object data, int index,
                             boolean testAndSet, Object testValue,
                             InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);
        if (testAndSet && !_ezObj.testProperty(propName, index, testValue)) {
            return; // the test failed: do not set the property
        }
        setProperty(propName, data, index);
    }

    // from EZGameProvider
    public void getDictionaryLetterSet (ClientObject caller, String locale, int count, 
                                        InvocationService.ResultListener listener)
        throws InvocationException
    {
        getDictionaryManager().getLetterSet(locale, count, listener);
    }
    
    // from EZGameProvider
    public void checkDictionaryWord (ClientObject caller, String locale, String word, 
                                     InvocationService.ResultListener listener)
        throws InvocationException
    {
        getDictionaryManager().checkWord(locale, word, listener);
    }  

    /**
     * Returns the dictionary manager if it has been properly initialized. Throws an INTERNAL_ERROR
     * exception if it has not.
     */
    protected DictionaryManager getDictionaryManager ()
        throws InvocationException
    {
        DictionaryManager dictionary = DictionaryManager.getInstance();
        if (dictionary == null) {
            log.warning("DictionaryManager not initialized.");
            throw new InvocationException(INTERNAL_ERROR);
        }
        return dictionary;
    }

    // from EZGameProvider
    public void addToCollection (ClientObject caller, String collName, byte[][] data,
                                 boolean clearExisting,
                                 InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);
        if (_collections == null) {
            _collections = new HashMap<String, ArrayList<byte[]>>();
        }

        // figure out if we're adding to an existing collection or creating a new one
        ArrayList<byte[]> list = null;
        if (!clearExisting) {
            list = _collections.get(collName);
        }
        if (list == null) {
            list = new ArrayList<byte[]>();
            _collections.put(collName, list);
        }

        CollectionUtil.addAll(list, data);
    }

    // from EZGameProvider
    public void getFromCollection (ClientObject caller, String collName, boolean consume, int count,
                                   String msgOrPropName, int playerId,
                                   InvocationService.ConfirmListener listener)
        throws InvocationException
    {
        validateUser(caller);

        int srcSize = 0;
        if (_collections != null) {
            ArrayList<byte[]> src = _collections.get(collName);
            srcSize = (src == null) ? 0 : src.size();
            if (srcSize >= count) {
                byte[][] result = new byte[count][];
                for (int ii=0; ii < count; ii++) {
                    int pick = RandomUtil.getInt(srcSize);
                    if (consume) {
                        result[ii] = src.remove(pick);
                        srcSize--;

                    } else {
                        result[ii] = src.get(pick);
                    }
                }

                if (playerId == 0) {
                    setProperty(msgOrPropName, result, -1);
                } else {
                    sendPrivateMessage(playerId, msgOrPropName, result);
                }
                listener.requestProcessed(); // SUCCESS!
                return;
            }
        }
        
        // TODO: decide what we want to return here
        throw new InvocationException(String.valueOf(srcSize));
    }
    
    // from EZGameProvider
    public void mergeCollection (ClientObject caller, String srcColl, String intoColl,
                                 InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);

        // non-existent collections are treated as empty, so if the source doesn't exist, we
        // silently accept it
        if (_collections != null) {
            ArrayList<byte[]> src = _collections.remove(srcColl);
            if (src != null) {
                ArrayList<byte[]> dest = _collections.get(intoColl);
                if (dest == null) {
                    _collections.put(intoColl, src);
                } else {
                    dest.addAll(src);
                }
            }
        }
    }

    // from EZGameProvider
    public void setTicker (ClientObject caller, String tickerName, int msOfDelay,
                           InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);

        Ticker t;
        if (msOfDelay >= MIN_TICKER_DELAY) {
            if (_tickers != null) {
                t = _tickers.get(tickerName);
            } else {
                _tickers = new HashMap<String, Ticker>();
                t = null;
            }

            if (t == null) {
                if (_tickers.size() >= MAX_TICKERS) {
                    throw new InvocationException(ACCESS_DENIED);
                }
                t = new Ticker(tickerName, _ezObj);
                _tickers.put(tickerName, t);
            }
            t.start(msOfDelay);

        } else if (msOfDelay <= 0) {
            if (_tickers != null) {
                t = _tickers.remove(tickerName);
                if (t != null) {
                    t.stop();
                }
            }

        } else {
            throw new InvocationException(ACCESS_DENIED);
        }
    }

    // from EZGameProvider
    public void getCookie (ClientObject caller, final int playerOid,
                           InvocationService.InvocationListener listener)
        throws InvocationException
    {
        if (_ezObj.userCookies != null && _ezObj.userCookies.containsKey(playerOid)) {
            // already loaded: we do nothing
            return;
        }

        // we only start looking up the cookie if nobody else already is
        if (_cookieLookups.contains(playerOid)) {
            return;
        }

        BodyObject body = getOccupantByOid(playerOid);
        if (body == null) {
            log.fine("getCookie() called with invalid occupant [occupantId=" + playerOid + "].");
            throw new InvocationException(INTERNAL_ERROR);
        }

        // indicate that we're looking up a cookie
        _cookieLookups.add(playerOid);

        int ppId = getPlayerPersistentId(body);
        getCookieManager().getCookie(_gameconfig.getGameId(), ppId, new ResultListener<byte[]>() {
            public void requestCompleted (byte[] result) {
                // note that we're done with this lookup
                _cookieLookups.remove(playerOid);
                // result may be null: that's ok, it means we've looked up the user's nonexistent
                // cookie; also only set the cookie if the player is still in the room
                if (_ezObj.occupants.contains(playerOid) && _ezObj.isActive()) {
                    _ezObj.addToUserCookies(new UserCookie(playerOid, result));
                }
            }

            public void requestFailed (Exception cause) {
                log.warning("Unable to retrieve cookie [cause=" + cause + "].");
                requestCompleted(null);
            }
        });
    }

    // from EZGameProvider
    public void setCookie (ClientObject caller, byte[] value,
                           InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);

        // persist this new cookie
        getCookieManager().setCookie(
            _gameconfig.getGameId(), getPlayerPersistentId((BodyObject)caller), value);

        // and update the distributed object
        UserCookie cookie = new UserCookie(caller.getOid(), value);
        if (_ezObj.userCookies.containsKey(cookie.getKey())) {
            _ezObj.updateUserCookies(cookie);
        } else {
            _ezObj.addToUserCookies(cookie);
        }
    }

    /**
     * Helper method to send a private message to the specified player oid (must already be
     * verified).
     */
    protected void sendPrivateMessage (int playerOid, String msg, Object data)
        throws InvocationException
    {
        BodyObject target = getPlayerByOid(playerOid);
        if (target == null) {
            // TODO: this code has no corresponding translation
            throw new InvocationException("m.player_not_around");
        }

        target.postMessage(EZGameObject.USER_MESSAGE + ":" + _ezObj.getOid(),
                           new Object[] { msg, data });
    }

    /**
     * Helper method to post a property set event.
     */
    protected void setProperty (String propName, Object value, int index)
    {
        // apply the property set immediately
        Object oldValue = _ezObj.applyPropertySet(propName, value, index);
        _ezObj.postEvent(
            new PropertySetEvent(_ezObj.getOid(), propName, value, index, oldValue));
    }

    /**
     * Validate that the specified user has access to do things in the game.
     */
    protected void validateUser (ClientObject caller)
        throws InvocationException
    {
        BodyObject body = (BodyObject)caller;

        switch (getMatchType()) {
        case GameConfig.PARTY:
            return; // always validate.

        default:
            if (getPlayerIndex(body.getVisibleName()) == -1) {
                throw new InvocationException(InvocationCodes.ACCESS_DENIED);
            }
            return;
        }
    }

    /**
     * Validate that the specified listener has access to make a change.
     */
    protected void validateStateModification (ClientObject caller, boolean requireHoldsTurn)
        throws InvocationException
    {
        validateUser(caller);

        if (requireHoldsTurn) {
            Name holder = _ezObj.turnHolder;
            if (holder != null && !holder.equals(((BodyObject) caller).getVisibleName())) {
                throw new InvocationException(InvocationCodes.ACCESS_DENIED);
            }
        }
    }

    /**
     * Get the specified player body by Oid.
     */
    protected BodyObject getPlayerByOid (int oid)
    {
        // verify that they're a player
        switch (getMatchType()) {
        case GameConfig.PARTY:
            // all occupants are players in a party game
            break;

        default:
            if (!IntListUtil.contains(_playerOids, oid)) {
                return null; // not a player!
            }
            break;
        }

        return getOccupantByOid(oid);
    }

    /**
     * Get the specified occupant body by Oid.
     */
    protected BodyObject getOccupantByOid (int oid)
    {
        if (!_ezObj.occupants.contains(oid)) {
            return null;
        }
        // return the body
        return (BodyObject) CrowdServer.omgr.getObject(oid);
    }

    @Override
    protected PlaceObject createPlaceObject ()
    {
        return new EZGameObject();
    }

    @Override
    protected void didStartup ()
    {
        super.didStartup();

        _ezObj = (EZGameObject) _plobj;
        _ezObj.setEzGameService(
            (EZGameMarshaller) CrowdServer.invmgr.registerDispatcher(new EZGameDispatcher(this)));

        // if we don't need the no-show timer, start right away (but allow the manager startup
        // process to finish before doing so)
        if (!needsNoShowTimer()) {
            CrowdServer.omgr.postRunnable(new Runnable() {
                public void run () {
                    startGame();
                }
            });
        }
    }

    @Override // from PlaceManager
    protected void bodyEntered (int bodyOid)
    {
        super.bodyEntered(bodyOid);

        // if we have no controller, then our new friend gets control
        if (_ezObj.controllerOid == 0) {
            _ezObj.setControllerOid(bodyOid);
        }
    }

    @Override // from PlaceManager
    protected void bodyUpdated (OccupantInfo info)
    {
        super.bodyUpdated(info);

        // if the controller just disconnected, reassign control
        if (info.status == OccupantInfo.DISCONNECTED && info.bodyOid == _ezObj.controllerOid) {
            _ezObj.setControllerOid(getControllerOid());

        // if everyone in the room was disconnected and this client just reconnected, it becomes
        // the new controller
        } else if (_ezObj.controllerOid == 0) {
            _ezObj.setControllerOid(info.bodyOid);
        }
    }

    @Override // from PlaceManager
    protected void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        // if this player was the controller, reassign control
        if (bodyOid == _ezObj.controllerOid) {
            _ezObj.setControllerOid(getControllerOid());
        }

        // nix any of this player's cookies
        if (_ezObj.userCookies != null && _ezObj.userCookies.containsKey(bodyOid)) {
            _ezObj.removeFromUserCookies(bodyOid);
        }
    }

    @Override
    protected void didShutdown ()
    {
        CrowdServer.invmgr.clearDispatcher(_ezObj.ezGameService);
        stopTickers();

        super.didShutdown();
    }

    @Override 
    protected void gameWillStart ()
    {
        // reset the round id to an initial value. note that we don't distribute the initial value,
        // because the super's version of this function will immediately increment it, and then
        // distribute the new incremented value.
        _ezObj.roundId = 0;
        super.gameWillStart();
    }

    @Override
    protected void gameDidEnd ()
    {
        stopTickers();

        super.gameDidEnd();

        // EZ games immediately resest to PRE_GAME after they end so that they can be restarted if
        // desired by having all players call playerReady() again
        _ezObj.setState(EZGameObject.PRE_GAME);
    }

    @Override
    protected void assignWinners (boolean[] winners)
    {
        if (_winnerOids != null) {
            for (int oid : _winnerOids) {
                int index = IntListUtil.indexOf(_playerOids, oid);
                if (index >= 0 && index < winners.length) {
                    winners[index] = true;
                }
            }
            _winnerOids = null;
        }
    }

    /**
     * Stop and clear all tickers.
     */
    protected void stopTickers ()
    {
        if (_tickers != null) {
            for (Ticker ticker : _tickers.values()) {
                ticker.stop();
            }
            _tickers = null;
        }
    }

    /**
     * Returns the oid of a player to whom to assign control of the game or zero if no players
     * qualify for control.
     */
    protected int getControllerOid ()
    {
        for (OccupantInfo info : _ezObj.occupantInfo) {
            if (info.status != OccupantInfo.DISCONNECTED) {
                return info.bodyOid;
            }
        }
        return 0;
    }

    /**
     * Get the cookie manager, and do a bit of other setup.
     */
    protected GameCookieManager getCookieManager ()
    {
        if (_cookMgr == null) {
            _cookMgr = createCookieManager();
            _ezObj.setUserCookies(new DSet<UserCookie>());
        }
        return _cookMgr;
    }

    /**
     * Creates the cookie manager we'll use to store user cookies.
     */
    protected GameCookieManager createCookieManager ()
    {
        return new GameCookieManager();
    }

    /**
     * A timer that fires message events to a game.
     */
    protected static class Ticker
    {
        /**
         * Create a Ticker.
         */
        public Ticker (String name, EZGameObject gameObj)
        {
            _name = name;
            // once we are constructed, we want to avoid calling methods on dobjs.
            _oid = gameObj.getOid();
            _omgr = gameObj.getManager();
        }

        public void start (int msOfDelay)
        {
            _value = 0;
            _interval.schedule(0, msOfDelay);
        }

        public void stop ()
        {
            _interval.cancel();
        }

        /**
         * The interval that does our work. Note well that this is not a 'safe' interval that
         * operates using a RunQueue.  This interval instead does something that we happen to know
         * is safe for any thread: posting an event to the dobj manager.  If we were using a
         * RunQueue it would be the same event queue and we would be posted there, wait our turn,
         * and then do the same thing: post this event. We just expedite the process.
         */
        protected Interval _interval = new Interval() {
            public void expired () {
                _omgr.postEvent(
                    new MessageEvent(_oid, EZGameObject.TICKER, new Object[] { _name, _value++ }));
            }
        };

        protected int _oid;
        protected DObjectManager _omgr;
        protected String _name;
        protected int _value;
    } // End: static class Ticker

    /** A nice casted reference to the game object. */
    protected EZGameObject _ezObj;

    /** Our turn delegate. */
    protected EZGameTurnDelegate _turnDelegate;

    /** The map of collections, lazy-initialized. */
    protected HashMap<String, ArrayList<byte[]>> _collections;

    /** The map of tickers, lazy-initialized. */
    protected HashMap<String, Ticker> _tickers;

    /** Tracks which cookies are currently being retrieved from the db. */
    protected ArrayIntSet _cookieLookups = new ArrayIntSet();

    /** The array of winner oids, after the user has filled it in. */
    protected int[] _winnerOids;

    /** Handles the storage of our user cookies; lazily initialized. */
    protected GameCookieManager _cookMgr;

    /** The minimum delay a ticker can have. */
    protected static final int MIN_TICKER_DELAY = 50;

    /** The maximum number of tickers allowed at one time. */
    protected static final int MAX_TICKERS = 3;
}
