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

import flash.utils.ByteArray;
import flash.utils.IExternalizable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

import com.threerings.util.StringBuilder;

import com.threerings.presents.dobj.DObject;
import com.threerings.presents.dobj.NamedEvent;

import com.threerings.ezgame.util.EZObjectMarshaller;

/**
 * Represents a property change on the actionscript object we
 * use in FlashGameObject.
 */
public class PropertySetEvent extends NamedEvent
{
    /**
     * Create a PropertySetEvent.
     */
    public function PropertySetEvent () // unserialize-only
    {
        super(0, null);
    }

    // from abstract DEvent
    override public function applyToObject (target :DObject) :Boolean
    {
        // since we're in actionscript, we're always on the client
        _oldValue = EZGameObject(target).applyPropertySet(_name, _data, _index);
        return true;
    }

    /**
     * Get the value that was set for the property.
     */
    public function getValue () :Object
    {
        return _data;
    }

    /**
     * Get the index, or -1 if not applicable.
     */
    public function getIndex () :int
    {
        return _index;
    }

    /**
     * Get the old value.
     */
    public function getOldValue () :Object
    {
        return _oldValue;
    }

    // from interface Streamable
    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);
        _index = ins.readInt();
        _data = EZObjectMarshaller.decode(ins.readObject());
    }

    // from interface Streamable
    override public function writeObject (out :ObjectOutputStream) :void
    {
        super.writeObject(out);
        out.writeInt(_index);
        out.writeObject(_data);
    }

    override protected function notifyListener (listener :Object) :void
    {
        if (listener is PropertySetListener) {
            (listener as PropertySetListener).propertyWasSet(this);
        }
    }

    override protected function toStringBuf (buf :StringBuilder) :void
    {
        buf.append("PropertySetEvent ");
        super.toStringBuf(buf);
        buf.append(", index=").append(_index);
    }

    /** The index of the property, if applicable. */
    protected var _index :int;

    /** The client-side data that is assigned to this property. */
    protected var _data :Object;

    /** The old value. */
    protected var _oldValue :Object;
}
}
