//
// $Id: GameCookieRepository.java 209 2007-02-24 00:37:33Z mdb $
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

import com.samskivert.jdbc.depot.Key;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.annotation.Column;
import com.samskivert.jdbc.depot.annotation.Entity;
import com.samskivert.jdbc.depot.annotation.Id;
import com.samskivert.jdbc.depot.expression.ColumnExp;

@Entity(name="GAME_COOKIES")
public class GameCookieRecord extends PersistentRecord
{
    // AUTO-GENERATED: FIELDS START
    /** The column identifier for the {@link #gameId} field. */
    public static final String GAME_ID = "gameId";

    /** The qualified column identifier for the {@link #gameId} field. */
    public static final ColumnExp GAME_ID_C =
        new ColumnExp(GameCookieRecord.class, GAME_ID);

    /** The column identifier for the {@link #userId} field. */
    public static final String USER_ID = "userId";

    /** The qualified column identifier for the {@link #userId} field. */
    public static final ColumnExp USER_ID_C =
        new ColumnExp(GameCookieRecord.class, USER_ID);

    /** The column identifier for the {@link #cookie} field. */
    public static final String COOKIE = "cookie";

    /** The qualified column identifier for the {@link #cookie} field. */
    public static final ColumnExp COOKIE_C =
        new ColumnExp(GameCookieRecord.class, COOKIE);
    // AUTO-GENERATED: FIELDS END

    public static final int SCHEMA_VERSION = 1;
    
    /** The id of the game for which this is a cookie. */
    @Id
    @Column(name="GAME_ID")
    public int gameId;
    
    /** The id of the user for which this is a cookie. */
    @Id
    @Column(name="USER_ID")
    public int userId;
    
    /** The actual cookie, as a byte array. */
    @Column(name="COOKIE")
    public byte[] cookie;

    /** A no-argument constructor for deserialization. */
    public GameCookieRecord ()
    {
    }
    
    /** A constructor for configuring all the fields of this record. */
    public GameCookieRecord (int gameId, int userId, byte[] cookie)
    {
        super();
        this.gameId = gameId;
        this.userId = userId;
        this.cookie = cookie;
    }

    // AUTO-GENERATED: METHODS START
    /**
     * Create and return a primary {@link Key} to identify a {@link #GameCookieRecord}
     * with the supplied key values.
     */
    public static Key<GameCookieRecord> getKey (int gameId, int userId)
    {
        return new Key<GameCookieRecord>(
                GameCookieRecord.class,
                new String[] { GAME_ID, USER_ID },
                new Comparable[] { gameId, userId });
    }
    // AUTO-GENERATED: METHODS END
}
