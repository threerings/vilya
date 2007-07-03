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

import java.util.prefs.Preferences;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.RepositoryListenerUnit;
import com.samskivert.util.Invoker;
import com.samskivert.util.ResultListener;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.server.CrowdServer;
import com.threerings.ezgame.server.persist.GameCookieRepository;

import static com.threerings.ezgame.server.Log.log;

/**
 * Manages access to game cookies.
 */
public class GameCookieManager
{
    /**
     * An interface for identifying users.
     */
    public interface UserIdentifier
    {
        /** Return the persistent user id for the specified player, or 0 if they're not a valid
         * user, or a guest, or something like that (they'll have no cookies). */
        public int getUserId (BodyObject bodyObj);
    }

    /**
     * Sets up cookie manager that will store cookie information on the local filesystem using the
     * Java preferences system. This should only be used during testing and not in a production
     * system.
     */
    public static void init (UserIdentifier ider)
    {
        _singleton = new GameCookieManager(ider);
    }

    /**
     * Sets up a cookie manager that will store cookie information in a database table accessed via
     * the supplied connection provider.
     */
    public static void init (ConnectionProvider conprov, UserIdentifier ider)
        throws PersistenceException
    {
        _singleton = new GameCookieManager(conprov, ider);
    }

    /**
     * Get an instance of the GameCookieManager.
     */
    public static GameCookieManager getInstance ()
    {
        return _singleton;
    }

    /**
     * Get the specified user's cookie.
     */
    public void getCookie (final int gameId, BodyObject bobj, ResultListener<byte[]> rl)
    {
        final int userId = _identifier.getUserId(bobj);
        if (userId == 0) {
            rl.requestCompleted(null);
            return;
        }

        // use our local prefs if our repository is not initialized
        if (_repo == null) {
            rl.requestCompleted(_prefs.getByteArray(gameId + ":" + userId, (byte[])null));
            return;
        }

        CrowdServer.invoker.postUnit(new RepositoryListenerUnit<byte[]>("getGameCookie", rl) {
            public byte[] invokePersistResult () throws PersistenceException {
                return _repo.getCookie(gameId, userId);
            }
        });
    }

    /**
     * Set the specified user's cookie.
     */
    public void setCookie (final int gameId, BodyObject bobj, final byte[] cookie)
    {
        final int userId = _identifier.getUserId(bobj);
        if (userId == 0) {
            // fail to save, silently
            return;
        }

        // use our local prefs if our repository is not initialized
        if (_repo == null) {
            _prefs.putByteArray(gameId + ":" + userId, cookie);
            return;
        }

        CrowdServer.invoker.postUnit(new Invoker.Unit("setGameCookie") {
            public boolean invoke () {
                try {
                    _repo.setCookie(gameId, userId, cookie);
                } catch (PersistenceException pe) {
                    log.warning("Unable to save game cookie [pe=" + pe + "].");
                }
                return false;
            }
        });
    }

    /**
     * Protected constructor.
     */
    protected GameCookieManager (UserIdentifier identifier)
    {
        _identifier = identifier;
        if (_identifier == null) {
            throw new IllegalArgumentException("UserIdentifier must be non-null");
        }
        _prefs = Preferences.userRoot().node("gameCookieManager");
    }

    /**
     * Protected constructor.
     */
    protected GameCookieManager (ConnectionProvider conprov, UserIdentifier identifier)
        throws PersistenceException
    {
        this(identifier);
        _repo = new GameCookieRepository(conprov);
    }

    /** The entity we ask to identify users. */
    protected UserIdentifier _identifier;

    /** Our database repository, which is used in real operation. */
    protected GameCookieRepository _repo;

    /** Our local store, which is used when testing. */
    protected Preferences _prefs;

    /** A reference to the single GameCookieManager instantiated. */
    protected static GameCookieManager _singleton;
}
