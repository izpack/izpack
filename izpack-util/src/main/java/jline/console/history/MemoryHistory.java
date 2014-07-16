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

import static jline.internal.Preconditions.checkNotNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * Non-persistent {@link History}.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.3
 */
public class MemoryHistory
    implements History
{
    public static final int DEFAULT_MAX_SIZE = 500;

    private final LinkedList<CharSequence> items = new LinkedList<CharSequence>();

    private int maxSize = DEFAULT_MAX_SIZE;

    private boolean ignoreDuplicates = true;

    private boolean autoTrim = false;

    // NOTE: These are all ideas from looking at the Bash man page:

    // TODO: Add ignore space? (lines starting with a space are ignored)

    // TODO: Add ignore patterns?

    // TODO: Add history timestamp?

    // TODO: Add erase dups?

    private int offset = 0;

    private int index = 0;

    public void setMaxSize(final int maxSize) {
        this.maxSize = maxSize;
        maybeResize();
    }

    public int getMaxSize() {
        return maxSize;
    }

    public boolean isIgnoreDuplicates() {
        return ignoreDuplicates;
    }

    public void setIgnoreDuplicates(final boolean flag) {
        this.ignoreDuplicates = flag;
    }

    public boolean isAutoTrim() {
        return autoTrim;
    }

    public void setAutoTrim(final boolean flag) {
        this.autoTrim = flag;
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public int index() {
        return offset + index;
    }

    @Override
    public void clear() {
        items.clear();
        offset = 0;
        index = 0;
    }

    @Override
    public CharSequence get(final int index) {
        return items.get(index - offset);
    }

    @Override
    public void set(int index, CharSequence item) {
        items.set(index - offset, item);
    }

    @Override
    public void add(CharSequence item) {
        checkNotNull(item);

        if (isAutoTrim()) {
            item = String.valueOf(item).trim();
        }

        if (isIgnoreDuplicates()) {
            if (!items.isEmpty() && item.equals(items.getLast())) {
                return;
            }
        }

        internalAdd(item);
    }

    @Override
    public CharSequence remove(int i) {
        return items.remove(i);
    }

    @Override
    public CharSequence removeFirst() {
        return items.removeFirst();
    }

    @Override
    public CharSequence removeLast() {
        return items.removeLast();
    }

    protected void internalAdd(CharSequence item) {
        items.add(item);

        maybeResize();
    }

    @Override
    public void replace(final CharSequence item) {
        items.removeLast();
        add(item);
    }

    private void maybeResize() {
        while (size() > getMaxSize()) {
            items.removeFirst();
            offset++;
        }

        index = size();
    }

    @Override
    public ListIterator<Entry> entries(final int index) {
        return new EntriesIterator(index - offset);
    }

    @Override
    public ListIterator<Entry> entries() {
        return entries(offset);
    }

    @Override
    public Iterator<Entry> iterator() {
        return entries();
    }

    private static class EntryImpl
        implements Entry
    {
        private final int index;

        private final CharSequence value;

        public EntryImpl(int index, CharSequence value) {
            this.index = index;
            this.value = value;
        }

        @Override
        public int index() {
            return index;
        }

        @Override
        public CharSequence value() {
            return value;
        }

        @Override
        public String toString() {
            return String.format("%d: %s", index, value);
        }
    }

    private class EntriesIterator
        implements ListIterator<Entry>
    {
        private final ListIterator<CharSequence> source;

        private EntriesIterator(final int index) {
            source = items.listIterator(index);
        }

        @Override
        public Entry next() {
            if (!source.hasNext()) {
                throw new NoSuchElementException();
            }
            return new EntryImpl(offset + source.nextIndex(), source.next());
        }

        @Override
        public Entry previous() {
            if (!source.hasPrevious()) {
                throw new NoSuchElementException();
            }
            return new EntryImpl(offset + source.previousIndex(), source.previous());
        }

        @Override
        public int nextIndex() {
            return offset + source.nextIndex();
        }

        @Override
        public int previousIndex() {
            return offset + source.previousIndex();
        }

        @Override
        public boolean hasNext() {
            return source.hasNext();
        }

        @Override
        public boolean hasPrevious() {
            return source.hasPrevious();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(final Entry entry) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(final Entry entry) {
            throw new UnsupportedOperationException();
        }
    }

    //
    // Navigation
    //

    /**
     * This moves the history to the last entry. This entry is one position
     * before the moveToEnd() position.
     *
     * @return Returns false if there were no history entries or the history
     *         index was already at the last entry.
     */
    @Override
    public boolean moveToLast() {
        int lastEntry = size() - 1;
        if (lastEntry >= 0 && lastEntry != index) {
            index = size() - 1;
            return true;
        }

        return false;
    }

    /**
     * Move to the specified index in the history
     * @param index
     * @return
     */
    @Override
    public boolean moveTo(int index) {
        index -= offset;
        if (index >= 0 && index < size() ) {
            this.index = index;
            return true;
        }
        return false;
    }

    /**
     * Moves the history index to the first entry.
     *
     * @return Return false if there are no entries in the history or if the
     *         history is already at the beginning.
     */
    @Override
    public boolean moveToFirst() {
        if (size() > 0 && index != 0) {
            index = 0;
            return true;
        }

        return false;
    }

    /**
     * Move to the end of the history buffer. This will be a blank entry, after
     * all of the other entries.
     */
    @Override
    public void moveToEnd() {
        index = size();
    }

    /**
     * Return the content of the current buffer.
     */
    @Override
    public CharSequence current() {
        if (index >= size()) {
            return "";
        }

        return items.get(index);
    }

    /**
     * Move the pointer to the previous element in the buffer.
     *
     * @return true if we successfully went to the previous element
     */
    @Override
    public boolean previous() {
        if (index <= 0) {
            return false;
        }

        index--;

        return true;
    }

    /**
     * Move the pointer to the next element in the buffer.
     *
     * @return true if we successfully went to the next element
     */
    @Override
    public boolean next() {
        if (index >= size()) {
            return false;
        }

        index++;

        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry e : this) {
            sb.append(e.toString() + "\n");
        }
        return sb.toString();
    }
}
