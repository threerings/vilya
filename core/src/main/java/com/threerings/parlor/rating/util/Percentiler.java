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

package com.threerings.parlor.rating.util;

import java.io.PrintWriter;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import com.samskivert.util.StringUtil;

import static com.threerings.parlor.Log.log;

/**
 * Used to keep track of the percentile distribution of positive values (generally puzzle scores).
 */
public class Percentiler
{
    /**
     * Creates an empty percentiler.
     */
    public Percentiler ()
    {
    }

    /**
     * Creates a percentiler that expects values to fall within the given range.
     */
    public Percentiler (int min, int max)
    {
        _min = min;
        _max = max;
        _fixedRange = true;
    }

    /**
     * Creates a percentiler from its serialized representation.
     */
    public Percentiler (byte[] data)
    {
        ByteBuffer in = ByteBuffer.wrap(data);

        // read our int data
        IntBuffer iin = in.asIntBuffer();
        _max = iin.get();
        iin.get(_counts);
        in.position(iin.position() * INT_SIZE);

        // read our long data
        LongBuffer lin = in.asLongBuffer();
        _snapTotal = (_total = lin.get());
        in.position(iin.position() * INT_SIZE + lin.position() * 2 * INT_SIZE);

        // read our min value (which was added afterwards and must do some jockeying to maintain
        // backwards compatibility)
        if (in.position() == in.limit()) {
            _min = 0; // legacy
        } else {
            _min = in.asIntBuffer().get();
        }

        // Un-break percentilers that have been stored with bogus data
        if (_max < _min) {
            log.warning("Percentiler initialized with bogus range. Coping.",
                "min", _min, "max", _max);
            _max = _min + 1;
        }

        // compute our percentiles
        recomputePercentiles();
    }

    /**
     * Records a value, updating the histogram but not the percentiles (a call to {@link
     * #recomputePercentiles} is required for that and is sufficiently expensive that it shouldn't
     * be done every time a value is added).
     */
    public void recordValue (float value)
    {
        recordValue(value, true);
    }

    /**
     * See {@link #recordValue(float)}.
     */
    public void recordValue (float value, boolean logNewMax)
    {
        // if this is the first value ever recorded; note our min and max
        if (_total == 0 && !_fixedRange) {
            _min = (int)Math.floor(value);
            _max = Math.max((int)Math.ceil(value), _min + 1);
        }

        // if this value is outside our bounds, we need to redistribute our buckets
        if (value < _min || value > _max) {
            if (_fixedRange) {
                log.warning("Recording value outside of initially fixed range",
                    "min", _min, "max", _max, "value", value);
                _fixedRange = false;
            }

            // expand by 20% in the direction of either our new minimum or new maximum
            int newmin = (value < _min) ? (_max - (int)Math.ceil((_max - value) * 1.2f)) : _min;
            int newmax = (value > _max) ? (_min + (int)Math.ceil((value - _min) * 1.2f)) : _max;

            if (newmin > _min || newmax < _max) {
                log.warning("Grew our range in crazy ways?!", "value", value, "total", _total,
                            "new", ("" + newmin + ":" + newmax), "old", ("" + _min + ":" + _max));
            }

            if (logNewMax) {
                log.info("Resizing", "value", value, "total", _total,
                         "new", ("" + newmin + ":" + newmax), "old", ("" + _min + ":" + _max));
            }

            // create a new counts array and map the old array to the new
            float ndelta = (newmax - newmin) / (float)BUCKET_COUNT;
            float odelta = (_max - _min) / (float)BUCKET_COUNT;
            int[] counts = new int[BUCKET_COUNT];

            for (int ii = 0; ii < BUCKET_COUNT; ii++) {
                // determine the first new bucket that contains some or all of the old bucket
                float obot = _min + odelta * ii;
                int newidx = Math.min(BUCKET_COUNT-1, (int)Math.floor((obot - newmin) / ndelta));

                // compute how much of this bucket (if any) spills over into the next bucket
                float newoff = (float)Math.IEEEremainder(obot - newmin, ndelta);
                float nextfrac = (newoff + odelta) - ndelta;

                // now put this bucket's contents into either one or two new buckets
                if (nextfrac <= 0 || newidx == BUCKET_COUNT-1) {
                    counts[newidx] += _counts[ii];
                } else {
                    int next = Math.round(_counts[ii] * nextfrac / odelta);
                    counts[newidx] += (_counts[ii] - next);
                    counts[newidx+1] += next;
                }
            }

            // put the remapped histogram into place
            _min = newmin;
            _max = newmax;
            _counts = counts;

            // force a recalculation
            _nextRecomp = 0;
        }

        // increment the bucket associated with this value
        _counts[toBucketIndex(value)]++;
        _total++;

        // see if it's time to recompute
        if (_nextRecomp-- <= 0) {
            recomputePercentiles();
            // recompute again when we've grown by 5%
            _nextRecomp = (int)(_total/20);
        }
    }

    /**
     * Returns the total number of values ever recorded to this percentiler.
     */
    public long getRecordedCount ()
    {
        return _total;
    }

