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

package com.threerings.ezgame.data;

import java.io.IOException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.samskivert.util.ObjectUtil;

import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

import com.threerings.presents.dobj.DSet;

import com.threerings.parlor.game.data.GameObject;
import com.threerings.parlor.turn.data.TurnGameObject;

import com.threerings.ezgame.util.EZObjectMarshaller;

/**
 * Contains the data for an ez game.
 */
public class EZGameObject extends GameObject
    implements TurnGameObject
{
    /** The identifier for a MessageEvent containing a user message. */
    public static final String USER_MESSAGE = "Umsg";

    /** The identifier for a MessageEvent containing game-system chat. */
    public static final String GAME_CHAT = "Uchat";

    /** The identifier for a MessageEvent containing ticker notifications. */
    public static final String TICKER = "Utick";

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>controllerOid</code> field. */
    public static final String CONTROLLER_OID = "controllerOid";

    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>userCookies</code> field. */
    public static final String USER_COOKIES = "userCookies";

    /** The field name of the <code>ezGameService</code> field. */
    public static final String EZ_GAME_SERVICE = "ezGameService";
    // AUTO-GENERATED: FIELDS END

    /** The client that is in control of this game. The first client to enter will be assigned
     * control and control will subsequently be reassigned if that client disconnects or leaves. */
    public int controllerOid;

    /** The current turn holder. */
    public Name turnHolder;

    /** A set of loaded user cookies. */
    public DSet<UserCookie> userCookies;

    /** The service interface for requesting special things from the server. */
    public EZGameMarshaller ezGameService;

    /**
     * Access the underlying user properties
     */
    public HashMap<String, Object> getUserProps ()
    {
        return _props;
    }

    // from TurnGameObject
    public String getTurnHolderFieldName ()
    {
        return TURN_HOLDER;
    }

    // from TurnGameObject
    public Name getTurnHolder ()
    {
        return turnHolder;
    }

    // from TurnGameObject
    public Name[] getPlayers ()
    {
        return players;
    }

    /**
     * Called by PropertySetEvent to effect the property update.
     * 
     * @return the old value.
     */
    public Object applyPropertySet (String propName, Object data, int index)
    {
        Object oldValue = _props.get(propName);
        if (index >= 0) {
            if (isOnServer()) {
                byte[][] arr = (oldValue instanceof byte[][])
                    ? (byte[][]) oldValue : null;
                if (arr == null || arr.length <= index) {
                    // TODO: in case a user sets element 0 and element 90000,
                    // we might want to store elements in a hash
                    byte[][] newArr = new byte[index + 1][];
                    if (arr != null) {
                        System.arraycopy(arr, 0, newArr, 0, arr.length);
                    }
                    _props.put(propName, newArr);
                    arr = newArr;
                }
                oldValue = arr[index];
                arr[index] = (byte[]) data;
                
            } else {
                Object[] arr = (oldValue instanceof Object[])
                    ? (Object[]) oldValue : null;
                if (arr == null || arr.length <= index) {
                    Object[] newArr = new Object[index + 1];
                    if (arr != null) {
                        System.arraycopy(arr, 0, newArr, 0, arr.length);
                    }
                    _props.put(propName, newArr);
                    arr = newArr;
                }
                oldValue = arr[index];
                arr[index] = data;
            }
            
        } else if (data != null) {
            _props.put(propName, data);

        } else {
            _props.remove(propName);
        }
    
        return oldValue;
    }

    /**
     * Test the specified property against the specified value. This is
     * called on the server to validate testAndSet events.
     *
     * @return true if the property contains the value specified.
     */
    public boolean testProperty (
        String propName, int index, Object testValue)
    {
        Object curValue = _props.get(propName);

        if (curValue != null && index >= 0) {
            // see if there's an array there already
            if (isOnServer()) {
                if (curValue instanceof byte[][]) {
                    byte[][] curArray = (byte[][]) curValue;
                    if (curArray.length > index) {
                        curValue = curArray[index];

                    } else {
                        // the index is out of range, but since we auto-grow,
                        // we treat it like null
                        curValue = null;
                    }

                } else {
                    // curData is not an array, so the test fails
                    return false;
                }

            } else {
                if (curValue instanceof Object[]) {
                    Object[] curArray = (Object[]) curValue;
                    if (curArray.length > index) {
                        curValue = curArray[index];

                    } else {
                        // the index is out of range, but since we auto-grow,
                        // we treat it like null
                        curValue = null;
                    }

                } else {
                    // curData is not an array, so the test fails
                    return false;
                }
            }
        }

        // let's test the values!
        if ((testValue instanceof Object[]) && (curValue instanceof Object[])) {
            // testing an array against another array
            return Arrays.deepEquals((Object[]) testValue, (Object[]) curValue);

        } else if ((testValue instanceof byte[]) && (curValue instanceof byte[])) {
            // testing a property against another property (may have
            // been from inside an array)
            return Arrays.equals((byte[]) testValue, (byte[]) curValue);

        // TODO: other array types must be tested if we're on the client
        // ??
        } else {
            // will catch null == null...
            return ObjectUtil.equals(testValue, curValue);
        }
    }
                
        

    // AUTO-GENERATED: METHODS START
    /**
     * Requests that the <code>controllerOid</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setControllerOid (int value)
    {
        int ovalue = this.controllerOid;
        requestAttributeChange(
            CONTROLLER_OID, Integer.valueOf(value), Integer.valueOf(ovalue));
        this.controllerOid = value;
    }

    /**
     * Requests that the <code>turnHolder</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setTurnHolder (Name value)
    {
        Name ovalue = this.turnHolder;
        requestAttributeChange(
            TURN_HOLDER, value, ovalue);
        this.turnHolder = value;
    }

    /**
     * Requests that the specified entry be added to the
     * <code>userCookies</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void addToUserCookies (UserCookie elem)
    {
        requestEntryAdd(USER_COOKIES, userCookies, elem);
    }

    /**
     * Requests that the entry matching the supplied key be removed from
     * the <code>userCookies</code> set. The set will not change until the
     * event is actually propagated through the system.
     */
    public void removeFromUserCookies (Comparable key)
    {
        requestEntryRemove(USER_COOKIES, userCookies, key);
    }

    /**
     * Requests that the specified entry be updated in the
     * <code>userCookies</code> set. The set will not change until the event is
     * actually propagated through the system.
     */
    public void updateUserCookies (UserCookie elem)
    {
        requestEntryUpdate(USER_COOKIES, userCookies, elem);
    }

    /**
     * Requests that the <code>userCookies</code> field be set to the
     * specified value. Generally one only adds, updates and removes
     * entries of a distributed set, but certain situations call for a
     * complete replacement of the set value. The local value will be
     * updated immediately and an event will be propagated through the
     * system to notify all listeners that the attribute did
     * change. Proxied copies of this object (on clients) will apply the
     * value change when they received the attribute changed notification.
     */
    public void setUserCookies (DSet<com.threerings.ezgame.data.UserCookie> value)
    {
        requestAttributeChange(USER_COOKIES, value, this.userCookies);
        @SuppressWarnings("unchecked") DSet<com.threerings.ezgame.data.UserCookie> clone =
            (value == null) ? null : value.typedClone();
        this.userCookies = clone;
    }

    /**
     * Requests that the <code>ezGameService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setEzGameService (EZGameMarshaller value)
    {
        EZGameMarshaller ovalue = this.ezGameService;
        requestAttributeChange(
            EZ_GAME_SERVICE, value, ovalue);
        this.ezGameService = value;
    }
    // AUTO-GENERATED: METHODS END

    /**
     * A custom serialization method.
     */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.defaultWriteObject();

        if (isOnServer()) {
            // write the number of properties, followed by each one
            out.writeInt(_props.size());
            for (Map.Entry<String, Object> entry : _props.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeObject(entry.getValue());
            }
        } else {
            throw new IllegalStateException();
        }
    }

    /**
     * A custom serialization method.
     */
    public void readObject (ObjectInputStream ins)
        throws IOException, ClassNotFoundException
    {
        ins.defaultReadObject();

        _props.clear();
        int count = ins.readInt();
        boolean onClient = !isOnServer();
        while (count-- > 0) {
            String key = ins.readUTF();
            Object o = ins.readObject();
            if (onClient) {
                o = EZObjectMarshaller.decode(o);
            }
            _props.put(key, o);
        }
    }

    /**
     * Called internally and by PropertySetEvent to determine if we're
     * on the server or on the client.
     */
    boolean isOnServer ()
    {
        return _omgr.isManager(this);
    }

    /** The current state of game data.
     * On the server, this will be a byte[] for normal properties
     * and a byte[][] for array properties.
     * On the client, the actual values are kept whole.
     */
    protected transient HashMap<String, Object> _props =
        new HashMap<String, Object>();
}
