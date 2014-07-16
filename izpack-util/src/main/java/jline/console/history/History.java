/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2002-2012, the original authors of the JLine project
 * Copyright 2014 René Krell
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jline.console.history;

import java.util.Iterator;
import java.util.ListIterator;

/**
 * Console history.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public interface History
    extends Iterable<History.Entry>
{
    int size();

    boolean isEmpty();

    int index();

    void clear();

    CharSequence get(int index);

    void add(CharSequence line);

    /**
     * Set the history item at the given index to the given CharSequence.
     *
     * @param index the index of the history offset
     * @param item the new item
     * @since 2.7
     */
    void set(int index, CharSequence item);

    /**
     * Remove the history element at the given index.
     *
     * @param i the index of the element to remove
     * @return the removed element
     * @since 2.7
     */
    CharSequence remove(int i);

    /**
     * Remove the first element from history.
     *
     * @return the removed element
     * @since 2.7
     */
    CharSequence removeFirst();

    /**
     * Remove the last element from history
     *
     * @return the removed element
     * @since 2.7
     */
    CharSequence removeLast();

    void replace(CharSequence item);

    //
    // Entries
    //

    interface Entry
    {
        int index();

        CharSequence value();
    }

    ListIterator<Entry> entries(int index);

    ListIterator<Entry> entries();

    @Override
    Iterator<Entry> iterator();

    //
    // Navigation
    //

    CharSequence current();

    boolean previous();

    boolean next();

    boolean moveToFirst();

    boolean moveToLast();

    boolean moveTo(int index);

    void moveToEnd();
}
