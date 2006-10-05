//
// $Id$

package com.threerings.ezgame.data;

import java.io.IOException;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;
import com.threerings.io.Streamer;
 
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.NamedEvent;

import com.threerings.ezgame.util.EZObjectMarshaller;

/**
 * Represents a property change on the actionscript object we
 * use in EZGameObject.
 */
public class PropertySetEvent extends NamedEvent
{
    /** Suitable for unserialization. */
    public PropertySetEvent ()
    {
    }

    /**
     * Create a PropertySetEvent.
     */
    public PropertySetEvent (
        int targetOid, String propName, Object value, int index)
    {
        super(targetOid, propName);
        _data = value;
        _index = index;
    }

    /**
     * Get the value that was set for the property.
     */
    public Object getValue ()
    {
        return _data;
    }

    /**
     * Get the old value.
     */
    public Object getOldValue ()
    {
        return _oldValue;
    }

    /**
     * Get the index, or -1 if not applicable.
     */
    public int getIndex ()
    {
        return _index;
    }

    // from abstract DEvent
    public boolean applyToObject (DObject target)
    {
        EZGameObject ezObj = (EZGameObject) target;
        if (!ezObj.isOnServer()) {
            _data = EZObjectMarshaller.decode(_data);
        }
        _oldValue = ezObj.applyPropertySet(_name, _data, _index);
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof PropertySetListener) {
            ((PropertySetListener) listener).propertyWasSet(this);
        }
    }

    /** The index of the property, if applicable. */
    protected int _index;

    /** The client-side data that is assigned to this property. */
    protected Object _data;

    /** The old value. */
    protected transient Object _oldValue;
}
