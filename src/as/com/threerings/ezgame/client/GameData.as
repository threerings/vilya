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

import flash.errors.IllegalOperationError;

import flash.utils.Proxy;

import flash.utils.flash_proxy;

use namespace flash_proxy;

public class GameData extends Proxy
{
    public function GameData (propSetFn :Function, props :Object)
    {
        _propSetFn = propSetFn;
        _props = props;
    }

    public function hasOwnProperty (propName :String) :Boolean
    {
        // pass-through
        return _props.hasOwnProperty(propName);
    }

    public function propertyIsEnumerable (propName :String) :Boolean
    {
        // pass-through
        return _props.propertyIsEnumerable(propName);
    }

    public function setPropertyIsEnumerable (
        propName :String, isEnum :Boolean = true) :void
    {
        // pass-through
        _props.setPropertyIsEnumerable(propName, isEnum);
    }

    override flash_proxy function callProperty (propName :*, ... rest) :*
    {
        // don't allow function calls
        throw new IllegalOperationError();
    }

    override flash_proxy function getDescendants (name :*) :*
    {
        // we don't need this XML business
        throw new IllegalOperationError();
    }

    override flash_proxy function isAttribute (name :*) :Boolean
    {
        // we don't need this XML business
        throw new IllegalOperationError();
    }

    override flash_proxy function getProperty (propName :*) :*
    {
        // pass-through
        return _props[propName];
    }

    override flash_proxy function hasProperty (propName :*) :Boolean
    {
        // pass-through
        return (_props[propName] !== undefined);
    }

    override flash_proxy function setProperty (propName :*, value :*) :void
    {
        _propSetFn(String(propName), value, -1);
    }

    override flash_proxy function deleteProperty (propName :*) :Boolean
    {
        var hasProp :Boolean = hasProperty(propName);
        setProperty(propName, null);
        return hasProp;
    }

    override flash_proxy function nextNameIndex (index :int) :int
    {
        // possibly set up the property list on the first call
        if (index == 0) {
            _propertyList = [];
            for (var prop :String in _props) {
                _propertyList.push(prop);
            }
        }

        // return a 1-based index to indicate that there is a property
        if (index < _propertyList.length) {
            return index + 1;

        } else {
            // we're done, clear the prop list
            _propertyList = null;
            return 0;
        }
    }

    override flash_proxy function nextName (index :int) :String
    {
        // the index is 1-based, so subtract one
        return (_propertyList[index - 1] as String);
    }

    override flash_proxy function nextValue (index :int) :*
    {
        return _props[nextName(index)];
    }

    /** The function which we pass property setting to. */
    protected var _propSetFn :Function;

    /** The object we're proxying. */
    protected var _props :Object = { };

    /** Used temporarily while iterating over our names or values. */
    protected var _propertyList :Array;
}
}
