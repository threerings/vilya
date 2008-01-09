//
// $Id: EZGameTurnDelegate.java 523 2007-12-07 21:41:22Z ray $
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

package com.threerings.ezgame.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;

import com.samskivert.util.RandomUtil;

import com.threerings.util.Name;

import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.server.PlaceManager;

import com.threerings.parlor.game.server.GameManagerDelegate;

import com.threerings.parlor.turn.data.TurnGameObject;
import com.threerings.parlor.turn.server.TurnGameManager;

/**
 * A special turn delegate for seated ez games.
 */
public class EZPartyTurnDelegate extends GameManagerDelegate
    implements EZGameTurnDelegate
{
    @Override
    public void setPlaceManager (PlaceManager plmgr)
    {
        super.setPlaceManager(plmgr);
        _tgmgr = (TurnGameManager) plmgr;
    }

    @Override
    public void didStartup (PlaceObject plobj)
    {
        super.didStartup(plobj);
        _plobj = plobj;
        _turnGame = (TurnGameObject) plobj;
    }

    @Override
    public void bodyEntered (int bodyOid)
    {
        super.bodyEntered(bodyOid);

        if (_ordering != null) {
            _ordering.add(bodyOid);
        }
    }

    @Override
    public void bodyLeft (int bodyOid)
    {
        super.bodyLeft(bodyOid);

        if (_ordering != null) {
            _ordering.remove(bodyOid);
        }
    }

    @Override
    public void gameDidEnd ()
    {
        super.gameDidEnd();

        // we can forget about any ordering now
        _ordering = null;
        _currentHolderId = 0;
    }

    // from EZGameTurnDelegate
    public void endTurn (int nextPlayerId)
    {
        _tgmgr.turnDidEnd();
        if (!_turnGame.isInPlay()) {
            _turnGame.setTurnHolder(null);
            _currentHolderId = 0;
            return;
        }

        // set up the ordering if we haven't done so already
        if (_ordering == null) {
            createOrdering();
        }

        // try using the player-specified value
        if (nextPlayerId != 0 && setNextTurn(nextPlayerId)) {
            return;
        }

        for (int playerId : _ordering) {
            if ((playerId != _currentHolderId) && setNextTurn(playerId)) {
                return;
            }
        }

        // we may get to the end without finding a new turn holder. Oh well!
    }

    /**
     * Set the next turn holder to the specified id, returning true on success.
     */
    protected boolean setNextTurn (int nextPlayerId)
    {
        BodyObject nextPlayer = ((EZGameManager) _plmgr).getOccupantByOid(nextPlayerId);
        if (nextPlayer == null) {
            return false;
        }

        // if the last turn holder was still in there, move them to the end of the list now
        if (_ordering.remove(_currentHolderId)) {
            _ordering.add(_currentHolderId);
        }

        _tgmgr.turnWillStart();
        _turnGame.setTurnHolder(nextPlayer.getVisibleName());
        _tgmgr.turnDidStart();
        _currentHolderId = nextPlayerId;
        return true;
    }

    /**
     * Add all the occupant body oids to the _ordering list in a random order.
     */
    protected void createOrdering ()
    {
        ArrayList<Integer> list = new ArrayList<Integer>(_plobj.occupants.size());
        for (int ii = _plobj.occupants.size() - 1; ii >= 0; ii--) {
            list.add(_plobj.occupants.get(ii));
        }

        // randomize the list
        Collections.shuffle(list, RandomUtil.rand);

        // and start out the ordering using that random order
        _ordering = new LinkedHashSet<Integer>(list);
    }

    /** The place object. */
    protected PlaceObject _plobj;

    /** The game manager for which we are delegating. */
    protected TurnGameManager _tgmgr;

    /** A reference to our game object. */
    protected TurnGameObject _turnGame;

    /** Tracks the turn ordering for occupants. Initialized only if turns are used by the game. */
    protected LinkedHashSet<Integer> _ordering;

    /** The oid of the current turn holder. */
    protected int _currentHolderId;
}
