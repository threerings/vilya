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
 * Contains 'bags' game services. Do not instantiate this class yourself, access it
 * via GameControl.services.bags.
 *
 * Bags are secret collections containing non-unique elements that are stored on the server.
 * They can be used to implement game features where clients can't be trusted to not
 * sniff their network.
 * 
 * For example you could create a bag called "dice" and fill it with [ 1, 2, 3, 4, 5, 6 ].
 * Now you can roll the die with _ctrl.services.bags.pick("dice", 1, "diceProperty");
 */
public class EZBagsSubControl extends AbstractSubControl
{
    /**
     * @private Constructed via EZGameControl.
     */
    public function EZBagsSubControl (parent :AbstractControl)
    {
        super(parent);
    }

    /**
     * Create a bag containing the specified values,
     * clearing any previous bag with the same name.
     */
    public function create (bagName :String, values :Array) :void
    {
        populate(bagName, values, true);
    }

    /**
     * Add values to an existing bag. If it doesn't exist, it will
     * be created.
     */
    public function addTo (bagName :String, values :Array) :void
    {
        populate(bagName, values, false);
    }

    /**
     * Merge all values from the specified bag into the other bag.
     * The source bag will be destroyed. The elements from
     * the source bag will be shuffled and appended to the end
     * of the destination bag.
     */
    public function merge (srcBag :String, intoBag :String) :void
    {
        callHostCode("mergeCollection_v1", srcBag, intoBag);
    }

    /**
     * Pick (do not remove) the specified number of elements from a bag,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     *
     * @param bagName the collection name.
     * @param count the number of elements to pick
     * @param msgOrPropName the name of the message or property
     *        that will contain the picked elements.
     * @param playerId if 0 (or unset), the picked elements should be
     *        set on the gameObject as a property for all to see.
     *        If a playerId is specified, only that player will receive
     *        the elements as a message.
     */
    // TODO: a way to specify exclusive picks vs. duplicate-OK picks?
    public function pick (
        bagName :String, count :int, msgOrPropName :String,
        playerId :int = 0) :void
    {
        getFrom(bagName, count, msgOrPropName, playerId, false, null);
    }

    /**
     * Deal (remove) the specified number of elements from a bag,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     *
     * @param bagName the collection name.
     * @param count the number of elements to pick
     * @param msgOrPropName the name of the message or property
     *        that will contain the picked elements.
     * @param playerId if 0 (or unset), the picked elements should be
     *        set on the gameObject as a property for all to see.
     *        If a playerId is specified, only that player will receive
     *        the elements as a message.
     */
    // TODO: figure out the method signature of the callback
    public function deal (
        bagName :String, count :int, msgOrPropName :String,
        callback :Function = null, playerId :int = 0) :void
    {
        getFrom(bagName, count, msgOrPropName, playerId, true, callback);
    }


    // == protected methods ==

    /**
     * Helper method for create and addTo.
     * @private
     */
    protected function populate (
        bagName :String, values :Array, clearExisting :Boolean) :void
    {
        callHostCode("populateCollection_v1", bagName, values, clearExisting);
    }

    /**
     * Helper method for pick and deal.
     * @private
     */
    protected function getFrom (
        bagName :String, count :int, msgOrPropName :String, playerId :int,
        consume :Boolean, callback :Function) :void
    {
        callHostCode("getFromCollection_v2", bagName, count, msgOrPropName,
            playerId, consume, callback);
    }
}
}
