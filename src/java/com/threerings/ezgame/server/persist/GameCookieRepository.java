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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.SimpleRepository;
import com.samskivert.jdbc.TransitionRepository;


/**
 * Provides storage services for user cookies used in games.
 */
public class GameCookieRepository extends SimpleRepository
{
    /** The database identifier used when establishing a connection. */
    public static final String COOKIE_DB_IDENT = "gameCookiedb";

    public GameCookieRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, COOKIE_DB_IDENT);

        maintenance("analyze", "GAME_COOKIES");
    }

    @Override
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        super.migrateSchema(conn, liaison);

        JDBCUtil.createTableIfMissing(conn, "GAME_COOKIES", new String[] {
            "GAME_ID integer not null",
            "USER_ID integer not null",
            "COOKIE blob not null",
            "primary key (GAME_ID, USER_ID)" }, "");
    }

    /**
     * Get the specified game cookie, or null if none.
     */
    public byte[] getCookie (final int gameId, final int userId)
        throws PersistenceException
    {
        return execute(new Operation<byte[]>() {
            public byte[] invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = conn.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery("select COOKIE " +
                        "from GAME_COOKIES where GAME_ID=" + gameId +
                        " and USER_ID=" + userId);
                    if (rs.next()) {
                        return rs.getBytes(1);
                    }
                    return null;
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /**
     * Set the specified user's game cookie.
     */
    public void setCookie (
        final int gameId, final int userId, final byte[] cookie)
        throws PersistenceException
    {
        executeUpdate(new Operation<Void>() {
            public Void invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                if (cookie == null) {
                    Statement stmt = conn.createStatement();
                    try {
                        stmt.executeUpdate("delete from GAME_COOKIES" +
                            " where GAME_ID=" + gameId +
                            " and USER_ID=" + userId);
                        return null;
                    } finally {
                        JDBCUtil.close(stmt);
                    }

                } else {
                    PreparedStatement stmt = conn.prepareStatement(
                        "insert into GAME_COOKIES (GAME_ID, USER_ID, COOKIE) " +
                        "values (" + gameId + "," + userId + ",?) " +
                        "on duplicate key update COOKIE=values(COOKIE)");
                    try {
                        stmt.setBytes(1, cookie);
                        stmt.executeUpdate();
                        return null;
                    } finally {
                        JDBCUtil.close(stmt);
                    }
                }
            }
        });
    }
}
