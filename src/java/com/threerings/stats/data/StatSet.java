//
// $Id$

package com.threerings.stats.data;

import java.util.Iterator;

import com.threerings.media.Log;
import com.threerings.presents.dobj.DSet;

/**
 * A distributed set containing {@link Stat} objects.
 */
public final class StatSet extends DSet<Stat>
{
    /** An interface to be implemented by an entity that wishes to be notified when the contents of
     * a stat set change. */
    public interface Container
    {
        public void addToStats (Stat stat);
        public void updateStats (Stat stat);
    }

    /** Creates a stat set with the specified contents. */
    public StatSet (Iterator<Stat> contents)
    {
        super(contents);
    }

    /** Creates a blank stat set. */
    public StatSet ()
    {
    }

    /**
     * Wires this stat set up to a containing user object. All subsequent modifications will be
     * published to the container.
     */
    public void setContainer (Container container)
    {
        _container = container;
    }

    /**
     * Updates a stat in this set, using the supplied StatModifier.
     */
    @SuppressWarnings("unchecked")
    public <T extends Stat> void updateStat (StatModifier<T> modifier)
    {
        Stat stat = getStat(modifier.getType());
        if (stat != null) {
            modifier.modify((T)stat);
            updateStat(stat);

        } else {
            stat = modifier.getType().newStat();
            modifier.modify((T)stat);
            addStat(stat);
        }
    }

    /**
     * Sets an integer statistic in this set.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntStat}.
     */
    public void setStat (Stat.Type type, int value)
    {
        IntStat stat = (IntStat)get(type.name());
        if (stat == null) {
            stat = (IntStat)type.newStat();
            stat.setValue(value);
            addStat(stat);
        } else if (stat.setValue(value)) {
            updateStat(stat);
        }
    }

    /**
     * Sets an integer stat to the specified value, if it exceeds our existing recorded value.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntStat}.
     */
    public void maxStat (Stat.Type type, int value)
    {
        int ovalue = getIntStat(type);
        if (value > ovalue) {
            setStat(type, value);
        }
    }

    /**
     * Increments an integer statistic in this set.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntStat}.
     */
    public void incrementStat (Stat.Type type, int delta)
    {
        IntStat stat = (IntStat)get(type.name());
        if (stat == null) {
            stat = (IntStat)type.newStat();
            stat.increment(delta);
            addStat(stat);
        } else if (stat.increment(delta)) {
            updateStat(stat);
        }
    }

    /**
     * Appends an integer value to an {@link IntArrayStat}.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntArrayStat}.
     */
    public void appendStat (Stat.Type type, int value)
    {
        IntArrayStat stat = (IntArrayStat)get(type.name());
        if (stat == null) {
            stat = (IntArrayStat)type.newStat();
            stat.appendValue(value);
            addStat(stat);
        } else {
            stat.appendValue(value);
            updateStat(stat);
        }
    }

    /**
     * Adds a string value to a {@link StringSetStat}.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link StringSetStat}.
     */
    public void addToSetStat (Stat.Type type, String value)
    {
        StringSetStat stat = (StringSetStat)get(type.name());
        if (stat == null) {
            stat = (StringSetStat)type.newStat();
            stat.add(value);
            addStat(stat);
        } else if (stat.add(value)) {
            updateStat(stat);
        }
    }

    /**
     * Adds an int value to a {@link IntSetStat}.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntSetStat}.
     */
    public void addToSetStat (Stat.Type type, int value)
    {
        IntSetStat stat = (IntSetStat)get(type.name());
        if (stat == null) {
            stat = (IntSetStat)type.newStat();
            stat.add(value);
            addStat(stat);
        } else if (stat.add(value)) {
            updateStat(stat);
        }
    }

    /**
     * Increments a string value in a {@link StringMapStat}.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link StringMapStat}.
     */
    public void incrementMapStat (Stat.Type type, String value, int amount)
    {
        StringMapStat stat = (StringMapStat)get(type.name());
        if (stat == null) {
            stat = (StringMapStat)type.newStat();
            stat.increment(value, amount);
            addStat(stat);
        } else if (stat.increment(value, amount)) {
            updateStat(stat);
        }
    }

    /**
     * Returns the current value of the specified integer statistic.
     */
    public int getIntStat (Stat.Type type)
    {
        IntStat stat = (IntStat)get(type.name());
        return (stat == null) ? 0 : stat.getValue();
    }

    /**
     * Returns the maximum value by which the specified integer statistic has ever been
     * incremented.
     */
    public int getMaxIntStat (Stat.Type type)
    {
        MaxIntStat stat = (MaxIntStat)get(type.name());
        return (stat == null) ? 0 : stat.getMaxValue();
    }

    /**
     * Returns the current value of the specified integer array statistic.
     */
    public int[] getIntArrayStat (Stat.Type type)
    {
        IntArrayStat stat = (IntArrayStat)get(type.name());
        return (stat == null) ? new int[0] : stat.getValue();
    }

    /**
     * Returns true if the specified {@link StringSetStat} contains the specified value, false
     * otherwise.
     */
    public boolean containsValue (Stat.Type type, String value)
    {
        StringSetStat stat = (StringSetStat)get(type.name());
        return (stat == null) ? false : stat.contains(value);
    }

    /**
     * Returns the value to which the specified string is mapped in a {@link StringMapStat} or zero
     * if the value has not been mapped.
     */
    public int getMapValue (Stat.Type type, String value)
    {
        StringMapStat stat = (StringMapStat)get(type.name());
        return (stat == null) ? 0 : stat.get(value);
    }

    /**
     * Don't call this method, it's only needed by some tricky business we do when preventing
     * distributed object event generation for all but the bounty criteria related stats.
     */
    public void addQuietly (Stat stat)
    {
        add(stat);
    }

    protected void addStat (Stat stat)
    {
        if (_container != null) {
            _container.addToStats(stat);
        } else {
            add(stat);
        }

        Log.log.info("StatSet.addStat()", "Stat", stat);
    }

    protected void updateStat (Stat stat)
    {
        if (_container != null) {
            _container.updateStats(stat);
        }

        Log.log.info("StatSet.updateStat()", "Stat", stat);
    }

    /**
     * Returns the Stat of the given type, if it exists in the set, and null otherwise.
     */
    protected Stat getStat (Stat.Type type)
    {
        Stat stat = get(type.name());
        return (stat != null ? stat : null);
    }

    protected transient Container _container;
}
