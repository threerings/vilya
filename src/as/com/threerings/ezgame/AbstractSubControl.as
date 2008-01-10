//
// $Id: SubControl.as 271 2007-04-07 00:25:58Z dhoover $
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

import flash.errors.IllegalOperationError;

/**
 * Abstract base class. Do not instantiate.
 */
public class AbstractSubControl extends AbstractControl
{
    public function AbstractSubControl (parent :AbstractControl)
    {
        super();
        if (parent == null || Object(this).constructor == AbstractSubControl) {
            throw new IllegalOperationError("Abstract");
        }

        _parent = parent;
    }

    /**
     * @inheritDoc
     */
    override public function isConnected () :Boolean
    {
        return _parent.isConnected();
    }

    /**
     * @inheritDoc
     */
    override public function doBatch (fn :Function) :void
    {
        return _parent.doBatch(fn);
    }

    /**
     * @private
     */
    override protected function callHostCode (name :String, ... args) :*
    {
        return _parent.callHostCodeFriend(name, args);
    }

    /**
     * @private
     */
    internal function populatePropertiesFriend (o :Object) :void
    {
        populateProperties(o);
    }

    /**
     * @private
     */
    internal function setHostPropsFriend (o :Object) :void
    {
        setHostProps(o);
    }

    /** @private */
    protected var _parent :AbstractControl;
}
}
