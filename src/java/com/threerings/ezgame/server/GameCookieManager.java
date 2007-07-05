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
     * Creates a game cookie manager that stores cookies in Java preferences on the local machine.
     * This should only be used for developer testing.
     */
    public GameCookieManager ()
    {
        _prefs = Preferences.userRoot().node("gameCookieManager");
    }

    /**
     * Creates a game cookie manager that stores cookies in the supplied repository.
     */
    public GameCookieManager (GameCookieRepository repo)
        throws PersistenceException
    {
        _repo = repo;
    }

    /**
     * Get the specified user's cookie.
     */
    public void getCookie (final int gameId, final int userId, ResultListener<byte[]> rl)
    {
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
    public void setCookie (final int gameId, final int userId, final byte[] cookie)
    {
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

    /** Our database repository, which is used in real operation. */
    protected GameCookieRepository _repo;

    /** Our local store, which is used when testing. */
    protected Preferences _prefs;
}
