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

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamable;
import com.threerings.io.Streamer;
import com.threerings.util.ActionScript;
 
import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.NamedEvent;

import com.threerings.ezgame.util.EZObjectMarshaller;

/**
 * Represents a property change on the actionscript object we use in EZGameObject.
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
    public PropertySetEvent (int targetOid, String propName, Object value, int index, Object ovalue)
    {
        super(targetOid, propName);
        _data = value;
        _index = index;
        _oldValue = ovalue;
    }

    /**
     * Returns the value that was set for the property.
     */
    public Object getValue ()
    {
        return _data;
    }

    /**
     * Returns the old value.
     */
    public Object getOldValue ()
    {
        return _oldValue;
    }

    /**
     * Returns the index, or -1 if not applicable.
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
        if (_oldValue == UNSET_OLD_VALUE) {
            // only apply the property change if we haven't already
            _oldValue = ezObj.applyPropertySet(_name, _data, _index);
        }
        return true;
    }

    @Override
    protected void notifyListener (Object listener)
    {
        if (listener instanceof PropertySetListener) {
            ((PropertySetListener) listener).propertyWasSet(this);
        }
    }

    @Override @ActionScript(name="toStringBuf")
    protected void toString (StringBuilder buf)
    {
        buf.append("PropertySetEvent ");
        super.toString(buf);
        buf.append(", index=").append(_index);
    }

    /** The index of the property, if applicable. */
    protected int _index;

    /** The client-side data that is assigned to this property. */
    protected Object _data;

    /** The old value. */
    protected transient Object _oldValue = UNSET_OLD_VALUE;
}
