//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.RepositoryListenerUnit;

import com.samskivert.util.Invoker;
import com.samskivert.util.ResultListener;

import com.threerings.presents.data.ClientObject;

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
        /** Return the persistent user id for the specified player,
         * or 0 if they're not a valid user, or a guest,
         * or something like that (they'll have no cookies). */
        public int getUserId (ClientObject clientObj);
    }

    /**
     * Called to set up game cookie services for the server.
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
     * Protected constructor.
     */
    protected GameCookieManager 
        (ConnectionProvider conprov, UserIdentifier identifier)
        throws PersistenceException
    {
        _repo = new GameCookieRepository(conprov);
        _identifier = identifier;
        if (_identifier == null) {
            throw new IllegalArgumentException(
                "UserIdentifier must be non-null");
        }
    }

    /**
     * Get the specified user's cookie.
     */
    public void getCookie (
        final int gameId, ClientObject cliObj, ResultListener<byte[]> rl)
    {
        final int userId = _identifier.getUserId(cliObj);
        if (userId == 0) {
            rl.requestCompleted(null);
            return;
        }
        CrowdServer.invoker.postUnit(
            new RepositoryListenerUnit<byte[]>("getGameCookie", rl) {
                public byte[] invokePersistResult ()
                    throws PersistenceException
                {
                    return _repo.getCookie(gameId, userId);
                }
            });
    }

    /**
     * Set the specified user's cookie.
     */
    public void setCookie (
        final int gameId, ClientObject cliObj, final byte[] cookie)
    {
        final int userId = _identifier.getUserId(cliObj);
        if (userId == 0) {
            // fail to save, silently
            return;
        }
        CrowdServer.invoker.postUnit(new Invoker.Unit("setGameCookie") {
            public boolean invoke ()
            {
                try {
                    _repo.setCookie(gameId, userId, cookie);
                } catch (PersistenceException pe) {
                    log.warning("Unable to save game cookie [pe=" + pe + "].");
                }
                return false;
            }
        });
    }

    /** Our repository. */
    protected GameCookieRepository _repo;

    /** The entity we ask to identify users. */
    protected UserIdentifier _identifier;

    /** A reference to the single GameCookieManager instantiated. */
    protected static GameCookieManager _singleton;

}
