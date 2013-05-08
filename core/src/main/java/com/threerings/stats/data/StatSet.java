//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2012 Three Rings Design, Inc., All Rights Reserved
// http://code.google.com/p/vilya/
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

package com.threerings.stats.data;

import java.util.Iterator;

import com.threerings.presents.dobj.DSet;

/**
 * A distributed set containing {@link Stat} objects.
 */
public class StatSet extends DSet<Stat>
{
    /** An interface to be implemented by an entity that wishes to be notified when the contents of
     * a stat set change. */
    public interface Container
    {
        public void addToStats (Stat stat);
        public void updateStats (Stat stat);
    }

    /** Creates a stat set with the specified contents. */
    public StatSet (Iterable<Stat> contents)
    {
        super(contents);
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
     * Updates a stat in this set, using the supplied StatModifier. This function should
     * only be called after a successful stat modification has been made to the StatRepository.
     * It will increment the Stat's modCount, and won't set its dirty bit if it's not already
     * set, to prevent the Stat from being re-written to the repo unnecessarily.
     */
    public <T extends Stat> void syncStat (StatModifier<T> modifier)
    {
        boolean wasModified = false;
        @SuppressWarnings("unchecked") T stat = (T)getStat(modifier.getType());
        if (stat != null) {
            wasModified = stat.isModified();
            modifier.modify(stat);
            updateStat(stat, true);

        } else {
            @SuppressWarnings("unchecked") T nstat = (T)modifier.getType().newStat();
            stat = nstat;
            modifier.modify(stat);
            addStat(stat, true);
        }

        stat.setModified(wasModified);
        stat.setModCount((byte)((stat.getModCount() + 1) % Byte.MAX_VALUE));
    }

    /**
     * Sets an integer statistic in this set.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntStat}.
     */
    public void setStat (Stat.Type type, int value)
    {
        IntStat stat = (IntStat)getStat(type);
        if (stat == null) {
            stat = (IntStat)type.newStat();
            stat.setValue(value);
            addStat(stat, false);
        } else if (stat.setValue(value)) {
            updateStat(stat, false);
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
        IntStat stat = (IntStat)getStat(type);
        if (stat == null) {
            stat = (IntStat)type.newStat();
            stat.increment(delta);
            addStat(stat, false);
        } else if (stat.increment(delta)) {
            updateStat(stat, false);
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
        IntArrayStat stat = (IntArrayStat)getStat(type);
        if (stat == null) {
            stat = (IntArrayStat)type.newStat();
            stat.appendValue(value);
            addStat(stat, false);
        } else {
            stat.appendValue(value);
            updateStat(stat, false);
        }
    }

    /**
     * Adds a value to a {@link SetStat}.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not
     * an {@link SetStat} parameterized on the given type.
     */
    public <T> void addToSetStat (Stat.Type type, T value)
    {
        @SuppressWarnings("unchecked") SetStat<T> stat = (SetStat<T>)getStat(type);
        if (stat == null) {
            @SuppressWarnings("unchecked") SetStat<T> nstat = (SetStat<T>)type.newStat();
            stat = nstat;
            stat.add(value);
            addStat(stat, false);
        } else if (stat.add(value)) {
            updateStat(stat, false);
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
        StringMapStat stat = (StringMapStat)getStat(type);
        if (stat == null) {
            stat = (StringMapStat)type.newStat();
            stat.increment(value, amount);
            addStat(stat, false);
        } else if (stat.increment(value, amount)) {
            updateStat(stat, false);
        }
    }

    /**
     * Returns the current value of the specified integer statistic.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntStat}.
     */
    public int getIntStat (Stat.Type type)
    {
        IntStat stat = (IntStat)getStat(type);
        return (stat == null) ? 0 : stat.getValue();
    }

    /**
     * Returns the maximum value by which the specified integer statistic has ever been
     * incremented.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link MaxIntStat}.
     */
    public int getMaxIntStat (Stat.Type type)
    {
        MaxIntStat stat = (MaxIntStat)getStat(type);
        return (stat == null) ? 0 : stat.getMaxValue();
    }

    /**
     * Returns the current value of the specified integer array statistic.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link IntArrayStat}.
     */
    public int[] getIntArrayStat (Stat.Type type)
    {
        IntArrayStat stat = (IntArrayStat)getStat(type);
        return (stat == null) ? new int[0] : stat.getValue();
    }

    /**
     * Returns the current size of the specified SetStat statistic.
     *
     * @exception ClassCastException thrown if the registered type of the specified stat is not an
     * {@link SetStat}.
     */
    public int getSetStatSize (Stat.Type type)
    {
        SetStat<?> stat = (SetStat<?>)getStat(type);
        return (stat == null ? 0 : stat.size());
    }

    /**
     * Returns true if the specified {@link SetStat} contains the specified value, false
     * otherwise.
     */
    public <T> boolean containsValue (Stat.Type type, T value)
    {
        @SuppressWarnings("unchecked") SetStat<T> stat = (SetStat<T>)getStat(type);
        return (stat == null) ? false : stat.contains(value);
    }

    /**
     * Returns the value to which the specified string is mapped in a {@link StringMapStat} or zero
     * if the value has not been mapped.
     */
    public int getMapValue (Stat.Type type, String value)
    {
        StringMapStat stat = (StringMapStat)getStat(type);
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

    /**
     * Adds the specified Stat to the set.
     *
     * @param syncingWithRepo should be set to true only when called from {@link #syncStat},
     * which itself is called only when a stat modification has been made to the database,
     * and the in-memory StatSet needs to be updated as a result.
     */
    protected void addStat (Stat stat, boolean syncingWithRepo)
    {
        if (_container != null) {
            _container.addToStats(stat);
        } else {
            add(stat);
        }
    }

    /**
     * Updates the specified Stat in the set.
     *
     * @param syncingWithRepo should be set to true only when called from {@link #syncStat},
     * which itself is called only when a stat modification has been made to the database,
     * and the in-memory StatSet needs to be updated as a result.
     */
    protected void updateStat (Stat stat, boolean syncingWithRepo)
    {
        if (_container != null) {
            _container.updateStats(stat);
        }
    }

    /**
     * Returns the Stat of the given type, if it exists in the set, and null otherwise.
     */
    protected Stat getStat (Stat.Type type)
    {
        return get(type.name());
    }

    protected transient Container _container;
}
