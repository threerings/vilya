//
// $Id$

package com.threerings.ezgame {

/**
 * Displays a player list with integer scores, sorting players according to their score.
 */
// TODO: retain scores for players that are absent?
public class ScorePlayersFlexDisplay extends PlayersFlexDisplay
{
    public function ScorePlayersFlexDisplay ()
    {
        super();
    }

    /**
     * Update the score for a single player.
     */
    public function setScore (occupantId :int, score :int) :void
    {
        for (var ii :int = _players.length - 1; ii >= 0; ii--) {
            var rec :Object = _players.getItemAt(ii);
            if (rec.id == occupantId) {
                rec.score = score;
                updateList();
                return;
            }
        }

        // hmm, never found...
    }

    /**
     * Mass-update scores for players.
     *
     * @param mapping a hash mapping occupantId -> score.
     */
    public function setScores (mapping :Object) :void
    {
        for (var ii :int = _players.length - 1; ii >= 0; ii--) {
            var rec :Object = _players.getItemAt(ii);
            trace("Examining " + rec.id);
            if (String(rec.id) in mapping) {
                rec.score = int(mapping[String(rec.id)]);
                trace("Set score of " + rec.id + " to " + rec.score);
            }
        }

        updateList();
    }

    override protected function getRendererClass () :Class
    {
        return ScorePlayerRenderer;
    }

    override protected function createRecord (occupantId :int) :Object
    {
        var record :Object = super.createRecord(occupantId);
        record.score = 0;
        return record;
    }

    override protected function sortFunction (rec1 :Object, rec2 :Object, fields :Array = null) :int
    {
        // always sort on score
        if (rec1.score > rec2.score) {
            return -1;
        }
        if (rec1.score < rec2.score) {
            return 1;
        }

        // if two players have the same score then sort according to the superclass rules
        return super.sortFunction(rec1, rec2, fields);
    }
}
}
