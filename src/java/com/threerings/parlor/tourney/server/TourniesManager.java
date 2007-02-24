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

package com.threerings.parlor.tourney.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.samskivert.util.Interval;
import com.samskivert.util.RunQueue;

import com.samskivert.io.PersistenceException;
import com.samskivert.jdbc.ConnectionProvider;

import com.threerings.parlor.tourney.server.persist.TourneyRepository;
import com.threerings.parlor.tourney.data.Prize;
import com.threerings.parlor.tourney.data.TourneyConfig;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.server.InvocationException;
import com.threerings.presents.server.PresentsServer;
import com.threerings.presents.data.ClientObject;

/**
 * An extensible tournament manager.
 */
public abstract class TourniesManager
    implements TourniesProvider, PresentsServer.Shutdowner
{
    public TourniesManager (ConnectionProvider conprov)
        throws PersistenceException
    {
        _tournrep = new TourneyRepository(conprov, getDBIdent());

        loadTourneyConfigs();
    }

    public void init ()
    {
        _interval = new Interval(getRunQueue()) {
            public void expired () {
                updateTournies();
            }
        };
        _interval.schedule(getIntervalDelay(), true);
    }

    // documentation inherited from interface PresentsServer.Shutdowner
    public void shutdown ()
    {
        _interval.cancel();
    }

    // documentation inherited from interface TourniesService
    public void createTourney (ClientObject caller, TourneyConfig config, 
            final InvocationService.ResultListener listener)
        throws InvocationException
    {
        makeTourney(config, listener);
    }

    /**
     * Called to actually create a tourney once it has been validated and the prize has been
     * reserved.
     */
    protected void makeTourney (TourneyConfig config, InvocationService.ResultListener listener)
    {
        // create a new tourney manager which will run things
        Integer tourneyID = Integer.valueOf(_tourneyCount++);
        _tourneys.put(tourneyID, makeTourneyManager(config, tourneyID, listener));
    }

    /**
     * Called by the tourney manager to remove itself from the tournies.
     */
    protected void releaseTourney (Comparable key)
    {
        _tourneys.remove(key);
    }

    /**
     * Load all the tournament configuration information stored in the repository.
     */
    protected void loadTourneyConfigs ()
        throws PersistenceException
    {
        ArrayList<TourneyConfig> tournies = _tournrep.loadTournies();
        for (TourneyConfig config : tournies) {
            makeTourney(config, null);
        }

    }

    /**
     * Called to update loaded tournies, possibly announcing tourney information or start a
     * pending tourney.
     */
    protected void updateTournies ()
    {
    }

    /**
     * Instantiates a new tourney manager.
     */
    protected abstract TourneyManager makeTourneyManager(
            TourneyConfig config, Comparable key, InvocationService.ResultListener listener);

    /**
     * Returns the database identifier for our repository.
     */
    protected abstract String getDBIdent ();

    /**
     * Returns the RunQueue to use for our tourney interval.
     */
    protected abstract RunQueue getRunQueue ();

    /**
     * Returns the tourney interval delay in milliseconds.
     */
    protected abstract long getIntervalDelay ();

    /** Holds all the current tournies in the game. */
    protected HashMap<Comparable, TourneyManager> _tourneys = 
        new HashMap<Comparable, TourneyManager>();

    /** Reference to our tourney repository. */
    protected TourneyRepository _tournrep;

    /** The interval which updates loaded tournies. */
    protected Interval _interval;

    /** Count to provide a unique key to tournies as they're created. */
    protected int _tourneyCount;
}
