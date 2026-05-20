package com.izforge.izpack.installer.console;

import java.io.StringWriter;

/**
 * A StringWriter that automatically wraps text at whitespace near a specified column width.
 */
public final class WrappingStringWriter extends StringWriter
{
    private final int wrapColumn;
    private int currentColumn = 0;

    public WrappingStringWriter(int initialSize, int wrapColumn)
    {
        super(initialSize);
        this.wrapColumn = wrapColumn;
    }

    @Override
    public StringWriter append(CharSequence csq)
    {
        return append(csq, 0, csq.length());
    }

    @Override
    public StringWriter append(CharSequence csq, int start, int end)
    {
        for (int i = start; i < end; i++) {
            append(csq.charAt(i));
        }
        return this;
    }

    @Override
    public StringWriter append(char c)
    {
        if (c == '\n')
        {
            super.append(c);
            currentColumn = 0;
        }
        else
        {
            if (currentColumn >= wrapColumn && Character.isWhitespace(c))
            {
                super.append('\n');
                currentColumn = 0;
            }
            else if (currentColumn >= wrapColumn)
            {
                // If we've exceeded the column limit and this isn't whitespace,
                // look for the last whitespace in the buffer
                String content = getBuffer().toString();
                int lastIndex = content.lastIndexOf(' ', content.length() - 1);
                if (lastIndex != -1 && content.length() - lastIndex <= wrapColumn) {
                    // Replace the last whitespace with a newline
                    getBuffer().setCharAt(lastIndex, '\n');
                    currentColumn = content.length() - lastIndex - 1;
                }
                super.append(c);
                currentColumn++;
            }
            else
            {
                super.append(c);
                currentColumn++;
            }
        }
        return this;
    }
}
