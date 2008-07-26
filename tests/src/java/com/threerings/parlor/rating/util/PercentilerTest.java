//
// $Id: TestConfig.java 4193 2006-06-13 23:06:48Z ray $
//
// Narya library - tools for developing networked games
// Copyright (C) 2002-2004 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/narya/
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

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * Tests the {@link Percentiler} class.
 */
public class PercentilerTest extends TestCase
{
    public PercentilerTest ()
    {
        super(PercentilerTest.class.getName());
    }

    @Override
    public void runTest ()
    {
        // create a percentiler
        Percentiler tiler = new Percentiler();
        Random rando = new Random();

        // add some random values
        for (int ii = 0; ii < 500; ii++) {
            tiler.recordValue((float)rando.nextGaussian() + 5.0f);
        }
        tiler.recomputePercentiles();

        // now dump the tiler
        tiler.dump(System.out);

        // serialize and unserialize
        Percentiler t2 = new Percentiler(tiler.toBytes());
        // and dump again
        t2.dump(System.out);
    }

    public static Test suite ()
    {
        return new PercentilerTest();
    }

    public static void main (String[] args)
    {
        PercentilerTest test = new PercentilerTest();
        test.runTest();
    }
}
