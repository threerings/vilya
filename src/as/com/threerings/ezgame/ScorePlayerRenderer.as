//
// $Id$

package com.threerings.ezgame {

import mx.controls.Label;

public class ScorePlayerRenderer extends SimplePlayerRenderer
{
    override protected function createUI () :void
    {
        super.createUI();

        addChild(_scoreLabel = new Label());
        _scoreLabel.maxWidth = 100;
    }

    override protected function configureUI () :void
    {
        super.configureUI();

        _scoreLabel.text = String(data.score);
    }

    /* The label used to display the player's score. */
    protected var _scoreLabel :Label;
}
}
