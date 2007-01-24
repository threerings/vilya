//
// $Id$

package com.threerings.stage.tools.xml;

import com.threerings.whirled.tools.xml.SceneRuleSet;

import com.threerings.stage.data.StageScene;
import com.threerings.stage.data.StageSceneModel;

/**
 * Used to parse an {@link StageScene} from XML.
 */
public class StageSceneRuleSet extends SceneRuleSet
{
    // documentation inherited
    protected Class getSceneClass ()
    {
        return StageSceneModel.class;
    }
}
