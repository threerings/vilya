package com.threerings.ezgame.util {

import flash.net.ObjectEncoding;

import flash.system.ApplicationDomain;

import flash.utils.ByteArray;
import flash.utils.Endian;

import com.threerings.util.StringUtil;

import com.threerings.io.TypedArray;

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
     * TODO: serialize IExternalizable implementations, and take
     * into account the ApplicationDomain.
     */
    public static function encode (
        obj :Object, encodeArrayElements :Boolean = true) :Object
    {
        if (obj == null) {
            return null;
        }
        if (encodeArrayElements && obj is Array) {
            var src :Array = (obj as Array);
            var dest :TypedArray = TypedArray.create(ByteArray);
            for each (var o :Object in src) {
                dest.push(encode(o, false));
            }
            return dest;
        }

        // TODO: Our own encoding, that takes into account
        // the ApplicationDomain
        var bytes :ByteArray = new ByteArray();
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        bytes.writeObject(obj);
        Log.getLog(EZObjectMarshaller).info(
            "The encoded bytes are: " + StringUtil.hexlate(bytes));
        return bytes;
    }

    public static function decode (encoded :Object) :Object
    {
        if (encoded == null) {
            return null;
        }
        if (encoded is TypedArray) {
            var src :TypedArray = (encoded as TypedArray);
            var dest :Array = [];
            for each (var b :ByteArray in src) {
                dest.push(decode(b));
            }
            return dest;
        }
        var bytes :ByteArray = (encoded as ByteArray);
        // re-set the position in case we're decoding the actual same byte
        // array used to encode (and not a network reconstruction)
        bytes.position = 0;

        // TODO: Our own decoding, that takes into account
        // the ApplicationDomain
        bytes.endian = Endian.BIG_ENDIAN;
        bytes.objectEncoding = ObjectEncoding.AMF3;
        return bytes.readObject();
    }
}
}
