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

package com.threerings.parlor.tourney.server.persist;

import java.sql.Connection;
import java.sql.SQLException;

import java.util.ArrayList;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.samskivert.io.PersistenceException;

import com.samskivert.jdbc.ConnectionProvider;
import com.samskivert.jdbc.DatabaseLiaison;
import com.samskivert.jdbc.JDBCUtil;
import com.samskivert.jdbc.JORARepository;
import com.samskivert.jdbc.jora.Table;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.parlor.tourney.data.TourneyConfig;

import static com.threerings.parlor.Log.log;

/**
 * The persistent store for tourney related information.
 */
@Singleton
public class TourneyRepository extends JORARepository
{
    /** Contains tourney information loaded from the database. */
    public static class TourneyRecord
    {
        /** The tourney's unique identifier. */
        public int tourneyId;

        /** The tourney configuration. */
        public byte[] config;

        public TourneyRecord ()
        {
        }

        public TourneyRecord (int tourneyId)
        {
            this.tourneyId = tourneyId;
        }

        public TourneyRecord (TourneyConfig tourneyConfig)
        {
            this(tourneyConfig.tourneyId);
            ByteArrayOutputStream bstream = new ByteArrayOutputStream();
            ObjectOutputStream ostream = new ObjectOutputStream(bstream);

            try {
                ostream.writeObject(config);
                config = bstream.toByteArray();

            } catch (IOException e) {
                log.warning("Error writing TourneyConfig to byte array [e=" + e + "].");
            }

        }

        public TourneyConfig getTourneyConfig ()
        {
            ObjectInputStream ostream = new ObjectInputStream(new ByteArrayInputStream(config));
            TourneyConfig tconfig = null;

            try {
                tconfig = (TourneyConfig)ostream.readObject();

            } catch (Exception e) {
                log.warning("Error reading TourneyConfig from byte array [e=" + e + "].");
            }
            return tconfig;
        }
    }

    /** The database identifier used when establishing a database connection. This value being
     * <code>tourneydb</code>. */
    public static final String TOURNEY_DB_IDENT = "tourneydb";

    @Inject public TourneyRepository (ConnectionProvider conprov)
    {
        super(conprov, TOURNEY_DB_IDENT);
    }

    /**
     * Inserts a new tourney into the repository, assigning a unique id to the tourney.
     */
    public void insertTourney (TourneyConfig tourney)
        throws PersistenceException
    {
        tourney.tourneyId = insert(_ttable, new TourneyRecord(tourney));
    }

    /**
     * Updates the tourney in the repository.
     */
    public void updateTourney (TourneyConfig tourney)
        throws PersistenceException
    {
        store(_ttable, new TourneyRecord(tourney));
    }

    /**
     * Deletes a tourney from the repository.
     */
    public void deleteTourney (int tourneyId)
        throws PersistenceException
    {
        delete(_ttable, new TourneyRecord(tourneyId));
    }

    /**
     * Loads all the tourney configs from the repository.
     */
    public ArrayList<TourneyConfig> loadTournies ()
        throws PersistenceException
    {
        ArrayList<TourneyRecord> recordList = loadAll(_ttable, "");
        ArrayList<TourneyConfig> configList = Lists.newArrayListWithCapacity(recordList.size());
        for (TourneyRecord record : recordList) {
            configList.add(record.getTourneyConfig());
        }
        return configList;
    }

    @Override
    protected void migrateSchema (Connection conn, DatabaseLiaison liaison)
        throws SQLException, PersistenceException
    {
        JDBCUtil.createTableIfMissing(conn, "TOURNEYS", new String[] {
            "TOURNEY_ID INTEGER NOT NULL AUTO_INCREMENT PRIMARY KEY",
            "CONFIG BLOB"
        }, "");
    }

    @Override
    protected void createTables ()
    {
        _ttable = new Table<TourneyRecord>(TourneyRecord.class, "TOURNEYS", "TOURNEY_ID", true);
    }

    protected Table<TourneyRecord> _ttable;
}
