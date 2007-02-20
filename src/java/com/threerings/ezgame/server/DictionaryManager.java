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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.lang.reflect.Array;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import com.samskivert.util.CountHashMap;
import com.samskivert.util.RandomUtil;

import static com.threerings.ezgame.server.Log.log;

/**
 * Manages loading and querying word dictionaries in multiple languages.
 *
 * NOTE: the service supports lazy loading of language files, but does not
 * _unload_ them from memory, leading to increasing memory usage.
 *
 * NOTE: the dictionary service has not yet been tested with language
 * files written in non-default character encodings.
 */
public class DictionaryManager
{
    /**
     * Helper class, encapsulates a sorted array of word hashes,
     * which can be used to look up the existence of a word.
     */
    private class Dictionary
    {
        /**
         * Constructor, loads up the word list and initializes storage.
         * This naive version assumes language files are simple list of words,
         * with one word per line.
         */
        public Dictionary (File wordfile)
        {
            try
            {
                CountHashMap <Character> letters = new CountHashMap <Character>();
                
                if (wordfile.exists() && wordfile.isFile() && wordfile.canRead())
                {
                    // Read it line by line
                    BufferedReader reader = new BufferedReader (new FileReader (wordfile));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        String word = line.toLowerCase();
                        // Add the word to the dictionary
                        _words.add(word);

                        // count characters
                        for (int ii = word.length() - 1; ii >= 0; ii--) {
                            char ch = word.charAt(ii);
                            letters.incrementCount(ch, 1);
                        }
                    }

                    initializeLetterCounts (letters);

                    log.log (Level.INFO,
                             "Loaded dictionary file " + wordfile.getName () +
                             " with " + _words.size () + " entries, " +
                             _letters.length + " letters.");
                                                          
                }
                else
                {
                    log.log (Level.WARNING,
                             "Could not access dictionary file " + wordfile.getAbsolutePath ());
                }
            }
            catch (Exception ex)
            {
                log.log (Level.WARNING, "Failed to load dictionary file", ex);
                _words.clear();  // dump everything
                _letters = new char[] { };
                _counts = new float[] { };
            }
        }

        /** Checks if the specified word exists in the word list */
        public boolean contains (String word)
        {
            return (word != null) && _words.contains(word.toLowerCase());
        }

        /** Gets an array of random letters for the language, with uniform distribution. */
        public char[] randomLetters (int count)
        {
            char[] results = new char[count];
            for (int i = 0; i < count; i++)
            {
                // find random index and get its letter
                int index = RandomUtil.getWeightedIndex (_counts);
                results[i] = _letters[index];
            }

            return results;
        }


        // PRIVATE HELPERS

        /** Given a CountHashMap of letters, initializes the internal
            letter and count arrays, used by RandomUtil. */
        protected void initializeLetterCounts (CountHashMap <Character> letters)
        {
            Set<Character> keys = letters.keySet ();
            int keycount = keys.size();
            int total = letters.getTotalCount ();
            if (total == 0) { return; } // Something went wrong, abort.

            // Initialize storage
            _letters = new char[keycount];
            _counts = new float[keycount];

            // Copy letters and normalize counts
            for (Character key : keys)
            {
                keycount--;
                _letters[keycount] = (char) key;
                _counts[keycount] = ((float) letters.getCount (key)) / total; // normalize
            }
        }
                
                

        // PRIVATE STORAGE

        /** The words. */
        protected HashSet<String> _words = new HashSet<String>();

        /** Letter array. */
        protected char[] _letters = new char[] { };

        /** Letter count array. */
        protected float[] _counts = new float[] { };
    }


    /**
     * Creates the singleton instance of the dictionary service.
     */
    public static void init (File dictionaryRoot)
    {
        _singleton = new DictionaryManager (dictionaryRoot);
    }

    /**
     * Get an instance of the dictionary service.
     */
    public static DictionaryManager getInstance ()
    {
        return _singleton;
    }

    /**
     * Protected constructor.
     */
    protected DictionaryManager (File dictionaryRoot) 
    {
        _dictionaryRoot = dictionaryRoot;
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
        Invoker.Unit work = new Invoker.Unit ("DictionaryManager.getLetterSet") {
            public boolean invoke ()
            {
                Dictionary dict = getDictionary (locale);
                char[] chars = dict.randomLetters (count);
                StringBuilder sb = new StringBuilder ();
                for (char c : chars)
                {
                    sb.append (c);
                    sb.append (',');
                }
                sb.deleteCharAt (sb.length() - 1);
                
                listener.requestProcessed (sb.toString());
            
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
        Invoker.Unit work = new Invoker.Unit ("DictionaryManager.checkWord") {
            public boolean invoke ()
            {
                Dictionary dict = getDictionary (locale);
                boolean result = (dict != null && dict.contains (word));
                listener.requestProcessed (result);

                return true;
            }
        };

        // ...and off we go.
        CrowdServer.invoker.postUnit (work);    
    }

    
    /**
     * Retrieves the dictionary object for a given locale.
     * Forces the dictionary file to be loaded, if it hasn't already.
     */
    private Dictionary getDictionary (String locale)
    {
        locale = locale.toLowerCase ();
        if (! _dictionaries.containsKey (locale))
        {
            try
            {
                // Make a file name
                String filename = locale + ".wordlist";
                File file = new File (_dictionaryRoot, filename);
                _dictionaries.put (locale, new Dictionary (file));
            }
            catch (Exception e)
            {
                log.log (Level.WARNING, "Failed to load language file", e);
            }
        }

        return _dictionaries.get (locale);
    }




    // PRIVATE VARIABLES

    /** Singleton instance pointer */
    private static DictionaryManager _singleton;

    /** Root directory where we find dictionary files */
    private File _dictionaryRoot;

    /** Map from locale name to Dictionary object */
    private HashMap <String, Dictionary> _dictionaries = new HashMap <String, Dictionary> ();

 

}
