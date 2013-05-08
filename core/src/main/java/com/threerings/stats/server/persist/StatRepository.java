//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
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

package com.threerings.stats.server.persist;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.io.ByteArrayOutInputStream;
import com.samskivert.util.IntMap;
import com.samskivert.util.IntMaps;

import com.samskivert.depot.DatabaseException;
import com.samskivert.depot.DepotRepository;
import com.samskivert.depot.DuplicateKeyException;
import com.samskivert.depot.Funcs;
import com.samskivert.depot.Key;
import com.samskivert.depot.PersistenceContext;
import com.samskivert.depot.PersistentRecord;
import com.samskivert.depot.clause.FieldDefinition;
import com.samskivert.depot.clause.FromOverride;
import com.samskivert.depot.clause.QueryClause;
import com.samskivert.depot.clause.Where;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.stats.data.Stat;
import com.threerings.stats.data.StatModifier;

import static com.threerings.stats.Log.log;

/**
 * Responsible for the persistent storage of per-player statistics.
 */
@Singleton
public class StatRepository extends DepotRepository
    implements Stat.AuxDataSource
{
    /**
     * Constructs a new statistics repository with the specified persistence context.
     */
    @Inject public StatRepository (PersistenceContext context)
    {
        super(context);
    }

    /**
     * Applies a modification to a single stat. If the stat in question does not exist, a blank
     * instance will be created via {@link com.threerings.stats.data.Stat.Type#newStat}.
     *
     * @return the modified Stat, if any modification took place; or null if the modification had
     * no effect on the stat's data.
     */
    public <T extends Stat> T updateStat (int playerId, StatModifier<T> modifier)
    {
        Where where = new Where(StatRecord.PLAYER_ID, playerId,
                                StatRecord.STAT_CODE, modifier.getType().code());

        for (int ii = 0; ii < MAX_UPDATE_TRIES; ii++) {
            StatRecord record = load(StatRecord.class, where); // TODO: force cache skip on ii > 0
            Stat stat = (record == null) ? modifier.getType().newStat() :
                decodeStat(record.statCode, record.statData, record.modCount);
            @SuppressWarnings("unchecked") T tstat = (T)stat;
            modifier.modify(tstat);
            if (!tstat.isModified()) {
                return null;
            }
            if (updateStat(playerId, tstat, false)) {
                return tstat;
            }
        }

        throw new DatabaseException(
            "Unable to update stat after " + MAX_UPDATE_TRIES + " attempts " +
            "[stat=" + modifier.getType() + ", pid=" + playerId + "]");
    }

    /**
     * Loads the stats associated with the specified player.
     *
     */
    public ArrayList<Stat> loadStats (int playerId)
    {
        ArrayList<Stat> stats = Lists.newArrayList();
        Where where = new Where(StatRecord.PLAYER_ID, playerId);
        for (StatRecord record : findAll(StatRecord.class, where)) {
            Stat stat = decodeStat(record.statCode, record.statData, record.modCount);
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
    {
        deleteAll(StatRecord.class, new Where(StatRecord.PLAYER_ID, playerId));
    }

    /**
     * Writes out any of the stats in the supplied array that have been modified since they were
     * first loaded. Exceptions that occur while writing the stats will be caught and logged.
     */
    public void writeModified (int playerId, Stat[] stats)
    {
        writeModified(playerId, Arrays.asList(stats));
    }

    /**
     * Writes out any of the stats in the supplied iterable that have been modified since they were
     * first loaded. Exceptions that occur while writing the stats will be caught and logged.
     */
    public void writeModified (int playerId, Iterable<Stat> stats)
    {
        for (Stat stat : stats) {
            try {
                if (stat.getType().isPersistent() && stat.isModified()) {
                    updateStat(playerId, stat, true);
                }
            } catch (Exception e) {
                log.warning("Error flushing modified stat", "stat", stat, e);
            }
        }
    }

    // documentation inherited from interface Stat.AuxDataSource
    public int getStringCode (Stat.Type type, String value)
    {
        Map<String,Integer> map = _stringToCode.get(type);
        if (map == null) {
            _stringToCode.put(type, map = Maps.newHashMap());
        }
        Integer code = map.get(value);
        if (code == null) {
            try {
                code = assignStringCode(type, value);
            } catch (DatabaseException pe) {
                log.warning("Failed to assign code", "type", type, "value", value, pe);
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
        IntMap<String> map = _codeToString.get(type);
        String value = (map == null) ? null : map.get(code);
        if (value == null) {
            // our value may have been mapped on a different server, so refresh this mapping table
            // from the database; then try again
            try {
                loadStringCodes(type);
            } catch (DatabaseException pe) {
                log.warning("Failed to reload string codes", "type", type, "code", code, pe);
            }
            map = _codeToString.get(type);
            value = (map == null) ? null : map.get(code);
            if (value == null) {
                log.warning("Missing reverse maping", "type", type, "code", code);
                value = "__UNKNOWN:" + code + "__"; // we don't want to return null
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
     * Deletes all data associated with the supplied players.
     */
    public void purgePlayers (Collection<Integer> playerIds)
    {
        deleteAll(StatRecord.class, new Where(StatRecord.PLAYER_ID.in(playerIds)));
    }

    /**
     * Instantiates the appropriate stat class and decodes the stat from the data.
     */
    protected Stat decodeStat (int statCode, byte[] data, byte modCount)
    {
        Stat.Type type = Stat.getType(statCode);
        if (type == null) {
            log.warning("Unable to decode stat, unknown type", "code", statCode);
            return null;
        }
        return decodeStat(type.newStat(), data, modCount);
    }

    /**
     * Instantiates the appropriate stat class and decodes the stat from the data.
     */
    protected Stat decodeStat (Stat stat, byte[] data, byte modCount)
    {
        String errmsg = null;
        Exception error = null;

        try {
            // decode its contents from the serialized data
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            stat.unpersistFrom(new ObjectInputStream(bin), this);
            stat.setModCount(modCount);
            return stat;

        } catch (ClassNotFoundException cnfe) {
            error = cnfe;
            errmsg = "Unable to instantiate stat";

        } catch (IOException ioe) {
            error = ioe;
            errmsg = "Unable to decode stat";
        }

        log.warning(errmsg, "type", stat.getType(), error);
        return null;
    }

    /**
     * Updates the specified stat in the database, inserting it if necessary.
     *
     * @return true if the update was successful, false if it failed due to the stat being
     * simultaneously modified by another database client.
     */
    protected boolean updateStat (int playerId, final Stat stat, boolean forceWrite)
    {
        ByteArrayOutInputStream out = new ByteArrayOutInputStream();
        try {
            stat.persistTo(new ObjectOutputStream(out), this);
        } catch (IOException ioe) {
            throw new DatabaseException("Error serializing stat " + stat, ioe);
        }

        byte[] data = out.toByteArray();
        byte nextModCount = (byte)((stat.getModCount() + 1) % Byte.MAX_VALUE);
        Key<StatRecord> key = StatRecord.getKey(playerId, stat.getCode());

        // update the row in the database only if it has the expected modCount
        int numRows = updatePartial(
            StatRecord.class,
            new Where(StatRecord.PLAYER_ID, playerId,
                      StatRecord.STAT_CODE, stat.getCode(),
                      StatRecord.MOD_COUNT, stat.getModCount()),
            key,
            StatRecord.STAT_DATA, data, StatRecord.MOD_COUNT, nextModCount);

        // if we failed to update any rows, it could be because we saw an unexpected modCount, or
        // because the stat did not already exist in the repo
        if (numRows == 0) {
            // if it didn't exist, let's try to create it
            if (load(StatRecord.class, key) == null) {
                try {
                    insert(new StatRecord(playerId, stat.getCode(), data, nextModCount));
                    numRows = 1;
                } catch (DuplicateKeyException e) {
                    // someone else inserted the StatRecord before we were able to
                    numRows = 0;
                }
            }

            // if it did exist but we collided with another writer, we may want to write anyway
            if (numRows == 0 && forceWrite) {
                log.warning("Possible collision while storing StatRecord",
                            "playerId", playerId, "stat", stat.getType().name(),
                            "modCount", nextModCount, "overwriting", load(StatRecord.class, key));
                store(new StatRecord(playerId, stat.getCode(), data, nextModCount));
                numRows = 1;
            }
        }

        return (numRows > 0);
    }

    /** Helper function for {@link #getStringCode}. */
    protected Integer assignStringCode (final Stat.Type type, final String value)
    {
        for (int ii = 0; ii < 10; ii++) {
            MaxStatCodeRecord maxRecord = load(
                MaxStatCodeRecord.class,
                new FromOverride(StringCodeRecord.class),
                new FieldDefinition(MaxStatCodeRecord.MAX_CODE, Funcs.max(StringCodeRecord.CODE)),
                new Where(StringCodeRecord.STAT_CODE, type.code()));

            int code = maxRecord != null ? maxRecord.maxCode + 1 : 1;

            // DEBUG: uncomment this to test code collision
            // if (ii == 0 && code > 0) {
            //     code = code-1;
            // }

            try {
                insert(new StringCodeRecord(type.code(), value, code));
                return code;

            } catch (DatabaseException pe) {
                // if this is not a duplicate row exception, something is booched and we just fail
                if (!(pe instanceof DuplicateKeyException)) {
                    throw pe;
                }

                // if it is a duplicate row exception, possibly someone inserted our value before
                // we could, in which case we can just look up the new mapping
                StringCodeRecord record = load(
                    StringCodeRecord.class, StringCodeRecord.getKey(type.code(), value));
                if (record != null) {
                    log.info("Value collision assigning string code", "type", type, "value", value);
                    return code;
                }

                // otherwise someone used the code we were trying to use and we just need to loop
                // around and get the next highest code
                log.info("Code collision assigning string code", "type", type, "value", value);
            }
        }
        throw new DatabaseException(
            "Unable to assign code after 10 attempts [type=" + type + ", value=" + value + "]");
    }

    /** Helper function used at repository startup. */
    protected void loadStringCodes (Stat.Type type)
    {
        QueryClause[] clauses;
        if (type != null) {
            clauses = new QueryClause[] { new Where(StringCodeRecord.STAT_CODE, type.code()) };
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
        Map<String,Integer> fmap = _stringToCode.get(type);
        if (fmap == null) {
            _stringToCode.put(type, fmap = Maps.newHashMap());
        }
        fmap.put(value, code);
        IntMap<String> rmap = _codeToString.get(type);
        if (rmap == null) {
            _codeToString.put(type, rmap = IntMaps.newHashIntMap());
        }
        rmap.put(code, value);
    }

    @Override // from DepotRepository
    protected void init ()
    {
        super.init();

        // load up our string set mappings
        loadStringCodes(null);
    }

    @Override // from DepotRepository
    protected void getManagedRecords (Set<Class<? extends PersistentRecord>> classes)
    {
        classes.add(StatRecord.class);
        classes.add(StringCodeRecord.class);
    }

    protected Map<Stat.Type,Map<String,Integer>> _stringToCode = Maps.newHashMap();
    protected Map<Stat.Type,IntMap<String>> _codeToString = Maps.newHashMap();

    protected static final int MAX_UPDATE_TRIES = 5;
}
