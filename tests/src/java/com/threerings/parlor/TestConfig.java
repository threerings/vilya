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

package com.threerings.parlor;

import com.threerings.crowd.client.PlaceController;
import com.threerings.parlor.game.client.GameConfigurator;
import com.threerings.parlor.game.data.GameConfig;

public class TestConfig extends GameConfig
{
    /** The foozle parameter. */
    public int foozle;

    @Override
    public int getGameId ()
    {
        return 0;
    }

    @Override
    public String getGameIdent ()
    {
        return "test";
    }

    @Override
    public GameConfigurator createConfigurator ()
    {
        return null;
    }

    @Override
    public PlaceController createController ()
    {
        return new TestController();
    }

    @Override
    public String getManagerClassName ()
    {
        return "com.threerings.parlor.test.TestManager";
    }

    @Override
    protected void toString (StringBuilder buf)
    {
        super.toString(buf);
        buf.append(", foozle=").append(foozle);
    }
}
