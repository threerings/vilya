//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2007 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.ezgame {

/**
 * Provides access to 'services' game services. Do not instantiate this class yourself,
 * access it via GameControl.services.
 */
public class EZServicesSubControl extends AbstractSubControl
{
    public function EZServicesSubControl (parent :AbstractGameControl)
    {
        super(parent);

        _bagsCtrl = createBagsControl();
    }

    /**
     * Access the 'bags' subcontrol.
     */
    public function get bags () :EZBagsSubControl
    {
        return _bagsCtrl;
    }

    /**
     * Requests a set of random letters from the dictionary service.  The letters will arrive in a
     * separate message with the specified key, as an array of strings.
     *
     * @param locale RFC 3066 string that represents language settings
     * @param dictionary the dictionary to use, or null for the default.
     *                   TODO: document possible parameters.
     * @param count the number of letters to be produced
     * @param callback the function that will process the results, of the form:
     * <pre>function (letters :Array) :void</pre>
     * where letters is an array of strings containing letters for the given language settings
     * (potentially empty).
     */
    public function getDictionaryLetterSet (
        locale :String, dictionary :String, count :int, callback :Function) :void
    {
        callHostCode("getDictionaryLetterSet_v2", locale, dictionary, count, callback);
    }

    /**
     * Checks to see if the dictionary for the given locale contains the given word.
     *
     * @param locale RFC 3066 string that represents language settings
     * @param dictionary the dictionary to use, or null for the default.
     *                   TODO: document possible parameters.
     * @param word the string contains the word to be checked
     * @param callback the function that will process the results, of the form:
     * <pre>function (word :String, result :Boolean) :void</pre>
     * where word is a copy of the word that was requested, and result specifies whether the word
     * is valid given language settings
     */
    public function checkDictionaryWord (
        locale :String, dictionary :String, word :String, callback :Function) :void
    {
        callHostCode("checkDictionaryWord_v2", locale, dictionary, word, callback);
    }

    /**
     * Start the ticker with the specified name. The ticker will deliver messages
     * (resulting in a MessageReceivedEvent being dispatched on the 'net' control)
     * to all connected clients, at the specified delay. The value of each message is
     * a single integer, starting with 0 and increasing by 1 with each messsage.
     */
    public function startTicker (tickerName :String, msOfDelay :int) :void
    {
        callHostCode("setTicker_v1", tickerName, msOfDelay);
    }

    /**
     * Stop the specified ticker.
     */
    public function stopTicker (tickerName :String) :void
    {
        startTicker(tickerName, 0);
    }

    /** 
     * Create the 'bags' subcontrol.
     * @private
     */
    protected function createBagsControl () :EZBagsSubControl
    {
        return new EZBagsSubControl(_parent);
    }

    /**
     * @private
     */
    override protected function populateProperties (o :Object) :void
    {
        super.populateProperties(o);

        _bagsCtrl.populatePropertiesFriend(o);
    }

    /**
     * @private
     */
    override protected function setHostProps (o :Object) :void
    {
        super.setHostProps(o);

        _bagsCtrl.setHostPropsFriend(o);
    }

    /** The bags sub-control. @private */
    protected var _bagsCtrl :EZBagsSubControl;
}
}