    /**
     * Returns true if thsi percentiler has been modified since it was created or since the last
     * call to {@link #clearModified}.
     */
    public boolean isModified ()
    {
        return (_total != _snapTotal);
    }

    /**
     * Clears this percentiler's "is modified" state.
     */
    public void clearModified ()
    {
        _snapTotal = _total;
    }

    /**
     * Returns the percent of all numbers seen that are lower than the specified value. This value
     * can range from zero to 100 (100 in the case where this is the highest value ever seen by
     * this percentiler). This value reflects the percentiles computed as of the most recent call
     * to {@link #recomputePercentiles}.
     */
    public int getPercentile (float value)
    {
        if (value < _min) {
            return 0;
        } else if (value > _max) {
            return 100;
        } else {
            return _percentile[toBucketIndex(value)];
        }
    }

    /**
     * Returns the score necessary to attain the specified percentile.  This value reflects the
     * percentiles computed as of the most recent call to {@link #recomputePercentiles}.
     *
     * @param percentile the desired percentile (from 0 to 99 inclusive).
     */
    public float getRequiredScore (int percentile)
    {
        percentile = Math.max(0, Math.min(99, percentile)); // bound this!
        return _reverse[percentile] * ((float)(_max - _min) / BUCKET_COUNT) + _min;
    }

    /**
     * Returns the largest score seen by this percentiler.
     */
    public int getMaxScore ()
    {
        return _max;
    }

    /**
     * Returns the smallest score seen by this percentiler.
     */
    public int getMinScore ()
    {
        return _min;
    }

    /**
     * Returns the scores required to obtain a percentile rating from 0 to 99.
     */
    public float[] getRequiredScores ()
    {
        float[] scores = new float[100];
        for (int ii = 0; ii < 100; ii++) {
            scores[ii] = getRequiredScore(ii);
        }
        return scores;
    }

    /**
     * Returns the counts for each bucket.
     */
    public int[] getCounts ()
    {
        return _counts.clone();
    }

    /**
     * Recomputes the percentile cutoffs based on the values recorded since the last percentile
     * computation.
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
            // scan forward to the percentile bucket that maps to this percentile
            while (_percentile[pp] < ii && pp < (BUCKET_COUNT-1)) {
                pp++;
            }
            _reverse[ii] = (byte)pp;
        }
    }

    /**
     * Converts this percentiler to a byte array so that it may be stored into a database.
     */
    public byte[] toBytes ()
    {
        byte[] data = new byte[(BUCKET_COUNT+4) * INT_SIZE];
        ByteBuffer out = ByteBuffer.wrap(data);

        // write our int data
        IntBuffer iout = out.asIntBuffer();
        iout.put(_max);
        iout.put(_counts);
        out.position(iout.position() * INT_SIZE);

        // write our long data
        LongBuffer lout = out.asLongBuffer();
        lout.put(_total);
        out.position(iout.position() * INT_SIZE + lout.position() * 2 * INT_SIZE);

        // write our min value (added later so we can't write it above like we wish we could)
        out.asIntBuffer().put(_min);

        return data;
    }

    @Override
    public String toString ()
    {
        StringBuilder buf = new StringBuilder();
        buf.append("[total=").append(_total);
        buf.append(", min=").append(_min);
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
     * Dumps out our data in a format that can be used to generate a gnuplot.
     */
    public void dumpGnuPlot (PrintWriter out)
    {
        float delta = (_max - _min) / (float)BUCKET_COUNT;
        for (int ii = 0; ii < BUCKET_COUNT; ii++) {
            out.println((_min + ii * delta) + " " + _percentile[ii] + " " + _counts[ii]);
        }
    }

    /**
     * Dumps a text representation of this percentiler to the supplied print stream.
     */
    public void dump (PrintWriter out)
    {
        // obtain our maximum count
        int max = 0;
        for (int ii = 0; ii < BUCKET_COUNT; ii++) {
            if (_counts[ii] > max) {
                max = _counts[ii];
            }
        }

        // figure out how many digits are needed to display the biggest bucket's size
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
        out.println("total: " + _total + " min: " + _min + " max: " + _max +
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
        int idx = Math.round((value - _min) * BUCKET_COUNT / (_max - _min));
        idx = Math.min(idx, BUCKET_COUNT-1);
        if (idx < 0 || idx >= BUCKET_COUNT) {
            log.warning("Bogus bucket index, using 0", "value", value, "max", _max, "min", _min,
                        "idx", idx, new Throwable());
            return 0;
        }
        return idx;
    }

    /** If this Percentiler was created with a fixed range. */
    protected boolean _fixedRange;

    /** The total number of data points seen by this percentiler. */
    protected long _total;

    /** The value of {@link #_total} at creation time or as of a call to {@link #clearModified}. */
    protected long _snapTotal;

    /** The minimum value seen by this percentiler. */
    protected int _min;

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

    /** The number of divisions between zero and our maximum value, which defines the granularity
     * of our histogram. */
    protected static final int BUCKET_COUNT = 100;

    /** Number of bytes in an int; makes code clearer. */
    protected static final int INT_SIZE = 4;
}
