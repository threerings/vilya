//
// $Id: Percentiler.java 25062 2006-06-13 22:52:01Z ray $

package com.threerings.parlor.rating.util;

import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import com.samskivert.util.StringUtil;

import com.threerings.parlor.Log;

/**
 * Used to keep track of the percentile distribution of positive values
 * (generally puzzle scores).
 */
public class Percentiler
{
    /**
     * Creates an empty percentiler.
     */
    public Percentiler ()
    {
        _total = 0;
        _max = 1;
    }

    /**
     * Creates a percentiler from its serialized representation.
     */
    public Percentiler (byte[] data)
    {
        // decode the data
        ByteBuffer in = ByteBuffer.wrap(data);
        IntBuffer iin = in.asIntBuffer();
        _max = iin.get();
        iin.get(_counts);
        in.position((BUCKET_COUNT+1) * INT_SIZE);
        LongBuffer lin = in.asLongBuffer();
        _total = lin.get();

        // compute our percentiles
        recomputePercentiles();
    }

    /**
     * Records a value, updating the histogram but not the percentiles (a
     * call to {@link #recomputePercentiles} is required for that and is
     * sufficiently expensive that it shouldn't be done every time a value
     * is added).
     */
    public void recordValue (float value)
    {
        // if this value is larger than our maximum value, we need to
        // redistribute our buckets
        if (value > _max) {
            // determine what our new maximum should be: twenty percent
            // again larger than this newly seen maximum and rounded to an
            // integer value
            int newmax = (int)Math.ceil(value*1.2);
            float newdelta = (float)newmax / BUCKET_COUNT;

            Log.info("Resizing [newmax=" + newmax + ", oldmax=" + _max + "].");
            if (newmax > 2 * _max) {
                Log.info("Holy christ! Big newmax [newmax=" + newmax +
                         ", oldmax=" + _max + "].");
                Thread.dumpStack();
            }

            // create a new counts array and map the old array to the new
            float delta = (float)_max / BUCKET_COUNT;
            int[] counts = new int[BUCKET_COUNT];
            float oval = delta, nval = newdelta;
            for (int ii = 0, ni = 0; ii < BUCKET_COUNT; ii++, oval += delta) {
                // if this old bucket is entirely contained within a new
                // bucket, add all of its counts to the new bucket
                if (oval <= nval) {
                    counts[ni] += _counts[ii];

                } else {
                    // otherwise, we need to add the appropriate fraction
                    // of this bucket's counts to the two new buckets into
                    // which it falls
                    float fraction = (nval - (oval - delta)) / delta;
                    int lesser = (int)Math.round(_counts[ii] * fraction);
                    counts[ni] += lesser;
                    counts[++ni] += (_counts[ii] - lesser);
                    nval += newdelta;
                }
            }

            // put the remapped histogram into place
            _max = newmax;
            _counts = counts;

            // force a recalculation
            _nextRecomp = 0;
        }

        // increment the bucket associated with this value
        _counts[toBucketIndex(value)]++;
        _total++;

//         Log.info("Recorded [value=" + value + ", total=" + _total + "].");

        // see if it's time to recompute
        if (_nextRecomp-- <= 0) {
            recomputePercentiles();
            // recompute again when we've grown by 5%
            _nextRecomp = (int)(_total/20);
        }
    }

    /**
     * Returns the percent of all numbers seen that are lower than the
     * specified value. This value can range from zero to 100 (100 in the
     * case where this is the highest value ever seen by this
     * percentiler). This value reflects the percentiles computed as of
     * the most recent call to {@link #recomputePercentiles}.
     */
    public int getPercentile (float value)
    {
        return _percentile[toBucketIndex(value)];
    }

    /**
     * Returns the score necessary to attain the specified percentile.
     * This value reflects the percentiles computed as of the most recent
     * call to {@link #recomputePercentiles}.
     *
     * @param percentile the desired percentile (from 0 to 99 inclusive).
     */
    public float getRequiredScore (int percentile)
    {
        percentile = Math.max(0, Math.min(99, percentile)); // bound this!
        return _reverse[percentile] * ((float)_max / BUCKET_COUNT);
    }

    /**
     * Returns the largest score seen by this percentiler.
     */
    public int getMaxScore ()
    {
        return _max;
    }

    /**
     * Recomputes the percentile cutoffs based on the values recorded
     * since the last percentile computation.
     */
    public void recomputePercentiles ()
    {
        // compute the forward mapping (score to percentile)
        long accum = 0;
        for (int ii = 0; ii < BUCKET_COUNT-1; ii++) {
            accum += _counts[ii];
            _percentile[ii+1] = (_total == 0) ? 50 : (byte)(accum*100/_total);
        }

        // compute the reverse mapping (percentile to minimum score)
        for (int ii = 0, pp = 0; ii < BUCKET_COUNT; ii++) {
            // scan forward to the percentile bucket that maps to this
            // percentile
            while (_percentile[pp] < ii && pp < (BUCKET_COUNT-1)) {
                pp++;
            }
            _reverse[ii] = (byte)pp;
        }
    }

