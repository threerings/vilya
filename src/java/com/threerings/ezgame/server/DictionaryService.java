//
// $Id$
//
// Vilya library - tools for developing networked games
// Copyright (C) 2002-2006 Three Rings Design, Inc., All Rights Reserved
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

package com.threerings.ezgame.server;

import com.samskivert.util.Invoker;
import com.samskivert.util.ResultListener;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.presents.client.InvocationService;

import java.lang.reflect.Array;

import java.util.ArrayList;
import java.util.Random;
import static com.threerings.ezgame.server.Log.log;

/**
 * Manages loading and querying word dictionaries in multiple languages.
 *
 * NOTE: the service supports lazy loading of language files, but does not
 * _unload_ them from memory, leading to increasing memory usage. 
 */
public class DictionaryService
{
    /**
     * Creates the singleton instance of the dictionary service.
     */
    public static void init ()
    {
        _singleton = new DictionaryService ();
    }

    /**
     * Get an instance of the dictionary service.
     */
    public static DictionaryService getInstance ()
    {
        return _singleton;
    }

    /**
     * Protected constructor.
     */
    protected DictionaryService () 
    {
        // TODO: set up file paths here
    }

    /**
     * Returns true if the language is known to be supported by the
     * dictionary service (would it be better to return a whole list
     * of supported languages instead?)
     */
    public void isLanguageSupported (
        final String locale, final InvocationService.ResultListener listener)
    {
        // TODO: once we have file paths set up, change this to match
        // against dictionary files
        listener.requestProcessed (locale.toLowerCase().startsWith("en"));
    }

    /**
     * Retrieves a set of letters from a language definition file,
     * and returns a random sampling of /count/ elements.
     */
    public void getLetterSet (
        final String locale, final int count, final InvocationService.ResultListener listener)
    {
        // Create a new unit of work
        Invoker.Unit work = new Invoker.Unit ("DictionaryService.getLetterSet") {
            public boolean invoke ()
            {
                if (loadLanguageFile (locale))
                {
                    // TODO: actual logic will go here
                    
                    Random r = new Random ();
                    char[] chars = new char[count];
                    for (int i = 0; i < count; i++)
                    {
                        int c = r.nextInt (24) + 65;
                        chars[i] = (char) (c);
                    }
                    StringBuilder sb = new StringBuilder ();
                    for (char c : chars)
                    {
                        sb.append (c);
                        sb.append (',');
                    }
                    sb.deleteCharAt (sb.length() - 1);
                        
                    listener.requestProcessed (sb.toString());
                }
                return true;
            }
        };

        // Do the work!
        CrowdServer.invoker.postUnit (work);    
    }
    
    /**
     * Checks if the specified word exists in the given language
     */
    public void checkWord (
        final String locale, final String word, final InvocationService.ResultListener listener)
    {
        // Create a new unit of work
        Invoker.Unit work = new Invoker.Unit ("DictionaryService.checkWord") {
            public boolean invoke ()
            {
                if (loadLanguageFile (locale))
                {
                    // TODO: actual logic
                    listener.requestProcessed (true);
                }
                return true;
            }
        };

        // ...and off we go.
        CrowdServer.invoker.postUnit (work);    
    }

    /**
     * Loads the language file, if necessary
     */
    public boolean loadLanguageFile (String locale)
    {
        // this is boring :)
        try {
            Thread.sleep (500);
        } catch (InterruptedException e) { }
        return true;
    }



    // PRIVATE VARIABLES

    /** Singleton instance pointer */
    private static DictionaryService _singleton;

}
