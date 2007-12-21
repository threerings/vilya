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

/**
 * Dispatched when a property has changed in the shared game state.
 *
 * @eventType com.threerings.ezgame.PropertyChangedEvent.TYPE
 */
[Event(name="PropChanged", type="com.threerings.ezgame.PropertyChangedEvent")]

/**
 * Dispatched when a message arrives with information that is not part of the shared game state.
 *
 * @eventType com.threerings.ezgame.MessageReceivedEvent.TYPE
 */
[Event(name="msgReceived", type="com.threerings.ezgame.MessageReceivedEvent")]

/**
 * Provides access to 'net' game services.
 */
public class EZNetSubControl extends AbstractSubControl
{
    public function EZNetSubControl (parent :AbstractGameControl)
    {
        super(parent);
    }

    /**
     * Get a property from data.
     */
    public function get (propName :String, index :int = -1) :Object
    {
        checkIsConnected();

        var value :Object = _gameData[propName];
        if (index >= 0) {
            if (value is Array) {
                return (value as Array)[index];

            } else {
                throw new ArgumentError("Property " + propName + " is not an array.");
            }
        }
        return value;
    }

    /**
     * Set a property that will be distributed. 
     */
    public function set (propName :String, value :Object, index :int = -1) :void
    {
        callHostCode("setProperty_v1", propName, value, index, false);
    }

    /**
     * Set a property, but have this client immediately set the value so that it can be
     * re-read. The property change event will still arrive and will be your clue as to when the
     * other clients will see the newly set value. Be careful with this method, as it can allow
     * data inconsistency: two clients may see different values for a property if one of them
     * recently set it immediately, and the resultant PropertyChangedEvent's oldValue also may not
     * be consistent.
     */
    public function setImmediate (propName :String, value :Object, index :int = -1) :void
    {
        callHostCode("setProperty_v1", propName, value, index, true);
    }

    /**
     * Set a property that will be distributed, but only if it's equal to the specified test value.
     *
     * <p> Please note that, unlike in the standard set() function, the property will not be
     * updated right away, but will require a request to the server and a response back. For this
     * reason, there may be a considerable delay between calling testAndSet, and seeing the result
     * of the update.
     *
     * <p> The operation is 'atomic', in the sense that testing and setting take place during the
     * same server event. In comparison, a separate 'get' followed by a 'set' operation would
     * involve two events with two network round-trips, and no guarantee that the value won't
     * change between the events.
     */
    public function testAndSet (
        propName :String, newValue :Object, testValue :Object, index :int = -1) :void
    {
        callHostCode("testAndSetProperty_v1", propName, newValue, testValue, index);
    }

    /**
     * Get the names of all currently-set properties that begin with the specified prefix.
     */
    public function getPropertyNames (prefix :String = "") :Array
    {
        var props :Array = [];
        for (var s :String in _gameData) {
            if (s.lastIndexOf(prefix, 0) == 0) {
                props.push(s);
            }
        }
        return props;
    }

    /**
     * Send a "message" to other clients subscribed to the game.  These is similar to setting a
     * property, except that the value will not be saved- it will merely end up coming out as a
     * MessageReceivedEvent.
     *
     * @param messageName The message to send.
     * @param value The value to attach to the message.
     * @param playerId if 0 (or unset), sends to all players, otherwise the message will be private
     * to just one player
     */
    public function sendMessage (messageName :String, value :Object, playerId :int = 0) :void
    {
        callHostCode("sendMessage_v2", messageName, value, playerId);
    }

    override protected function populateProperties (o :Object) :void
    {
        super.populateProperties(o);

        o["propertyWasSet_v1"] = propertyWasSet_v1;
        o["messageReceived_v1"] = messageReceived_v1;
    }

    override protected function setHostProps (o :Object) :void
    {
        super.setHostProps(o);

        _gameData = o.gameData;
    }

    /**
     * Private method to post a PropertyChangedEvent.
     */
    private function propertyWasSet_v1 (
        name :String, newValue :Object, oldValue :Object, index :int) :void
    {
        dispatch(new PropertyChangedEvent(_parent, name, newValue, oldValue, index));
    }

    /**
     * Private method to post a MessageReceivedEvent.
     */
    private function messageReceived_v1 (name :String, value :Object) :void
    {
        dispatch(new MessageReceivedEvent(_parent, name, value));
    }

    /** Game properties. */
    protected var _gameData :Object;
}
}