    /**
     * Converts this percentiler to a byte array so that it may be stored
     * into a database.
     */
    public byte[] toBytes ()
    {
        byte[] data = new byte[(BUCKET_COUNT+3) * INT_SIZE];
        ByteBuffer out = ByteBuffer.wrap(data);
        IntBuffer iout = out.asIntBuffer();
        iout.put(_max);
        iout.put(_counts);
        out.position((BUCKET_COUNT+1) * INT_SIZE);
        LongBuffer lout = out.asLongBuffer();
        lout.put(_total);
        return data;
    }

    /**
     * Generates a string representation of this instance.
     */
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[total=").append(_total);
        buf.append(", max=").append(_max);
        buf.append(", pcts=(");
        for (int ii = 0; ii < 10; ii++) {
            if (ii > 0) {
                buf.append("-");
            }
            buf.append(StringUtil.format(getRequiredScore(10*ii)));
        }
        return buf.append(")]").toString();
    }

    /**
     * Dumps out our data in a format that can be used to generate a
     * gnuplot.
     */
    public void dumpGnuPlot (PrintStream out)
    {
        for (int ii = 0; ii < 100; ii++) {
            float score = (float)_max*ii/100;
            out.println(score + " " + _percentile[ii] + " " + _counts[ii]);
        }
    }

    /**
     * Dumps a text representation of this percentiler to the supplied
     * print stream.
     */
    public void dump (PrintStream out)
    {
        // obtain our maximum count
        int max = 0;
        for (int ii = 0; ii < BUCKET_COUNT; ii++) {
            if (_counts[ii] > max) {
                max = _counts[ii];
            }
        }

        // figure out how many digits are needed to display the biggest
        // bucket's size
        int digits = (int)Math.ceil(Math.log(max) / Math.log(10));
        digits = Math.max(digits, 1);

        // output each bucket in a column of its own
        for (int rr = 9; rr >= 0; rr--) {
            // print the "value" of this row
            out.print(StringUtil.pad("" + (rr+1)*max/10, digits) + " ");
            for (int ii = 0; ii < BUCKET_COUNT; ii++) {
                out.print((_counts[ii] * 10 / max > rr) ? "*" : " ");
            }
            out.println("");
        }

        out.print(spaces(digits));
        for (int ii = 0; ii < BUCKET_COUNT; ii++) {
            out.print("-");
        }
        out.println("");

        out.print(spaces(digits));
        for (int ii = 0; ii < BUCKET_COUNT; ii++) {
            out.print(_percentile[ii]%10);
        }
        out.println("");

        out.print(spaces(digits));
        for (int ii = 0; ii < BUCKET_COUNT; ii++) {
            out.print((_percentile[ii]/10)%10);
        }
        out.println("");

        // print out a scale along the very bottom
        out.println("");
        out.println("total: " + _total + " max: " + _max +
                    " delta: " + ((float)_max / BUCKET_COUNT));
    }

    protected final String spaces (int count)
    {
        StringBuilder buf = new StringBuilder();
        for (int ii = 0; ii < count; ii++) {
            buf.append(" ");
        }
        return buf.toString();
    }

    /**
     * Returns the histogram bucket to which this value is assigned.
     */
    protected final int toBucketIndex (float value)
    {
        int idx = Math.min((int)Math.round(value * BUCKET_COUNT / _max), 99);
        if (idx < 0 || idx >= BUCKET_COUNT) {
            Log.warning("'" + value + "' caused bogus bucket index (" +
                        idx + ") to be computed.");
            Thread.dumpStack();
            return 0;
        }
        return idx;
    }

    /** The total number of data points seen by this percentiler. */
    protected long _total;

    /** The maximum value seen by this percentiler. */
    protected int _max;

    /** Counts down to our next recalculation. */
    protected int _nextRecomp;

    /** A histogram of all values recorded to this percentiler. */
    protected int[] _counts = new int[BUCKET_COUNT];

    /** The percentile associated with each bucket. */
    protected byte[] _percentile = new byte[BUCKET_COUNT];

    /** The bucket associated with each percentile. */
    protected byte[] _reverse = new byte[BUCKET_COUNT];

    /** The number of divisions between zero and our maximum value, which
     * defines the granularity of our histogram. */
    protected static final int BUCKET_COUNT = 100;

    /** Number of bytes in an int; makes code clearer. */
    protected static final int INT_SIZE = 4;
}
