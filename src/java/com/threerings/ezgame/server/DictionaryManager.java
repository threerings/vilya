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

package com.threerings.ezgame.server;

import com.samskivert.util.Invoker;
import com.samskivert.util.ResultListener;

import com.threerings.crowd.server.CrowdServer;

import com.threerings.presents.client.InvocationService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.zip.GZIPInputStream;

import com.samskivert.util.CountHashMap;
import com.samskivert.util.RandomUtil;

import static com.threerings.ezgame.server.Log.log;

/**
 * Manages loading and querying word dictionaries in multiple languages.
 *
 * NOTE: the service supports lazy loading of language files, but does not _unload_ them from
 * memory, leading to increasing memory usage.
 *
 * NOTE: the dictionary service has not yet been tested with language files written in non-default
 * character encodings.
 */
public class DictionaryManager
{
    /**
     * Creates the singleton instance of the dictionary service.
     *
     * @param prefix used to locate dictionary files in the classpath. A dictionary for "en_US" for
     * example, would be searched for as "prefix/en_US.wordlist.gz".
     */
    public static void init (String prefix)
    {
        _singleton = new DictionaryManager(prefix);
    }

    /**
     * Get an instance of the dictionary service.
     */
    public static DictionaryManager getInstance ()
    {
        return _singleton;
    }

    /**
     * Returns true if the language is known to be supported by the dictionary service (would it be
     * better to return a whole list of supported languages instead?)
     */
    public void isLanguageSupported (final String locale,
                                     final InvocationService.ResultListener listener)
    {
        // TODO: once we have file paths set up, change this to match
        // against dictionary files
        listener.requestProcessed(locale.toLowerCase().startsWith("en"));
    }

    /**
     * Retrieves a set of letters from a language definition file, and returns a random sampling of
     * /count/ elements.
     */
    // TODO: honor the dictionary parameter
    public void getLetterSet (
        final String locale, final String dictionary, final int count,
        final InvocationService.ResultListener listener)
    {
        CrowdServer.invoker.postUnit(new Invoker.Unit("DictionaryManager.getLetterSet") {
            public boolean invoke () {
                Dictionary dict = getDictionary(locale);
                char[] chars = dict.randomLetters(count);
                StringBuilder sb = new StringBuilder();
                for (char c : chars) {
                    sb.append(c);
                    sb.append(',');
                }
                sb.deleteCharAt(sb.length() - 1);
                _set = sb.toString();
                return true;
            }
            public void handleResult () {
                listener.requestProcessed(_set);
            }
            protected String _set;
        });
    }

    /**
     * Checks if the specified word exists in the given language
     */
    // TODO: honor the dictionary parameter
    public void checkWord (
        final String locale, final String dictionary, final String word,
        final InvocationService.ResultListener listener)
    {
        CrowdServer.invoker.postUnit(new Invoker.Unit("DictionaryManager.checkWord") {
            public boolean invoke () {
                Dictionary dict = getDictionary(locale);
                _result = (dict != null && dict.contains(word));
                return true;
            }
            public void handleResult () {
                listener.requestProcessed(_result);
            }
            protected boolean _result;
        });
    }

    /**
     * Protected constructor.
     */
    protected DictionaryManager (String prefix)
    {
        _prefix = prefix;
    }

    /**
     * Retrieves the dictionary object for a given locale.  Forces the dictionary file to be
     * loaded, if it hasn't already.
     */
    protected Dictionary getDictionary (String locale)
    {
        locale = locale.toLowerCase();
        if (!_dictionaries.containsKey(locale)) {
            String path = _prefix + "/" + locale + ".wordlist.gz";
            try {
                InputStream in = getClass().getClassLoader().getResourceAsStream(path);
                _dictionaries.put(locale, new Dictionary(locale, new GZIPInputStream(in)));
            } catch (Exception e) {
                log.log(Level.WARNING, "Failed to load dictionary [path=" + path + "].", e);
            }
        }
        return _dictionaries.get(locale);
    }

    /**
     * Helper class, encapsulates a sorted array of word hashes, which can be used to look up the
     * existence of a word.
     */
    protected class Dictionary
    {
        /**
         * Constructor, loads up the word list and initializes storage.  This naive version assumes
         * language files are simple list of words, with one word per line.
         */
        public Dictionary (String locale, InputStream words)
            throws IOException
        {
            CountHashMap <Character> letters = new CountHashMap <Character>();

            if (words != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(words));
                String line = null;
                while ((line = reader.readLine()) != null) {
                    String word = line.toLowerCase();
                    // add the word to the dictionary
                    _words.add(word);
                    // then count characters
                    for (int ii = word.length() - 1; ii >= 0; ii--) {
                        char ch = word.charAt(ii);
                        letters.incrementCount(ch, 1);
                    }
                }

            } else {
                log.warning("Missing dictionary file [locale=" + locale + "].");
            }

            initializeLetterCounts(letters);

            log.fine("Loaded dictionary [locale=" + locale + ", words=" + _words.size() +
                     ", letters=" + letters + "].");
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
            for (int i = 0; i < count; i++) {
                // find random index and get its letter
                int index = RandomUtil.getWeightedIndex(_counts);
                results[i] = _letters[index];
            }

            return results;
        }


        // PROTECTED HELPERS

        /** Given a CountHashMap of letters, initializes the internal letter and count arrays, used
         * by RandomUtil. */
        protected void initializeLetterCounts (CountHashMap <Character> letters)
        {
            Set<Character> keys = letters.keySet();
            int keycount = keys.size();
            int total = letters.getTotalCount();
            if (total == 0) { return; } // Something went wrong, abort.

            // Initialize storage
            _letters = new char[keycount];
            _counts = new float[keycount];

            // Copy letters and normalize counts
            for (Character key : keys)
            {
                keycount--;
                _letters[keycount] = (char) key;
                _counts[keycount] = ((float) letters.getCount(key)) / total; // normalize
            }
        }

        /** The words. */
        protected HashSet<String> _words = new HashSet<String>();

        /** Letter array. */
        protected char[] _letters = new char[] { };

        /** Letter count array. */
        protected float[] _counts = new float[] { };
    }

    /** Used to locate dictionaries in the classpath. */
    protected String _prefix;

    /** Map from locale name to Dictionary object. */
    protected HashMap <String, Dictionary> _dictionaries = new HashMap <String, Dictionary>();

    /** Singleton instance pointer. */
    protected static DictionaryManager _singleton;
}
