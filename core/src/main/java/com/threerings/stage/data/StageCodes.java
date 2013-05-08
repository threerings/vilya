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

package com.threerings.stage.data;

import com.threerings.util.MessageBundle;

import com.threerings.presents.data.InvocationCodes;
import com.threerings.presents.data.Permission;

/**
 * Codes and constants relating to the Stage system.
 */
public interface StageCodes extends InvocationCodes
{
    /** The i18n bundle identifier for the Stage system. */
    public static final String STAGE_MESSAGE_BUNDLE = "stage.general";

    /** The resource set that contains our tileset bundles. */
    public static final String TILESET_RSRC_SET = "tilesets";

    /** The access control identifier for scene modification privileges. */
    public static final Permission MODIFY_SCENE_ACCESS = new Permission();

    /** The access control identifier for potentially damaging scene modification privileges. */
    public static final Permission MUTILATE_SCENE_ACCESS = new Permission();

    /** An error delivered when adding objects to scenes. */
    public static final String ERR_NO_OVERLAP = MessageBundle.qualify(
        STAGE_MESSAGE_BUNDLE, "m.addobj_no_overlap");

    /** An error delivered when adding objects to scenes. */
    public static final String ERR_CANNOT_CLUSTER = MessageBundle.qualify(
        STAGE_MESSAGE_BUNDLE, "m.cannot_cluster");
}
