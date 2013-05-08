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

package com.threerings.stage.tools.xml;

import org.xml.sax.Attributes;

import org.apache.commons.digester.Rule;

import com.threerings.whirled.spot.data.Location;
import com.threerings.whirled.spot.tools.xml.SpotSceneRuleSet;
import com.threerings.whirled.tools.xml.SceneParser;
import com.threerings.whirled.tools.xml.SceneRuleSet;

import com.threerings.stage.data.StageLocation;
import com.threerings.stage.data.StageSceneModel;

/**
 * Parses {@link StageSceneModel} instances from an XML description file.
 */
public class StageSceneParser extends SceneParser
{
    /**
     * Constructs a parser that can be used to parse Stage scene models.
     */
    public StageSceneParser ()
    {
        super("");

        // add a rule to parse scene colorizations
        _digester.addRule("scene/zations/zation", new Rule() {
            @Override
            public void begin (String namespace, String name,
                               Attributes attrs) throws Exception {
                StageSceneModel yoscene = (StageSceneModel) digester.peek();
                int classId = Integer.parseInt(attrs.getValue("classId"));
                int colorId = Integer.parseInt(attrs.getValue("colorId"));
                yoscene.setDefaultColor(classId, colorId);
            }
        });

        // add rule sets for our aux scene models
        registerAuxRuleSet(new SpotSceneRuleSet() {
            @Override
            protected Location createLocation () {
                return new StageLocation();
            }
        });
        registerAuxRuleSet(new StageMisoSceneRuleSet());
    }

    @Override
    protected SceneRuleSet createSceneRuleSet ()
    {
        return new StageSceneRuleSet();
    }

    /**
     * A simple hook for parsing a single scene from the command line.
     */
    public static void main (String[] args)
    {
        if (args.length < 1) {
            System.err.println("Usage: StageSceneParser scene.xml");
            System.exit(-1);
        }

        try {
            System.out.println(
                "Parsed " + new StageSceneParser().parseScene(args[0]));
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
