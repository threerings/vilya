package com.threerings.ezgame;

public class MessageReceivedEvent extends EZEvent
{
    public MessageReceivedEvent (
        EZGame ezgame, String messageName, Object value)
    {
        super(ezgame);
        _name = messageName;
        _value = value;
    }

    /**
     * Access the message name.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Access the message value.
     */
    public Object getValue ()
    {
        return _value;
    }

    public String toString ()
    {
        return "[MessageReceivedEvent name=" + _name +
            ", value=" + _value + "]";
    }

    protected String _name;
    protected Object _value;
}
