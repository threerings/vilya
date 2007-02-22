package com.threerings.ezgame {

/**
 * Contains EZ methods related to collections.
 */
public class CollectionsControl extends SubControl
{
    public function CollectionsControl (ctrl :EZGameControl)
    {
        super(ctrl);
    }

    /**
     * Create a collection containing the specified values,
     * clearing any previous collection with the same name.
     */
    public function create (collName :String, values :Array) :void
    {
        populate(collName, values, true);
    }

    /**
     * Add to an existing collection. If it doesn't exist, it will
     * be created. The new values will be inserted randomly into the
     * collection.
     */
    public function addTo (collName :String, values :Array) :void
    {
        populate(collName, values, false);
    }

    /**
     * Merge the specified collection into the other collection.
     * The source collection will be destroyed. The elements from
     * The source collection will be shuffled and appended to the end
     * of the destination collection.
     */
    public function merge (srcColl :String, intoColl :String) :void
    {
        _ctrl.callEZCode("mergeCollection_v1", srcColl, intoColl);
    }

    /**
     * Pick (do not remove) the specified number of elements from a collection,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     *
     * @param collName the collection name.
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
        collName :String, count :int, msgOrPropName :String,
        playerId :int = 0) :void
    {
        getFrom(collName, count, msgOrPropName, playerId, false, null);
    }

    /**
     * Deal (remove) the specified number of elements from a collection,
     * and distribute them to a specific player or set them as a property
     * in the game data.
     *
     * @param collName the collection name.
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
        collName :String, count :int, msgOrPropName :String,
        callback :Function = null, playerId :int = 0) :void
    {
        getFrom(collName, count, msgOrPropName, playerId, true, callback);
    }


    // == protected methods ==

    /**
     * Helper method for create and addTo.
     */
    protected function populate (
        collName :String, values :Array, clearExisting :Boolean) :void
    {
        _ctrl.callEZCode("populateCollection_v1", collName, values, clearExisting);
    }

    /**
     * Helper method for pick and deal.
     */
    protected function getFrom (
        collName :String, count :int, msgOrPropName :String, playerId :int,
        consume :Boolean, callback :Function) :void
    {
        _ctrl.callEZCode("getFromCollection_v2", collName, count, msgOrPropName,
            playerId, consume, callback);
    }
}
}
