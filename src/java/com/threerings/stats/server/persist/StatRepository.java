//
// $Id$

package com.threerings.stats.server.persist;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.logging.Level;

import com.samskivert.io.ByteArrayOutInputStream;
import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;

import com.samskivert.jdbc.depot.CacheInvalidator;
import com.samskivert.jdbc.depot.DepotRepository;
import com.samskivert.jdbc.depot.PersistenceContext.CacheEvictionFilter;
import com.samskivert.jdbc.depot.PersistenceContext;
import com.samskivert.jdbc.depot.PersistentRecord;
import com.samskivert.jdbc.depot.clause.FieldOverride;
import com.samskivert.jdbc.depot.clause.FromOverride;
import com.samskivert.jdbc.depot.clause.QueryClause;
import com.samskivert.jdbc.depot.clause.Where;
import com.samskivert.jdbc.depot.expression.FunctionExp;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.stats.data.Stat;

import static com.threerings.stats.Log.log;

/**
 * Responsible for the persistent storage of per-player statistics.
 */
public class StatRepository extends DepotRepository
    implements Stat.AuxDataSource
{
    /**
     * Constructs a new statistics repository with the specified persistence context.
     */
    public StatRepository (PersistenceContext context)
        throws PersistenceException
    {
        super(context);

        // load up our string set mappings
        loadStringCodes(null);
    }

    /**
     * Loads the stats associated with the specified player.
     */
    public ArrayList<Stat> loadStats (int playerId)
        throws PersistenceException
    {
        ArrayList<Stat> stats = new ArrayList<Stat>();
        Where where = new Where(StatRecord.PLAYER_ID_C, playerId);
        for (StatRecord record : findAll(StatRecord.class, where)) {
            Stat stat = decodeStat(record.statCode, record.statData);
            if (stat != null) {
                stats.add(stat);
            }
        }
        return stats;
    }

    /**
     * Deletes all stats associated with the specified player.
     */
    public void deleteStats (final int playerId)
        throws PersistenceException
    {
        CacheInvalidator invalidator = new CacheInvalidator() {
            public void invalidate (PersistenceContext ctx) {
                ctx.cacheTraverse(
                    StatRecord.class.getName(), new CacheEvictionFilter<StatRecord>() {
                    public boolean testForEviction (Serializable key, StatRecord record) {
                        return record != null && record.playerId == playerId;
                    }
                });
            }
        };
        deleteAll(StatRecord.class, new Where(StatRecord.PLAYER_ID_C, playerId), invalidator);
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
        ByteArrayOutInputStream out = new ByteArrayOutInputStream();
        try {
            stat.persistTo(new ObjectOutputStream(out), this);
        } catch (IOException ioe) {
            String errmsg = "Error serializing stat " + stat;
            throw new PersistenceException(errmsg, ioe);
        }

        store(new StatRecord(playerId, stat.getCode(), out.toByteArray()));
    }

    /** Helper function for {@link #getStringCode}. */
    protected Integer assignStringCode (final Stat.Type type, final String value)
        throws PersistenceException
    {
        for (int ii = 0; ii < 10; ii++) {
            MaxStatCodeRecord maxRecord = load(
                MaxStatCodeRecord.class,
                new FromOverride(StringCodeRecord.class),
                new FieldOverride(MaxStatCodeRecord.MAX_CODE,
                                  new FunctionExp("MAX", StringCodeRecord.CODE_C)),
                new Where(StringCodeRecord.STAT_CODE_C, type.code()));

            int code = maxRecord != null ? maxRecord.maxCode + 1 : 1;

            // DEBUG: uncomment this to test code collision
            // if (ii == 0 && code > 0) {
            //     code = code-1;
            // }

            try {
                insert(new StringCodeRecord(type.code(), value, code));
                return code;

            } catch (PersistenceException pe) {
                // if this is not a duplicate row exception, something is booched and we
                // just fail

//                if (!liaison.isDuplicateRowException(sqe)) {
//                    throw sqe;
//                }

                // if it is a duplicate row exception, possibly someone inserted our value
                // before we could, in which case we can just look up the new mapping
                StringCodeRecord record = load(
                    StringCodeRecord.class,
                    new Where(StringCodeRecord.STAT_CODE_C, type.code(),
                              StringCodeRecord.VALUE_C, value));
                if (record != null) {
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
        throw new PersistenceException(
            "Unable to assign code after 10 attempts [type=" + type + ", value=" + value + "]");
    }

    /** Helper function used at repository startup. */
    protected void loadStringCodes (Stat.Type type)
        throws PersistenceException
    {
        QueryClause[] clauses;
        if (type != null) {
            clauses = new QueryClause[] { new Where(StringCodeRecord.STAT_CODE_C, type.code()) };
        } else {
            clauses = new QueryClause[0];
        }

        for (StringCodeRecord record : findAll(StringCodeRecord.class, clauses)) {
            mapStringCode(Stat.getType(record.statCode), record.value, record.code);
        }
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

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(StatRecord.class);
        classes.add(StringCodeRecord.class);
    }

    protected HashMap<Stat.Type,HashMap<String,Integer>> _stringToCode =
        new HashMap<Stat.Type,HashMap<String,Integer>>();
    protected HashMap<Stat.Type,HashIntMap<String>> _codeToString =
        new HashMap<Stat.Type,HashIntMap<String>>();
}
