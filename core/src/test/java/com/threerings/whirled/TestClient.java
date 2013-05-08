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

package com.threerings.whirled;

import com.threerings.presents.client.Client;

import com.threerings.crowd.client.LocationObserver;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.whirled.client.SceneDirector;
import com.threerings.whirled.client.persist.SceneRepository;
import com.threerings.whirled.data.Scene;
import com.threerings.whirled.data.SceneImpl;
import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.util.SceneFactory;
import com.threerings.whirled.util.WhirledContext;

import static com.threerings.crowd.Log.log;

public class TestClient extends com.threerings.crowd.client.TestClient
    implements LocationObserver
{
    public TestClient (String username)
    {
        super(username);

        // create the handles for our various services
        _screp = new DummyClientSceneRepository();

        SceneFactory sfact = new SceneFactory() {
            public Scene createScene (SceneModel model, PlaceConfig config) {
                return new SceneImpl(model, config);
            }
        };
        _scdir = new SceneDirector(_ctx, _locdir, _screp, sfact);

        // we want to know about location changes
        _locdir.addLocationObserver(this);
    }

    @Override
    public void clientDidLogon (Client client)
    {
        // we specifically do not call super()

        log.info("Client did logon [client=" + client + "].");

        // request to move to scene 0
        _ctx.getSceneDirector().moveTo(0);
    }

    public boolean locationMayChange (int placeId)
    {
        // we're easy
        return true;
    }

    public void locationDidChange (PlaceObject place)
    {
        log.info("At new location [plobj=" + place + ", scene=" + _scdir.getScene() + "].");
    }

    public void locationChangeFailed (int placeId, String reason)
    {
        log.warning("Location change failed [plid=" + placeId + ", reason=" + reason + "].");
    }

    @Override
    protected CrowdContext createContext ()
    {
        return (_ctx = new WhirledContextImpl());
    }

    public static void main (String[] args)
    {
        // create our test client
        TestClient tclient = new TestClient("test");
        // start it running
        tclient.run();
    }

    protected class WhirledContextImpl
        extends com.threerings.crowd.client.TestClient.CrowdContextImpl
        implements WhirledContext
    {
        public SceneDirector getSceneDirector ()
        {
            return _scdir;
        }
    }

    protected WhirledContext _ctx;
    protected SceneDirector _scdir;
    protected SceneRepository _screp;
}
