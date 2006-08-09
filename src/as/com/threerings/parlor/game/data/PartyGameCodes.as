package com.threerings.parlor.game.data {

public class PartyGameCodes
{
    /** Party game constant indicating that this game, while it does
     * implement PartyGameConfig, is not currently being played in party
     * mode. */
    public static const NOT_PARTY_GAME :int = 0;

    /** Party game constant indicating that we're in a party game in which
     * players must sit at an available seat to play, otherwise they're
     * an observer. */
    public static const SEATED_PARTY_GAME :int = 1;

    /** Party game constant indicating that everyone in the game place is
     * a "player", meaning that they do not need to claim a seat to play. */
    public static const FREE_FOR_ALL_PARTY_GAME :int = 2;
}
}
