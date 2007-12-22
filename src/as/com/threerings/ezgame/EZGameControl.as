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

package com.threerings.ezgame {

import flash.display.DisplayObject;

/**
 * The single point of control for each client in your multiplayer EZGame.
 *
 * Usage: Usually, in your top-level movieclip/sprite:
 * _ctrl = new EZGameControl(this);
 * 
 */
public class EZGameControl extends AbstractGameControl
{
    /**
     * Create an EZGameControl object using some display object currently on the hierarchy.
     *
     * @param disp the display object that is the game's UI.
     * @param autoReady if true, the game will automatically be started when initialization is
     * complete, if false, the game will not start until all clients call {@link #playerReady}.
     */
    public function EZGameControl (disp :DisplayObject, autoReady :Boolean = true)
    {
        super(disp, autoReady);
    }

    /**
     * Access the 'local' services.
     */
    public function get local () :EZLocalSubControl
    {
        return _localCtrl;
    }

    /**
     * Access the 'net' services.
     */
    public function get net () :EZNetSubControl
    {
        return _netCtrl;
    }

    /**
     * Access the 'player' services.
     */
    public function get player () :EZPlayerSubControl
    {
        return _playerCtrl;
    }

    /**
     * Access the 'game' services.
     */
    public function get game () :EZGameSubControl
    {
        return _gameCtrl;
    }

    /**
     * Access the 'services' services.
     */
    public function get services () :EZServicesSubControl
    {
        return _servicesCtrl;
    }
}
}
