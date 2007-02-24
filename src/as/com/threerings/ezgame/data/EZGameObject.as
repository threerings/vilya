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

package com.threerings.ezgame.data {

import flash.events.Event;

import flash.utils.ByteArray;

import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.TypedArray;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;

import com.threerings.ezgame.util.EZObjectMarshaller;

public class EZGameObject extends GameObject
    implements TurnGameObject
{
    /** The identifier for a MessageEvent containing a user message. */
    public static const USER_MESSAGE :String = "Umsg";

    /** The identifier for a MessageEvent containing game-system chat. */
    public static const GAME_CHAT :String = "Uchat";

    /** The identifier for a MessageEvent containing ticker notifications. */
    public static const TICKER :String = "Utick";

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>turnHolder</code> field. */
    public static const TURN_HOLDER :String = "turnHolder";

    /** The field name of the <code>userCookies</code> field. */
    public static const USER_COOKIES :String = "userCookies";

    /** The field name of the <code>ezGameService</code> field. */
    public static const EZ_GAME_SERVICE :String = "ezGameService";
    // AUTO-GENERATED: FIELDS END

    /** The current turn holder. */
    public var turnHolder :Name;

    /** A set of loaded user cookies. */
    public var userCookies :DSet;

    /** The service interface for requesting special things from the server. */
    public var ezGameService :EZGameMarshaller;

    /**
     * Access the underlying user properties.
     */
    public function getUserProps () :Object
    {
        return _props;
    }

    // from TurnGameObject
    public function getTurnHolderFieldName () :String
    {
        return TURN_HOLDER;
    }

    // from TurnGameObject
    public function getTurnHolder () :Name
    {
        return turnHolder;
    }

    // from TurnGameObject
    public function getPlayers () :TypedArray /* of Name */
    {
        return players;
    }

//    // AUTO-GENERATED: METHODS START
//    /**
//     * Requests that the <code>turnHolder</code> field be set to the
//     * specified value. The local value will be updated immediately and an
//     * event will be propagated through the system to notify all listeners
//     * that the attribute did change. Proxied copies of this object (on
//     * clients) will apply the value change when they received the
//     * attribute changed notification.
//     */
//    public function setTurnHolder (value :Name) :void
//    {
//        var ovalue :Name = this.turnHolder;
//        requestAttributeChange(
//            TURN_HOLDER, value, ovalue);
//        this.turnHolder = value;
//    }
//    // AUTO-GENERATED: METHODS END

    /**
     * Called by a PropertySetEvent to enact a property change.
     * @return the old value
     */
    public function applyPropertySet (
        propName :String, value :Object, index :int) :Object
    {
        var oldValue :Object = _props[propName];
        if (index >= 0) {
            // set an array element
            var arr :Array = (oldValue as Array);
            if (arr == null) {
                arr = [];
                _props[propName] = arr;
            }
            oldValue = arr[index];
            arr[index] = value;

        } else if (value != null) {
            // normal property set
            _props[propName] = value;

        } else {
            // remove a property
            delete _props[propName];
        }
        return oldValue;
    }

//    override public function writeObject (out :ObjectOutputStream) :void
//    {
//        super.writeObject(out);
//
//        out.writeObject(turnHolder);
//        out.writeObject(ezGameService);
//
//        out.writeInt(_props.length);
//        for (var key :String in _props) {
//            out.writeUTF(key);
//            out.writeObject(EZObjectMarshaller.encode(_props[key]));
//        }
//    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        // first read any regular bits
        readDefaultFields(ins);

        // then user properties
        var count :int = ins.readInt();
        while (count-- > 0) {
            var key :String = ins.readUTF();
            var value :Object = EZObjectMarshaller.decode(ins.readObject());
            _props[key] = value;
        }
    }

    /**
     * Reads the fields written by the default serializer for this instance.
     */
    protected function readDefaultFields (ins :ObjectInputStream) :void
    {
        turnHolder = (ins.readObject() as Name);
        userCookies = (ins.readObject() as DSet);
        ezGameService = (ins.readObject() as EZGameMarshaller);
    }
    
    /** The raw properties set by the game. */
    protected var _props :Object = new Object();
}
}
