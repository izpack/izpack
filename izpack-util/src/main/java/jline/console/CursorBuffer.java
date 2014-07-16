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

package jline.console;

import static jline.internal.Preconditions.checkNotNull;

/**
 * A holder for a {@link StringBuilder} that also contains the current cursor position.
 *
 * @author <a href="mailto:mwp1@cornell.edu">Marc Prud'hommeaux</a>
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @since 2.0
 */
public class CursorBuffer
{
    private boolean overTyping = false;

    public int cursor = 0;

    public final StringBuilder buffer = new StringBuilder();

    public CursorBuffer copy () {
        CursorBuffer that = new CursorBuffer();
        that.overTyping = this.overTyping;
        that.cursor = this.cursor;
        that.buffer.append (this.toString());

        return that;
    }

    public boolean isOverTyping() {
        return overTyping;
    }

    public void setOverTyping(final boolean b) {
        overTyping = b;
    }

    public int length() {
        return buffer.length();
    }

    public char nextChar() {
        if (cursor == buffer.length()) {
            return 0;
        } else {
            return buffer.charAt(cursor);
        }
    }

    public char current() {
        if (cursor <= 0) {
            return 0;
        }

        return buffer.charAt(cursor - 1);
    }

    /**
     * Write the specific character into the buffer, setting the cursor position
     * ahead one. The text may overwrite or insert based on the current setting
     * of {@link #isOverTyping}.
     *
     * @param c the character to insert
     */
    public void write(final char c) {
        buffer.insert(cursor++, c);
        if (isOverTyping() && cursor < buffer.length()) {
            buffer.deleteCharAt(cursor);
        }
    }

    /**
     * Insert the specified chars into the buffer, setting the cursor to the end of the insertion point.
     */
    public void write(final CharSequence str) {
        checkNotNull(str);

        if (buffer.length() == 0) {
            buffer.append(str);
        }
        else {
            buffer.insert(cursor, str);
        }

        cursor += str.length();

        if (isOverTyping() && cursor < buffer.length()) {
            buffer.delete(cursor, (cursor + str.length()));
        }
    }

    public boolean clear() {
        if (buffer.length() == 0) {
            return false;
        }

        buffer.delete(0, buffer.length());
        cursor = 0;
        return true;
    }

    public String upToCursor() {
        if (cursor <= 0) {
            return "";
        }

        return buffer.substring(0, cursor);
    }

    @Override
    public String toString() {
        return buffer.toString();
    }
}
