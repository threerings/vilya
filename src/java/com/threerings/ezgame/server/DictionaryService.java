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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

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
public class DictionaryService
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
                if (wordfile.exists() && wordfile.isFile() && wordfile.canRead())
                {
                    // File length will give us a rough starting point for the word array
                    long bytecount = wordfile.length();
                    int approxWords = (int) (bytecount / 5); // just a heuristic,
                    _hashes.ensureCapacity (approxWords);    //   we'll trim the vector later

                    // Read it line by line
                    BufferedReader reader = new BufferedReader (new FileReader (wordfile));
                    String line = null;
                    while ((line = reader.readLine()) != null)
                    {
                        // Add the word to the dictionary
                        int hash = hashWord (line); 
                        _hashes.add (hash);
                        
                        // Add each letter to a letter set
                        int len = line.length ();
                        for (int i = 0; i < len; i++)
                        {
                            char ch = line.charAt (i);
                            if (! _letters.containsKey (ch))
                            {
                                _letters.put (ch, 1);
                            }
                            else
                            {
                                _letters.put (ch, _letters.get (ch) + 1);
                            }
                        }
                        
                    }

                    // Trim and sort the vector
                    _hashes.trimToSize();
                    Collections.sort (_hashes);

                    log.log (Level.INFO,
                             "Loaded dictionary file " + wordfile.getName () +
                             " with " + _hashes.size () + " entries, " +
                             _letters.size () + " letters.");
                                                          
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
                _hashes.clear();  // dump everything
            }
        }

        /** Checks if the specified word exists in the word list */
        public boolean contains (String word)
        {
            // Hash the word and check
            int hash = hashWord (word);
            int result = Collections.binarySearch (_hashes, hash);
            return (result >= 0);
        }

        /** Gets an array of random letters for the language, with uniform distribution. */
        public char[] randomLetters (int count)
        {
            Set <Character> letterSet = _letters.keySet();
            int letterCount = letterSet.size();
            Character [] letters = new Character [letterCount];
            letterSet.toArray (letters);
            
            char [] results = new char [count];
            
            for (int i = 0; i < count; i++)
            {
                int r = _random.nextInt (letterCount);
                char ch = letters[r];
                results[i] = ch;
            }

            return results;
        }
            

        /** Hashes the word, for use in storage */
        private int hashWord (String word)
        {
            return word.toLowerCase().hashCode();
        }

        // PRIVATE STORAGE

        /** Sorted vector of word hashes */
        private Vector <Integer> _hashes = new Vector <Integer> ();

        /** Mapping from letters in this language to their total count */
        private HashMap <Character, Integer> _letters = new HashMap <Character, Integer> ();

        /** Random number generator */
        private Random _random = new Random ();
    }


    /**
     * Obsolete init function. I'm keeping it around to prevent
     * any automated build breakage between the upcoming Vilya checkin
     * and the subsequent MSoy checkin. But this will go away real soon.
     */
    public static void init ()
    {
    }

    /**
     * Creates the singleton instance of the dictionary service.
     */
    public static void init (File dictionaryRoot)
    {
        _singleton = new DictionaryService (dictionaryRoot);
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
    protected DictionaryService (File dictionaryRoot) 
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
        Invoker.Unit work = new Invoker.Unit ("DictionaryService.getLetterSet") {
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
        Invoker.Unit work = new Invoker.Unit ("DictionaryService.checkWord") {
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
    private static DictionaryService _singleton;

    /** Root directory where we find dictionary files */
    private File _dictionaryRoot;

    /** Map from locale name to Dictionary object */
    private HashMap <String, Dictionary> _dictionaries = new HashMap <String, Dictionary> ();

 

}
