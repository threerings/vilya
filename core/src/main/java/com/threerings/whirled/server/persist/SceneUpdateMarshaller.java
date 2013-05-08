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

package com.threerings.whirled.server.persist;

import java.util.HashMap;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.google.common.collect.Maps;

import com.samskivert.io.PersistenceException;
import com.samskivert.util.HashIntMap;

import com.threerings.io.ObjectInputStream;
import com.threerings.io.ObjectOutputStream;

import com.threerings.whirled.data.SceneUpdate;

/**
 * A utility class to assist the management of scene updates by a SceneRepository.
 */
public class SceneUpdateMarshaller
{
    /**
     * Create a SceneUpdateMarshaller that understands the update types specified.
     *
     * Note: Once a type is registered and in use by some SceneRepository that type id can never be
     * used again. If you need to remove an update type, it should be replaced with null in the
     * class list to reserve the old type id that it represented.
     */
    public SceneUpdateMarshaller (Class<?> ... typesClasses)
    {
        for (Class<?> c : typesClasses) {
            registerUpdateClass(c);
        }
    }

    /**
     * Returns the type code that is assigned to the specified SceneUpdate instance, or -1.
     */
    public int getUpdateType (SceneUpdate update)
    {
        return (update == null) ? -1 : getUpdateType(update.getClass());
    }

    /**
     * Returns the type code that is assigned to the specified SceneUpdate class, or -1.
     */
    public int getUpdateType (Class<?> typeClass)
    {
        Integer type = _classToType.get(typeClass);
        return (type == null) ? -1 : type.intValue();
    }

    /**
     * Returns the update class associated with the specified type code, or null.
     */
    public Class<?> getUpdateClass (int type)
    {
        return _typeToClass.get(type);
    }

    /**
     * Persists the specified update to a new ByteArrayOutInputStream.
     */
    public byte[] persistUpdate (SceneUpdate update)
        throws PersistenceException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            update.persistTo(new ObjectOutputStream(out));
        } catch (IOException ioe) {
            throw new PersistenceException("Error serializing update " + update, ioe);
        }
        return out.toByteArray();
    }

    /**
     * Instantiates the appropriate update class and decodes the update from the data.
     */
    public SceneUpdate decodeUpdate (int sceneId, int sceneVersion, int updateType, byte[] data)
        throws PersistenceException
    {
        String errmsg = null;
        Exception error = null;

        try {
            Class<?> updateClass = getUpdateClass(updateType);
            if (updateClass == null) {
                errmsg = "No class registered for update type [sceneId=" + sceneId +
                    ", sceneVersion=" + sceneVersion + ", updateType=" + updateType + "].";
                throw new PersistenceException(errmsg);
            }

            // create the update
            SceneUpdate update = (SceneUpdate)updateClass.newInstance();
            update.init(sceneId, sceneVersion);

            // decode its contents from the serialized data
            ByteArrayInputStream bin = new ByteArrayInputStream(data);
            update.unpersistFrom(new ObjectInputStream(bin));
            return update;

        } catch (IOException ioe) {
            error = ioe;
            errmsg = "Unable to decode update";

        } catch (ClassNotFoundException cnfe) {
            error = cnfe;
            errmsg = "Unable to instantiate update";

        } catch (InstantiationException ie) {
            error = ie;
            errmsg = "Unable to instantiate update";
        } catch (IllegalAccessException iae) {
            error = iae;
            errmsg = "Unable to instantiate update";
        }

        errmsg += " [sceneId=" + sceneId + ", sceneVersion=" + sceneVersion +
            ", updateType=" + updateType + "].";
        throw new PersistenceException(errmsg, error);
    }

    /**
     * Registers the update class with the update factory. This should be called below in the
     * canonical list of update registrations.
     */
    protected void registerUpdateClass (Class<?> typeClass)
    {
        // ensure that callers can't fuck up the reciprocal nature of our two maps.
        if (_classToType.containsKey(typeClass)) {
            throw new IllegalArgumentException("Class already registered: " + typeClass);
        }

        // always reserve the type Id
        int type = ++_nextType;

        // but only register non-null classes
        if (typeClass != null) {
            _typeToClass.put(type, typeClass);
            _classToType.put(typeClass, Integer.valueOf(type));
        }
    }

    /** The table mapping update types to classes. */
    protected HashIntMap<Class<?>> _typeToClass = new HashIntMap<Class<?>>();

    /** The table mapping update classes to types. */
    protected HashMap<Class<?>, Integer> _classToType = Maps.newHashMap();

    /** A counter used in assigning update types to classes. */
    protected int _nextType = 0;
}
