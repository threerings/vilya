//
// $Id$

package com.threerings.stats.data;

import java.io.IOException;
import java.io.Serializable;
import java.util.zip.CRC32;

import com.samskivert.util.HashIntMap;
import com.samskivert.util.StringUtil;
import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.presents.dobj.DSet;

import static com.threerings.stats.Log.log;

/**
 * A base class for persistent statistics tracked on a per-player basis (some for a single game,
 * others for all time).
 */
public abstract class Stat
    implements DSet.Entry, Cloneable
{
    /**
     * Defines the various per-player tracked statistics.
     */
    public interface Type extends Serializable
    {
        /** Returns a new blank stat instance of the specified type. */
        public Stat newStat ();

        /** Returns the enum name of stat. */
        public String name ();

        /** Returns the unique code for this stat which is a function of its name. */
        public int code ();

        /** Returns true if this stat is persisted between sessions. */
        public boolean isPersistent ();
    };

    /** Provides auxilliary information to statistics during the persisting process. */
    public static interface AuxDataSource
    {
        /** Maps the specified string to a unique integer value. */
        public int getStringCode (Type type, String value);

        /** Maps the specified unique code back to its string value. */
        public String getCodeString (Type type, int code);
    }

    /**
     * Maps a {@link Type}'s code code back to a {@link Type} instance.
     */
    public static Type getType (int code)
    {
        return _codeToType.get(code);
    }

    /**
     * Used by the {@link Type} implementation to map itself to an integer code.
     *
     * @return the code to which the supplied type was mapped.
     */
    public static int initType (Type type, Stat prototype)
    {
        // compute a code for this stat using the CRC32 hash of its name
        _crc.reset();
        _crc.update(type.name().getBytes());
        int code = (int)_crc.getValue();

        // make sure the code does not collide
        if (_codeToType.containsKey(code)) {
            log.warning("Stat type collision! " + type.name() + " and " +
                        _codeToType.get(code).name() + " both map to '" + code + "'.");
            return -1;
        }

        // initialize the prototype
        prototype._type = type;

        // map the stat by its code
        _codeToType.put(code, type);
        return code;
    }

    /**
     * Returns the type of this statistic.
     */
    public Type getType ()
    {
        return _type;
    }

    /**
     * Returns the integer code to which this statistic's name maps.
     */
    public int getCode ()
    {
        return _type.code();
    }

    /**
     * Returns true if the supplied statistic has been modified since it
     * was loaded from the repository.
     */
    public boolean isModified ()
    {
        return _modified;
    }

    /**
     * Forces this stat to consider itself modified. Generally this is not
     * called but rather the derived class will update its modified state when
     * it is actually modified.
     */
    public void setModified (boolean modified)
    {
        _modified = modified;
    }

    /**
     * Returns the modification count of this Stat when it was loaded from the repository.
     */
    public byte getModCount ()
    {
        return _modCount;
    }

    /**
     * Sets the modification count for this Stat. StatRepository calls this when converting
     * a StatRecord into a Stat; it shouldn't be called otherwise.
     */
    public void setModCount (byte modCount)
    {
        _modCount = modCount;
    }

    /** Writes our custom streamable fields. */
    public void writeObject (ObjectOutputStream out)
        throws IOException
    {
        out.writeInt(_type.code());
        out.defaultWriteObject();
    }

    /** Reads our custom streamable fields. */
    public void readObject (ObjectInputStream in)
        throws IOException, ClassNotFoundException
    {
        _type = getType(in.readInt());
        in.defaultReadObject();
    }

    /**
     * Serializes this instance for storage in the item database. Derived classes must override
     * this method to implement persistence.
     */
    public abstract void persistTo (ObjectOutputStream out, AuxDataSource aux)
        throws IOException;

    /**
     * Unserializes this item from data obtained from the item database.  Derived classes must
     * override this method to implement persistence.
     */
    public abstract void unpersistFrom (ObjectInputStream in, AuxDataSource aux)
        throws IOException, ClassNotFoundException;

    @Override
    public String toString ()
    {
        StringBuffer buf = new StringBuffer(StringUtil.toUSLowerCase(_type.name()));
        buf.append("=");
        buf.append(valueToString());
        return buf.toString();
    }

    /**
     * Derived statistics must override this method and render their value to a string. Used by
     * {@link #toString} and to display the value in game.
     */
    public abstract String valueToString ();

    // documentation inherited from DSet.Entry
    public String getKey ()
    {
        return _type.name();
    }

    @Override
    public Object clone ()
    {
        try {
            return super.clone();
        } catch (Exception e) {
            throw new RuntimeException("Clone failed", e);
        }
    }

    /** The type of the statistic in question. */
    protected transient Type _type;

    /** Indicates whether or not this statistic has been modified since it
     * was loaded from the database. */
    protected transient boolean _modified;

    /** The last known modification count for this stat, if it was read from the database. */
    protected transient byte _modCount;

    /** Used for computing our stat codes. */
    protected static CRC32 _crc = new CRC32();

    /** The table mapping stat codes to enumerated types. */
    protected static HashIntMap<Type> _codeToType = new HashIntMap<Type>();
}
