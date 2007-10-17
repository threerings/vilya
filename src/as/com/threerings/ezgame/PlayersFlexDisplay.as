//
// $Id$

package com.threerings.ezgame {

import mx.core.ClassFactory;

import mx.collections.ArrayCollection;
import mx.collections.Sort;

import mx.containers.VBox;

import mx.controls.List;

/**
 * A players display in flex.
 * Feel free to subclass/rewrite/copy/alter/tweak/whatever as much as you want.
 * Check out ScorePlayersFlexDisplay for an example of a subclass.
 */
public class PlayersFlexDisplay extends VBox
{
    public function PlayersFlexDisplay ()
    {
        super();
    }
    
    /**
     * Called to configure this display to start displaying. This is separated from the
     * constructor to make subclassing easier.
     */
    public function configure (gameCtrl :EZGameControl, playersOnly :Boolean = false) :void
    {
        _ctrl = gameCtrl;
        _playersOnly = playersOnly;
        _ourId = _ctrl.getMyId();

        // set up the UI
        createInterface();

        // add listeners
        _ctrl.addEventListener(StateChangedEvent.TURN_CHANGED, handleTurnChanged);
        _ctrl.addEventListener(OccupantChangedEvent.OCCUPANT_ENTERED, handleOccupantEntered);
        _ctrl.addEventListener(OccupantChangedEvent.OCCUPANT_LEFT, handleOccupantLeft);

        // set up the sort for the collection
        var sort :Sort = new Sort();
        sort.compareFunction = sortFunction;
        _players.sort = sort;

        // find all the current occupants and add them to our list of players
        var list :Array;
        if (_playersOnly) {
            list = _ctrl.seating.getPlayerIds();

        } else {
            list = _ctrl.getOccupants();
        }
        for each (var occupantId :int in list) {
            _players.addItem(createRecord(occupantId));
        }
        updateList();
    }

    /**
     * Create the UI.
     */
    protected function createInterface () :void
    {
        _list = new List();
        _list.itemRenderer = new ClassFactory(getRendererClass());
        _list.dataProvider = _players;

        addChild(_list);
    }

    /**
     * Return the class to use as our List itemRenderer.
     */
    protected function getRendererClass () :Class
    {
        return SimplePlayerRenderer;
    }

    /**
     * Handle an update to the current turn.
     * This is only applicable for turn-based games.
     */
    protected function handleTurnChanged (event :StateChangedEvent) :void
    {
        // TODO
    }

    /**
     * Handle the addition of a new occupant to the game.
     */
    protected function handleOccupantEntered (event :OccupantChangedEvent) :void
    {
        // if we're only displaying players, and this isn't, then don't show 'em!
        if (_playersOnly && !event.player) {
            return;
        }

        _players.addItem(createRecord(event.occupantId));
    }

    /**
     * Handle the removal of an occupant to the game.
     */
    protected function handleOccupantLeft (event :OccupantChangedEvent) :void
    {
        for (var ii :int = _players.length - 1; ii >= 0; ii--) {
            var rec :Object = _players.getItemAt(ii);
            if (rec.id == event.occupantId) {
                _players.removeItemAt(ii);
                return;
            }
        }

        // don't worry if we never found the record..
    }

    /**
     * Should be called when records are added to the list, or list data has changed such
     * that it should be re-sorted.
     */
    protected function updateList () :void
    {
        _players.refresh();
    }

    /**
     * Create an associative hash to hold properties related to a player or occupant.
     * This class assumes that 'id' will hold the occupantId, and that 'name' will hold
     * the name. Other fields can be added and a custom renderer can be used to display those
     * fields.
     *
     * @return an Object populated with the occupant's properties, or null if the occupant
     * should be ommitted
     */
    protected function createRecord (occupantId :int) :Object
    {
        var record :Object = new Object();
        record.id = occupantId;
        record.name = _ctrl.getOccupantName(occupantId);

        return record;
    }

    /**
     * The sort function that will be used to display occupant records.
     *
     * @return -1 if rec1 should be first, 0 if they are equal (?), 1 if rec2 should be first.
     */
    protected function sortFunction (rec1 :Object, rec2 :Object, fields :Array = null) :int
    {
        // make our record always on top..
        if (rec1.id == _ourId) {
            return -1;
        }
        if (rec2.id == _ourId) {
            return 1;
        }

        // otherwise, sort by name
        if (rec1.name < rec2.name) {
            return -1;
        }
        if (rec1.name > rec2.name) {
            return 1;
        }
        return 0; // huh.. same damn name
    }

    /** The game control. */
    protected var _ctrl :EZGameControl;

    /** The occupantId of the occupant that's instatiated this widget. */
    protected var _ourId :int;

    /** Are we only displaying players (not occupants)? */
    protected var _playersOnly :Boolean;

    /** The List widget that displays the players. */
    protected var _list :List;

    /** The list of player data. */
    protected var _players :ArrayCollection = new ArrayCollection();
}
}
