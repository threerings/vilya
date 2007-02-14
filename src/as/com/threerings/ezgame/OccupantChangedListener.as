package com.threerings.ezgame {

public interface OccupantChangedListener
{
    function occupantEntered (event :OccupantChangedEvent) :void;

    function occupantLeft (event :OccupantChangedEvent) :void;
}
}
