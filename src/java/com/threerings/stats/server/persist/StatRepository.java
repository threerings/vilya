//
// $Id$

package com.threerings.stats.server.persist;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;

import com.samskivert.io.ByteArrayOutInputStream;
import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.SimpleRepository;
import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.stats.data.Stat;

import static com.threerings.stats.Log.log;

/**
 * Responsible for the persistent storage of per-player statistics.
 */
public class StatRepository extends SimpleRepository
    implements Stat.AuxDataSource
{
    /**
     * The database identifier used when establishing a database connection. This value being
     * <code>statdb</code>.
     */
    public static final String STAT_DB_IDENT = "statdb";

    /**
     * Constructs a new statistics repository with the specified connection provider.
     *
     * @param conprov the connection provider via which we will obtain our
     * database connection.
     */
    public StatRepository (ConnectionProvider conprov)
        throws PersistenceException
    {
        super(conprov, STAT_DB_IDENT);

        // load up our string set mappings
        loadStringCodes(null);
    }

    /**
     * Loads the stats associated with the specified player.
     */
    public ArrayList<Stat> loadStats (final int playerId)
        throws PersistenceException
    {
        final ArrayList<Stat> stats = new ArrayList<Stat>();
        final String query = "select STAT_CODE, STAT_DATA from STATS where PLAYER_ID = " + playerId;
        execute(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = conn.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        Stat stat = decodeStat(
                            rs.getInt(1), (byte[])rs.getObject(2));
                        if (stat != null) {
                            stats.add(stat);
                        }
                    }
                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
        return stats;
    }

    /**
     * Deletes all stats associated with the specified player.
     */
    public void deleteStats (int playerId)
        throws PersistenceException
    {
        update("delete from STATS where PLAYER_ID = " + playerId);
    }

    /**
     * Writes out any of the stats in the supplied array that have been modified since they were
     * first loaded. Exceptions that occur while writing the stats will be caught and logged.
     */
    public void writeModified (int playerId, Stat[] stats)
    {
        for (int ii = 0; ii < stats.length; ii++) {
            try {
                if (stats[ii].getType().isPersistent() &&
                    stats[ii].isModified()) {
                    updateStat(playerId, stats[ii]);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "Error flushing modified stat [stat=" + stats[ii] + "].", e);
            }
        }
    }

    // documentation inherited from interface Stat.AuxDataSource
    public int getStringCode (Stat.Type type, String value)
    {
        HashMap<String,Integer> map = _stringToCode.get(type);
        if (map == null) {
            _stringToCode.put(type, map = new HashMap<String,Integer>());
        }
        Integer code = map.get(value);
        if (code == null) {
            try {
                code = assignStringCode(type, value);
            } catch (PersistenceException pe) {
                log.log(Level.WARNING, "Failed to assign code [type=" + type +
                        ", value=" + value + "].", pe);
                // at this point the database is probably totally hosed, so we can just punt here,
                // and assume that this value will never be persisted
                code = -1;
            }
            mapStringCode(type, value, code);
        }
        return code;
    }

    // documentation inherited from interface Stat.AuxDataSource
    public String getCodeString (Stat.Type type, int code)
    {
        HashIntMap<String> map = _codeToString.get(type);
        String value = (map == null) ? null : map.get(code);
        if (value == null) {
            // our value may have been mapped on a different server, so refresh
            // this mapping table from the database; then try again
            try {
                loadStringCodes(type);
            } catch (PersistenceException pe) {
                log.log(Level.WARNING, "Failed to reload string codes " +
                    "[type=" + type + ", code=" + code + "].", pe);
            }
            map = _codeToString.get(type);
            value = (map == null) ? null : map.get(code);
            if (value == null) {
                log.warning("Missing reverse maping [type=" + type +
                    ", code=" + code + "].");
            }
        }
        return value;
    }

    /**
     * This is only used for testing. Do not call this method.
     */
    public void clearMapping (Stat.Type type, String value)
    {
        int ocode = _stringToCode.get(type).remove(value);
        _codeToString.get(type).remove(ocode);
    }

    /**
     * Instantiates the appropriate stat class and decodes the stat from the data.
     */
    protected Stat decodeStat (int statCode, byte[] data)
    {
        Stat.Type type = Stat.getType(statCode);
        if (type == null) {
            log.warning("Unable to decode stat, unknown type [code=" + statCode + "].");
            return null;
        }
        return decodeStat(type.newStat(), data);
    }

    /**
     * Instantiates the appropriate stat class and decodes the stat from the data.
     */
    protected Stat decodeStat (Stat stat, byte[] data)
    {
        String errmsg = null;
        Exception error = null;

        try {
            // decode its contents from the serialized data
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            stat.unpersistFrom(new ObjectInputStream(bin), this);
            return stat;

        } catch (ClassNotFoundException cnfe) {
            error = cnfe;
            errmsg = "Unable to instantiate stat";

        } catch (IOException ioe) {
            error = ioe;
            errmsg = "Unable to decode stat";
        }

        log.log(Level.WARNING, errmsg + " [type=" + stat.getType() + "]", error);
        return null;
    }

    /**
     * Updates the specified stat in the database, inserting it if necessary.
     */
    protected void updateStat (int playerId, final Stat stat)
        throws PersistenceException
    {
        final String uquery = "update STATS set STAT_DATA = ?" +
            " where PLAYER_ID = " + playerId + " and STAT_CODE = " + stat.getCode();
        final String iquery = "insert into STATS (PLAYER_ID, STAT_CODE, " +
            "STAT_DATA) values (" + playerId + ", " + stat.getCode() + ", ?)";
        final ByteArrayOutInputStream out = new ByteArrayOutInputStream();
        try {
            stat.persistTo(new ObjectOutputStream(out), this);
        } catch (IOException ioe) {
            String errmsg = "Error serializing stat " + stat;
            throw new PersistenceException(errmsg, ioe);
        }

        // now update (or insert) the flattened data into the database
        executeUpdate(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                PreparedStatement stmt = conn.prepareStatement(uquery);
                try {
                    stmt.setBinaryStream(1, out.getInputStream(), out.size());
                    if (stmt.executeUpdate() == 0) {
                        JDBCUtil.close(stmt);
                        stmt = conn.prepareStatement(iquery);
                        stmt.setBinaryStream(1, out.getInputStream(), out.size());
                        JDBCUtil.checkedUpdate(stmt, 1);
                    }
                    return null;
                } finally {
                    JDBCUtil.close(stmt);
                }
            }
        });
    }

    /** Helper function for {@link #getStringCode}. */
    protected Integer assignStringCode (final Stat.Type type, final String value)
        throws PersistenceException
    {
        return executeUpdate(new Operation<Integer>() {
            public Integer invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                for (int ii = 0; ii < 10; ii++) {
                    try {
                        int code = getNextCode(conn, type);
                        // DEBUG: uncomment this to test code collision
                        // if (ii == 0 && code > 0) {
                        //     code = code-1;
                        // }
                        insertStringCode(conn, type, value, code);
                        return code;

                    } catch (SQLException sqe) {
                        // if this is not a duplicate row exception, something is booched and we
                        // just fail
                        if (!liaison.isDuplicateRowException(sqe)) {
                            throw sqe;
                        }

                        // if it is a duplicate row exception, possibly someone inserted our value
                        // before we could, in which case we can just look up the new mapping
                        int code = getCurrentCode(conn, type, value);
                        if (code != -1) {
                            log.info("Value collision assigning string code [type=" + type +
                                     ", value=" + value + "].");
                            return code;
                        }

                        // otherwise someone used the code we were trying to use and we just need
                        // to loop around and get the next highest code
                        log.info("Code collision assigning string code [type=" + type +
                                 ", value=" + value + "].");
                    }
                }
                throw new SQLException("Unable to assign code after 10 attempts " +
                                       "[type=" + type + ", value=" + value + "]");
            }
        });
    }

    /** Helper function for {@link #assignStringCode}. */
    protected int getNextCode (Connection conn, Stat.Type type)
        throws SQLException
    {
        return getCode(conn, "select MAX(CODE)+1 from STRING_CODES where STAT_CODE = " +
                       type.code());
    }

    /** Helper function for {@link #assignStringCode}. */
    protected int getCurrentCode (Connection conn, Stat.Type type, String value)
        throws SQLException
    {
        return getCode(conn, "select CODE from STRING_CODES where STAT_CODE = " + type.code() +
                       " and VALUE = " + JDBCUtil.escape(value));
    }

    /** Helper function for {@link #getNextCode} and {@link #getCurrentCode}. */
    protected int getCode (Connection conn, String query)
        throws SQLException
    {
        int code = -1;
        Statement stmt = conn.createStatement();
        try {
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                code = rs.getInt(1);
            }
        } finally {
            JDBCUtil.close(stmt);
        }
        return code;
    }

    /** Helper function for {@link #assignStringCode}. */
    protected void insertStringCode (Connection conn, Stat.Type type, String value, int code)
        throws SQLException
    {
        String query = "insert into STRING_CODES (STAT_CODE, VALUE, CODE) values(" + type.code() +
            ", " + JDBCUtil.escape(value) + ", " + code + ")";
        Statement stmt = conn.createStatement();
        try {
            int mods = stmt.executeUpdate(query);
            if (mods != 1) {
                throw new SQLException("Insertion failed to modify one row [type=" + type +
                                       ", value=" + value + ", code=" + code +
                                       ", mods=" + mods + "]");
            }
        } finally {
            JDBCUtil.close(stmt);
        }
    }

    /** Helper function used at repository startup. */
    protected void loadStringCodes (Stat.Type type)
        throws PersistenceException
    {
        final String query = "select STAT_CODE, VALUE, CODE from STRING_CODES " +
            ((type == null) ? "" : (" where STAT_CODE = " + type.code()));
        execute(new Operation<Object>() {
            public Object invoke (Connection conn, DatabaseLiaison liaison)
                throws SQLException, PersistenceException
            {
                Statement stmt = conn.createStatement();
                try {
                    ResultSet rs = stmt.executeQuery(query);
                    while (rs.next()) {
                        mapStringCode(Stat.getType(rs.getInt(1)), rs.getString(2), rs.getInt(3));
                    }
                } finally {
                    JDBCUtil.close(stmt);
                }
                return null;
            }
        });
    }

    /** Helper function used at repository startup. */
    protected void mapStringCode (Stat.Type type, String value, int code)
    {
        HashMap<String,Integer> fmap = _stringToCode.get(type);
        if (fmap == null) {
            _stringToCode.put(type, fmap = new HashMap<String,Integer>());
        }
        fmap.put(value, code);
        HashIntMap<String> rmap = _codeToString.get(type);
        if (rmap == null) {
            _codeToString.put(type, rmap = new HashIntMap<String>());
        }
        rmap.put(code, value);
    }

    @Override // documentation inherited
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        JDBCUtil.createTableIfMissing(conn, "STATS", new String[] {
            "PLAYER_ID INTEGER NOT NULL",
            "STAT_CODE INTEGER NOT NULL",
            "STAT_DATA BLOB NOT NULL",
            "KEY (PLAYER_ID)",
            "KEY (STAT_CODE)",
        }, "");

        JDBCUtil.createTableIfMissing(conn, "STRING_CODES", new String[] {
            "STAT_CODE INTEGER NOT NULL",
            "VALUE VARCHAR(255) NOT NULL",
            "CODE INTEGER NOT NULL",
            "UNIQUE (STAT_CODE, VALUE)",
            "UNIQUE (STAT_CODE, CODE)",
        }, "");
    }

    protected HashMap<Stat.Type,HashMap<String,Integer>> _stringToCode =
        new HashMap<Stat.Type,HashMap<String,Integer>>();
    protected HashMap<Stat.Type,HashIntMap<String>> _codeToString =
        new HashMap<Stat.Type,HashIntMap<String>>();
}
