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

package com.threerings.ezgame.client {

import flash.events.Event;

import com.threerings.util.Name;

import com.threerings.presents.dobj.AttributeChangedEvent;

import com.threerings.crowd.client.PlaceView;
import com.threerings.crowd.data.BodyObject;
import com.threerings.crowd.data.PlaceConfig;
import com.threerings.crowd.data.PlaceObject;
import com.threerings.crowd.util.CrowdContext;

import com.threerings.parlor.game.client.GameController;
import com.threerings.parlor.game.data.GameObject;

import com.threerings.parlor.turn.client.TurnGameController;
import com.threerings.parlor.turn.client.TurnGameControllerDelegate;

import com.threerings.ezgame.data.EZGameObject;

/**
 * A controller for flash games.
 */
public class EZGameController extends GameController
    implements TurnGameController
{
    public function EZGameController ()
    {
        addDelegate(_turnDelegate = new TurnGameControllerDelegate(this));
    }

    /**
     * This is called by the GameControlBackend once it has initialized and made contact with
     * usercode.
     */
    public function userCodeIsConnected (autoReady :Boolean) :void
    {
        if (autoReady) {
            playerIsReady();
        }
    }

    /**
     * Called by the GameControlBackend when the game is ready to start. If the game has ended,
     * this can be called by all clients to start the game anew.
     */
    public function playerIsReady () :void
    {
        playerReady();
    }

    // from PlaceController
    override public function willEnterPlace (plobj :PlaceObject) :void
    {
        _ezObj = (plobj as EZGameObject);

        super.willEnterPlace(plobj);
    }

    // from PlaceController
    override public function didLeavePlace (plobj :PlaceObject) :void
    {
        super.didLeavePlace(plobj);

        _ezObj = null;
    }

    // from TurnGameController
    public function turnDidChange (turnHolder :Name) :void
    {
        _panel.backend.turnDidChange();
    }

    // from GameController
    override public function attributeChanged (event :AttributeChangedEvent) :void
    {
        var name :String = event.getName();
        if (EZGameObject.CONTROLLER_OID == name) {
            _panel.backend.controlDidChange();
        } else if (GameObject.ROUND_ID == name) {
            if ((event.getValue() as int) > 0) {
                _panel.backend.roundStateChanged(true);
            } else {
                _panel.backend.roundStateChanged(false);
            }
        } else {
            super.attributeChanged(event);
        }
    }

    // from GameController
    override protected function playerReady () :void
    {
        // we require the user to be connected, and we redundantly only do this if the user is in
        // the players array
        if (_panel.backend.isConnected()) {
            var bobj :BodyObject = (_ctx.getClient().getClientObject() as BodyObject);
            if (_gobj.getPlayerIndex(bobj.getVisibleName()) != -1) {
                super.playerReady();
            }

        } else {
            log.debug("Waiting to call playerReady, userCode not yet connected.");
        }
    }

    // from GameController
    override protected function gameDidStart () :void
    {
        super.gameDidStart();
        _panel.backend.gameStateChanged(true);
    }

    // from GameController
    override protected function gameDidEnd () :void
    {
        super.gameDidEnd();
        _panel.backend.gameStateChanged(false);
    }

    // from PlaceController
    override protected function createPlaceView (ctx :CrowdContext) :PlaceView
    {
        return new EZGamePanel(ctx, this);
    }

    // from PlaceController
    override protected function didInit () :void
    {
        super.didInit();

        // we can't just assign _panel in createPlaceView() for some exciting reason
        _panel = (_view as EZGamePanel);
    }

    protected var _ezObj :EZGameObject;
    protected var _turnDelegate :TurnGameControllerDelegate;
    protected var _panel :EZGamePanel;
}
}
