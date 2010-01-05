//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2010 Three Rings Design, Inc., All Rights Reserved
// http://www.threerings.net/code/vilya/
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

package com.threerings.stage.tools.xml;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

import com.megginson.sax.DataWriter;

import com.threerings.miso.tools.xml.SparseMisoSceneWriter;

import com.threerings.whirled.data.SceneModel;
import com.threerings.whirled.spot.data.SpotSceneModel;
import com.threerings.whirled.spot.tools.xml.SpotSceneWriter;
import com.threerings.whirled.tools.xml.SceneWriter;

import com.threerings.stage.data.StageMisoSceneModel;
import com.threerings.stage.data.StageSceneModel;

/**
 * Generates an XML representation of a {@link StageSceneModel}.
 */
public class StageSceneWriter extends SceneWriter
{
    public StageSceneWriter ()
    {
        // register our auxiliary model writers
        registerAuxWriter(SpotSceneModel.class, new SpotSceneWriter());
        registerAuxWriter(StageMisoSceneModel.class,
                          new SparseMisoSceneWriter());
    }

    @Override
    protected void addSceneAttributes (SceneModel scene, AttributesImpl attrs)
    {
        super.addSceneAttributes(scene, attrs);
        StageSceneModel sscene = (StageSceneModel)scene;
        attrs.addAttribute("", "type", "", "", sscene.type);
    }

    @Override
    protected void writeSceneData (SceneModel scene, DataWriter writer)
        throws SAXException
    {
        // write out any default colorizations
        StageSceneModel sscene = (StageSceneModel)scene;
        if (sscene.defaultColors != null) {
            writer.startElement("zations");
            int[] keys = sscene.defaultColors.getKeys();
            for (int key : keys) {
                int value = sscene.defaultColors.get(key);
                AttributesImpl attrs = new AttributesImpl();
                attrs.addAttribute("", "classId", "", "",
                                   String.valueOf(key));
                attrs.addAttribute("", "colorId", "", "",
                                   String.valueOf(value));
                writer.emptyElement("", "zation", "", attrs);
            }
            writer.endElement("zations");
        }

        super.writeSceneData(scene, writer);
    }
}
