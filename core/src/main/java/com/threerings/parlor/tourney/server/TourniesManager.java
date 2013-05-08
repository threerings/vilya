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

package com.threerings.parlor.tourney.server;

import java.util.ArrayList;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.Lifecycle;

import com.threerings.presents.client.InvocationService;
import com.threerings.presents.data.ClientObject;
import com.threerings.presents.dobj.RootDObjectManager;
import com.threerings.presents.server.InvocationException;

import com.threerings.parlor.tourney.data.TourneyConfig;
import com.threerings.parlor.tourney.server.persist.TourneyRepository;

import static com.threerings.parlor.Log.log;

/**
 * An extensible tournament manager.
 */
@Singleton
public abstract class TourniesManager
    implements TourniesProvider, Lifecycle.InitComponent
{
    @Inject public TourniesManager (Lifecycle cycle)
    {
        cycle.addComponent(this);
    }

    // from interface Lifecycle.Component
    public void init ()
    {
        loadTourneyConfigs();

        _omgr.newInterval(new Runnable() {
            public void run () {
                updateTournies();
            }
        }).schedule(getIntervalDelay(), true);
    }

    // from interface TourniesService
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
        TourneyManager tmgr = _injector.getInstance(getTourneyManagerClass());
        int tournId = _tourneyCount++;
        int tournOid = tmgr.init(config, tournId);
        _tourneys.put(tournId, tmgr);
        if (listener != null) {
            listener.requestProcessed(tournOid);
        }
    }

    /**
     * Called by the tourney manager to remove itself from the tournies.
     */
    protected void releaseTourney (Comparable<?> key)
    {
        _tourneys.remove(key);
    }

    /**
     * Load all the tournament configuration information stored in the repository.
     */
    protected void loadTourneyConfigs ()
    {
        try {
            ArrayList<TourneyConfig> tournies = _tournrep.loadTournies();
            for (TourneyConfig config : tournies) {
                makeTourney(config, null);
            }
        } catch (PersistenceException pe) {
            log.warning("Failed to load tourney configurations.", pe);
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
     * Returns the derivation of {@link TourneyManager} to use.
     */
    protected abstract Class<? extends TourneyManager> getTourneyManagerClass ();

    /**
     * Returns the tourney interval delay in milliseconds.
     */
    protected abstract long getIntervalDelay ();

    /** Count to provide a unique key to tournies as they're created. */
    protected int _tourneyCount;

    /** Holds all the current tournies in the game. */
    protected Map<Comparable<?>, TourneyManager> _tourneys = Maps.newHashMap();

    // our dependencies
    @Inject protected Injector _injector;
    @Inject protected RootDObjectManager _omgr;
    @Inject protected TourneyRepository _tournrep;
}
