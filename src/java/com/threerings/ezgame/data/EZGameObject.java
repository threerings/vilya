//
// $Id$

package com.threerings.ezgame.data;

import java.io.IOException;

import java.util.HashMap;
import java.util.Map;

import com.threerings.util.Name;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;

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

    // AUTO-GENERATED: FIELDS START
    /** The field name of the <code>turnHolder</code> field. */
    public static final String TURN_HOLDER = "turnHolder";

    /** The field name of the <code>ezGameService</code> field. */
    public static final String EZ_GAME_SERVICE = "ezGameService";
    // AUTO-GENERATED: FIELDS END

    /** The current turn holder. */
    public Name turnHolder;

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

    // AUTO-GENERATED: METHODS START
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
     * Requests that the <code>ezGameService</code> field be set to the
     * specified value. The local value will be updated immediately and an
     * event will be propagated through the system to notify all listeners
     * that the attribute did change. Proxied copies of this object (on
     * clients) will apply the value change when they received the
     * attribute changed notification.
     */
    public void setEZGameService (EZGameMarshaller value)
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
