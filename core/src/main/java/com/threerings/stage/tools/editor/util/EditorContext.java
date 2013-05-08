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

package com.threerings.stage.tools.editor.util;

import java.util.List;

import com.threerings.media.image.ColorPository;
import com.threerings.media.tile.TileSetRepository;

import com.threerings.stage.util.StageContext;

public interface EditorContext extends StageContext
{
    /**
     * Return a reference to the tile set repository in use by the tile
     * manager.  This reference is valid for the lifetime of the
     * application.
     */
    public TileSetRepository getTileSetRepository ();

    /**
     * Returns a colorization repository for use by the editor.
     */
    public ColorPository getColorPository ();

    /**
     * Inserts all known scene types into the supplied list.
     */
    public void enumerateSceneTypes (List<String> types);
}
