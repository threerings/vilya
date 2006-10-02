//
// $Id$

package com.threerings.ezgame.server;

import java.util.ArrayList;
import java.util.HashMap;

import com.samskivert.util.ArrayUtil;
import com.samskivert.util.CollectionUtil;
import com.samskivert.util.Interval;
import com.samskivert.util.RandomUtil;

import com.threerings.util.Name;

import com.threerings.presents.data.ClientObject;
import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.dobj.AccessController;
import com.threerings.presents.dobj.DObjectManager;
import com.threerings.presents.dobj.MessageEvent;
import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.parlor.game.server.GameManager;

import com.threerings.parlor.turn.server.TurnGameManager;

import com.threerings.ezgame.data.EZGameObject;
import com.threerings.ezgame.data.EZGameMarshaller;
import com.threerings.ezgame.data.PropertySetEvent;

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
    public void endTurn (
        ClientObject caller, int nextPlayerIndex,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateStateModification(caller);
        _turnDelegate.endTurn(nextPlayerIndex);
    }

    // from EZGameProvider
    public void endGame (
        ClientObject caller, int[] winners,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateStateModification(caller);

        _winnerIndexes = winners;
        endGame();
    }

    // from EZGameProvider
    public void sendMessage (
        ClientObject caller, String msg, Object data, int playerIdx,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);

        if (playerIdx < 0 || playerIdx >= _gameObj.players.length) {
            _gameObj.postMessage(EZGameObject.USER_MESSAGE,
                new Object[] { msg, data });

        } else {
            sendPrivateMessage(playerIdx, msg, data);
        }
    }

    // from EZGameProvider
    public void setProperty (
        ClientObject caller, String propName, Object data, int index,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);
        setProperty(propName, data, index);
    }

    // from EZGameProvider
    public void addToCollection (
        ClientObject caller, String collName, byte[][] data,
        boolean clearExisting, InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);
        if (_collections == null) {
            _collections = new HashMap<String, ArrayList<byte[]>>();
        }

        // figure out if we're adding to an existing collection
        // or creating a new one
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
    public void getFromCollection (
        ClientObject caller, String collName, boolean consume, int count,
        String msgOrPropName, int playerIndex,
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

                if (playerIndex >= 0 && playerIndex < _gameObj.players.length) {
                    sendPrivateMessage(playerIndex, msgOrPropName, result);

                } else {
                    setProperty(msgOrPropName, result, -1);
                }
                // SUCCESS!
                listener.requestProcessed();
                return;
            }
        }

        // TODO: decide what we want to return here
        throw new InvocationException(String.valueOf(srcSize));
    }

    // from EZGameProvider
    public void mergeCollection (
        ClientObject caller, String srcColl, String intoColl,
        InvocationService.InvocationListener listener)
        throws InvocationException
    {
        validateUser(caller);

        // non-existent collections are treated as empty, so if the
        // source doesn't exist, we silently accept it
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
    public void setTicker (
        ClientObject caller, String tickerName, int msOfDelay,
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
                t = new Ticker(tickerName, _gameObj);
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

    /**
     * Helper method to send a private message to the specified player
     * index (must already be verified).
     */
    protected void sendPrivateMessage (
        int playerIdx, String msg, Object data)
        throws InvocationException
    {
        BodyObject target = getPlayer(playerIdx);
        if (target == null) {
            // TODO: this code has no corresponding translation
            throw new InvocationException("m.player_not_around");
        }

        target.postMessage(
            EZGameObject.USER_MESSAGE + ":" + _gameObj.getOid(),
            new Object[] { msg, data });
    }

    /**
     * Helper method to post a property set event.
     */
    protected void setProperty (String propName, Object value, int index)
    {
        _gameObj.postEvent(
            new PropertySetEvent(_gameObj.getOid(), propName, value, index));
    }

    /**
     * Validate that the specified user has access to do things in the game.
     */
    protected void validateUser (ClientObject caller)
        throws InvocationException
    {
        if (getPresentPlayerIndex(caller.getOid()) == -1) {
            throw new InvocationException(InvocationCodes.ACCESS_DENIED);
        }
    }

    /**
     * Validate that the specified listener has access to make a
     * change.
     */
    protected void validateStateModification (ClientObject caller)
        throws InvocationException
    {
        validateUser(caller);

        Name holder = _gameObj.turnHolder;
        if (holder != null &&
                !holder.equals(((BodyObject) caller).getVisibleName())) {
            throw new InvocationException(InvocationCodes.ACCESS_DENIED);
        }
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

        _gameObj = (EZGameObject) _plobj;

        _gameObj.setEzGameService(
            (EZGameMarshaller) CrowdServer.invmgr.registerDispatcher(
            new EZGameDispatcher(this), false));
    }

    @Override
    protected void didShutdown ()
    {
        CrowdServer.invmgr.clearDispatcher(_gameObj.ezGameService);
        stopTickers();

        super.didShutdown();
    }

    @Override
    protected void gameDidEnd ()
    {
        stopTickers();

        super.gameDidEnd();
    }

    @Override
    protected void assignWinners (boolean[] winners)
    {
        if (_winnerIndexes != null) {
            for (int index : _winnerIndexes) {
                if (index >= 0 && index < winners.length) {
                    winners[index] = true;
                }
            }
            _winnerIndexes = null;
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
            // once we are constructed, we want to avoid calling
            // methods on dobjs.
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

        /** The interval that does our work. Note well that this is
         * not a 'safe' interval that operates using a RunQueue.
         * This interval instead does something that we happen to know
         * is safe for any thread: posting an event to the dobj manager.
         * If we were using a RunQueue it would be the same event queue
         * and we would be posted there, wait our turn, and then do the same
         * thing: post this event. We just expedite the process.
         */
        protected Interval _interval = new Interval() {
            public void expired ()
            {
                _omgr.postEvent(
                    new MessageEvent(_oid, EZGameObject.USER_MESSAGE,
                        new Object[] { _name, _value++ }));
            }
        };

        protected int _oid;
        protected DObjectManager _omgr;
        protected String _name;
        protected int _value;
    } // End: static class Ticker

    /** A nice casted reference to the game object. */
    protected EZGameObject _gameObj;

    /** Our turn delegate. */
    protected EZGameTurnDelegate _turnDelegate;

    /** The array of winners, after the user has filled it in. */
    protected int[] _winnerIndexes;

    /** The map of collections, lazy-initialized. */
    protected HashMap<String, ArrayList<byte[]>> _collections;

    /** The map of tickers, lazy-initialized. */
    protected HashMap<String, Ticker> _tickers;

    /** The minimum delay a ticker can have. */
    protected static final int MIN_TICKER_DELAY = 50;

    /** The maximum number of tickers allowed at one time. */
    protected static final int MAX_TICKERS = 3;
}
