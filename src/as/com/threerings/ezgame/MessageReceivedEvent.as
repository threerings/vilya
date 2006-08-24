package com.threerings.ezgame {

import flash.events.Event;

public class MessageReceivedEvent extends EZEvent
{
    /** The type of all MessageReceivedEvents. */
    public static const TYPE :String = "msgReceived";

    /**
     * Access the message name.
     */
    public function get name () :String
    {
        return _name;
    }

    /**
     * Access the message value.
     */
    public function get value () :Object
    {
        return _value;
    }

    public function MessageReceivedEvent (
        ezgame :EZGame, messageName :String, value :Object)
    {
        super(TYPE, ezgame);
        _name = messageName;
        _value = value;
    }

    override public function toString () :String
    {
        return "[MessageReceivedEvent name=" + _name +
            ", value=" + _value + "]";
    }

    override public function clone () :Event
    {
        return new MessageReceivedEvent(_ezgame, _name, _value);
    }

    protected var _name :String;
    protected var _value :Object;
}
}
