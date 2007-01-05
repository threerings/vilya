package com.threerings.ezgame {

import flash.display.DisplayObject;
import flash.display.Sprite;

import flash.text.StyleSheet;
import flash.text.TextField;
import flash.text.TextFieldAutoSize;

/**
 * A sample component that displays the players of a game.
 * If the game has a turn holder, the current turn holder will be highlighted.
 *
 * This class demonstrates that the 'Game' interface may be implemented
 * by any DisplayObject that want access to the GameObject, not just the
 * actual DisplayObject that is displaying the game. Here, all we are
 * interested in is the names of the players and the current turn holder.
 *
 * You may use this, with any modifications you desire, in your game. Feel
 * free to copy/modify or extend this class.
 */
public class PlayersDisplay extends Sprite
    implements StateChangedListener
{
    /**
     * Set the game control that will be used with this display.
     */
    public function setGameControl (gameCtrl :EZGameControl) :void
    {
        _gameCtrl = gameCtrl;
        _gameCtrl.registerListener(this);

        configureInterface();
    }

    // from StateChangedListener
    public function stateChanged (event :StateChangedEvent) :void
    {
        displayCurrentTurn();
    }

    /**
     * Set up the player labels and configure the look of the entire UI.
     */
    protected function configureInterface () :void
    {
        var border :int = getBorderSpacing();
        var pad :int = getInternalSpacing();
        var y :Number = border;
        var maxWidth :Number = 0;
        var label :TextField;
        var icon :DisplayObject;

        // create a label at the top, above the player names
        label = createHeader();
        if (label != null) {
            label.x = border;
            label.y = y;
            addChild(label);
            y += label.textHeight + pad;
            maxWidth = label.textWidth;
        }

        // create a label for each player
        var playerIndex :int = 0;
        for each (var name :String in _gameCtrl.getPlayerNames()) {
            label = createPlayerLabel(playerIndex, name);
            icon = createPlayerIcon(playerIndex, name);
            var iconW :int = 0;
            var iconH :int = 0;
            if (icon != null) {
                iconW = icon.width + pad;
                iconH = icon.height;

                icon.x = border;
                icon.y = y;
                addChild(icon);
            }
            label.x = border + iconW;
            label.y = y;
            addChild(label);
            y += Math.max(label.textHeight, iconH) + pad;
            maxWidth = Math.max(maxWidth, iconW + label.textWidth);

            _playerLabels.push(label);
            playerIndex++;
        }

        // make all the player labels the same width
        // (looks nice when highlighted)
        for each (label in _playerLabels) {
            label.autoSize = TextFieldAutoSize.NONE;
            label.width = maxWidth - (label.x - border);
        }

        // y has a pad at the end, we want border instead
        y += border - pad;

        // draw a blue rectangle around everything
        graphics.clear();
        graphics.lineStyle(1, 0x0000FF);
        graphics.drawRect(0, 0, maxWidth + (border * 2), y);

        displayCurrentTurn();
    }

    protected function createHeader () :TextField
    {
        var label :TextField = createHeader();
// damn stylesheet doesn't seem to actually -work-
//        var style :StyleSheet = new StyleSheet();
//        style.fontWeight = "bold";
//        style.color = "#0000FF";
//        style.fontFamily = "serif";
//        style.fontSize = 18;
//        label.styleSheet = style;
        label.autoSize = TextFieldAutoSize.LEFT;
        label.selectable = false;
        label.text = "Players";
        return label;
    }

    /**
     * Create a TextArea that will be used to display player names.
     */
    protected function createPlayerLabel (idx :int, name :String) :TextField
    {
        var label :TextField = new TextField();
        label.autoSize = TextFieldAutoSize.LEFT;
        label.background = true;
        label.selectable = false;
        label.text = name;
        return label;
    }

    protected function createPlayerIcon (idx :int, name :String) :DisplayObject
    {
        return null;
    }

    protected function getBackground (isTurn :Boolean) :uint
    {
        return isTurn ? 0xFF9999 : 0xFFFFFF;
    }

    protected function getBorderSpacing () :int
    {
        return 6;
    }

    protected function getInternalSpacing () :int
    {
        return 2;
    }

    /**
     * Re-set the background color for every player label, highlighting
     * only the player who has the turn.
     */
    protected function displayCurrentTurn () :void
    {
        var idx :int = _gameCtrl.isInPlay() ? _gameCtrl.getTurnHolderIndex() : -1;
        for (var ii :int = 0; ii < _playerLabels.length; ii++) {
            var label :TextField = (_playerLabels[ii] as TextField);
            label.backgroundColor = getBackground(ii == idx);
        }
    }

    /** Our game Control. */
    protected var _gameCtrl :EZGameControl;

    /** An array of labels, one for each player name. */
    protected var _playerLabels :Array = [];
}
}
