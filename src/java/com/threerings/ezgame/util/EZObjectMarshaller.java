package com.threerings.ezgame.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import java.lang.reflect.Array;

import java.util.ArrayList;

/**
 * Utility methods for transferring flash properties via
 * the presents dobj system.
 */
public class EZObjectMarshaller
{
    /**
     * Encode the specified object as either a byte[] or a byte[][] if it
     * is an array. The specific mechanism of encoding is not important,
     * as long as decode returns a clone of the original object.
     *
     * Currently, cycles in the object graph are preserved on the other end.
     * TODO: serialize Externalizable implementations, and take
     * into account the ClassLoader.
     */
    public static Object encode (Object obj)
    {
        return encode(obj, true);
    }

    protected static Object encode (Object obj, boolean encodeArrayElements)
    {
        if (obj == null) {
            return null;
        }
        if (encodeArrayElements) {
            if (obj instanceof Iterable) {
                ArrayList<byte[]> list = new ArrayList<byte[]>();
                for (Object o : (Iterable) obj) {
                    list.add((byte[]) encode(o, false));
                }
                byte[][] retval = new byte[list.size()][];
                list.toArray(retval);
                return retval;
            }

            if (obj.getClass().isArray()) {
                int length = Array.getLength(obj);
                byte[][] retval = new byte[length][];
                for (int ii=0; ii < length; ii++) {
                    retval[ii] = (byte[]) encode(Array.get(obj, ii), false);
                }
                return retval;
            }
        }

        // TODO: Our own encoding?
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(obj);
            oos.flush();
            return baos.toByteArray();
        } catch (IOException ioe) {
            // TODO: pass outward?
            return null; // just crush the object into nothingness
        }
    }

    // TODO: we may extend this to take the client ezgame's ClassLoader
    // and reconstitute custom classes that implement Externalizable
    public static Object decode (Object encoded)
    {
        if (encoded == null) {
            return null;
        }
        if (encoded instanceof byte[][]) {
            byte[][] src = (byte[][]) encoded;
            Object[] retval = new Object[src.length];
            for (int ii=0; ii < src.length; ii++) {
                retval[ii] = decode(src[ii]);
            }
            return retval;
        }

        try {
            ByteArrayInputStream bais =
                new ByteArrayInputStream((byte[]) encoded);
            ObjectInputStream ois = new ObjectInputStream(bais);
            return ois.readObject();

        } catch (ClassNotFoundException cnse) {
            return null;

        } catch (IOException ioe) {
            // TODO: pass outward?
            return null; // must not have been that important!
        }
    }
}
