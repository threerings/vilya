package com.threerings.ezgame;

/**
 * Property change events are dispatched after the property change was
 * validated on the server.
 */
public class PropertyChangedEvent extends EZEvent
{
    /**
     * Constructor.
     */
    public PropertyChangedEvent (
        EZGame ezgame, String propName, Object newValue, Object oldValue,
        int index)
    {
        super(ezgame);
        _name = propName;
        _newValue = newValue;
        _oldValue = oldValue;
        _index = index;
    }

    /**
     * Get the name of the property that changed.
     */
    public String getName ()
    {
        return _name;
    }

    /**
     * Get the property's new value.
     */
    public Object getNewValue ()
    {
        return _newValue;
    }

    /**
     * Get the property's previous value (handy!).
     */
    public Object getOldValue ()
    {
        return _oldValue;
    }
    
    /**
     * If an array element was updated, get the index, or -1 if not applicable.
     */
    public int getIndex ()
    {
        return _index;
    }

    @Override
    public String toString ()
    {
        return "[PropertyChangedEvent name=" + _name + ", value=" + _newValue +
            ((_index < 0) ? "" : (", index=" + _index)) + "]";
    }

    /** Our implementation details. */
    protected String _name;
    protected Object _newValue;
    protected Object _oldValue;
    protected int _index;
}
