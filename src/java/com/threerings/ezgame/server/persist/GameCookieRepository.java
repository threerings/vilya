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

package com.threerings.ezgame.server.persist;

import java.util.Set;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.depot.DepotRepository;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.jdbc.depot.PersistentRecord;

/**
 * Provides storage services for user cookies used in games.
 */
public class GameCookieRepository extends DepotRepository
{
    /** The database identifier used when establishing a connection. */
    public static final String COOKIE_DB_IDENT = "gameCookiedb";

    public GameCookieRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(new PersistenceContext(COOKIE_DB_IDENT, conprov));
    }

    /**
     * Get the specified game cookie, or null if none.
     */
    public byte[] getCookie (int gameId, int userId)
        throws PersistenceException
    {
        GameCookieRecord record = load(
            GameCookieRecord.class, GameCookieRecord.getKey(gameId, userId));
        return record != null ? record.cookie : null;
    }

    /**
     * Set the specified user's game cookie.
     */
    public void setCookie (
        final int gameId, final int userId, final byte[] cookie)
        throws PersistenceException
    {
        if (cookie != null) {
            store(new GameCookieRecord(gameId, userId, cookie));
        } else {
            delete(GameCookieRecord.class, GameCookieRecord.getKey(gameId, userId));
        }
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(GameCookieRecord.class);
    }
}
