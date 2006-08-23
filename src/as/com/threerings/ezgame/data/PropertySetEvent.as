//
// $Id$

package com.threerings.ezgame.data {

import flash.utils.ByteArray;
import flash.utils.IExternalizable;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;
import com.threerings.io.Streamer;

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

    /**
     * Get the value that was set for the property.
     */
    public function getValue () :Object
    {
        return _data;
    }

    /**
     * Get the old value.
     */
    public function getOldValue () :Object
    {
        return _oldValue;
    }

    /**
     * Get the index, or -1 if not applicable.
     */
    public function getIndex () :int
    {
        return _index;
    }

    override public function applyToObject (target :DObject) :Boolean
    {
        _oldValue =
            EZGameObject(target).applyPropertySet(_name, _data, _index);
        return true;
    }

    override protected function notifyListener (listener :Object) :void
    {
        if (listener is PropertySetListener) {
            (listener as PropertySetListener).propertyWasSet(this);
        }
    }

    override public function readObject (ins :ObjectInputStream) :void
    {
        super.readObject(ins);

        _index = ins.readInt();
        _data = EZObjectMarshaller.decode(ins.readObject());
    }

    /** The index of the property, if applicable. */
    protected var _index :int;

    /** The client-side data that is assigned to this property. */
    protected var _data :Object;

    /** The old value. */
    protected var _oldValue :Object;
}
}
