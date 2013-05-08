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

import com.threerings.media.tile.TileSet;

import com.threerings.miso.tile.BaseTileSet;

import com.threerings.stage.tools.editor.EditorModel;

/**
 * Miscellaneous useful routines for working with lists of {@link TileSet}
 * and {@link BaseTileSet} objects.
 */
public class TileSetUtil
{
    /**
     * Returns the layer index of the layer for which this tileset
     * provides tiles.
     */
    public static int getLayerIndex (TileSet set)
    {
        if (set instanceof BaseTileSet) {
            return EditorModel.BASE_LAYER;
        } else {
            return EditorModel.OBJECT_LAYER;
        }
    }
}
