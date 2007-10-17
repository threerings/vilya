//
// $Id$

package com.threerings.ezgame {

import mx.containers.HBox;

import mx.controls.Label;

import mx.core.ScrollPolicy;


public class SimplePlayerRenderer extends HBox
{
    public function SimplePlayerRenderer ()
    {
        super();

        configureProperties();
    }

    override public function set data (value :Object) :void
    {
        super.data = value;

        if (processedDescriptors) {
            configureUI();
        }
    }

    override protected function createChildren () :void
    {
        super.createChildren();

        createUI();

        if (data != null) {
            data = data; // re-set
        }
    }

    /**
     * Configure any UI properties of this renderer.
     */
    protected function configureProperties () :void
    {
        verticalScrollPolicy = ScrollPolicy.OFF;
        horizontalScrollPolicy = ScrollPolicy.OFF;
    }

    /**
     * Create any UI elements needed.
     */
    protected function createUI () :void
    {
        addChild(_nameLabel = new Label());
        _nameLabel.maxWidth = 100;
    }

    /**
     * Configure the actual UI elements with the data.
     */
    protected function configureUI () :void
    {
        _nameLabel.text = String(data.name);
    }

    /** The label used to display the player's name. */
    protected var _nameLabel :Label;
}

}
