package com.threerings.ezgame {

public class SeatingControl extends SubControl
{
    public function SeatingControl (ctrl :EZGameControl)
    {
        super(ctrl);
    }

    /**
     * Get the player's position (seated index), or -1 if not a player.
     */
    public function getPlayerPosition (playerId :int) :int
    {
        return int(_ctrl.callEZCode("getPlayerPosition_v1", playerId));
    }

    /**
     * Get all the players at the table, in their seated position.
     * Absent players will be represented by a 0.
     */
    public function getPlayerIds () :Array /* of playerId (int) */
    {
        return (_ctrl.callEZCode("getPlayers_v1") as Array);
    }

    // TODO: methods for allowing a player to pick a seat
}
}
