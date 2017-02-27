/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

package com.izforge.izpack.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

public class WordUtil
{
    public static void main(String[] args) throws FileNotFoundException
    {
        InputStream in = null;
        try
        {
            in = new BufferedInputStream(new FileInputStream("/home/rkrell/Downloads/pentaho-eula-wrap-config-1.0.7-eula.txt"));
            for (String line : WordUtil.wordWrap(in, 80))
            {
                System.out.println(line);
            }
        }
        finally
        {
            try
            {
                if (in != null)
                {
                    in.close();
                }
            }
            catch (IOException ignored) {}
        }
    }

    public static String wordWrap(String text, int maxCharsPerLine)
    {
        return wordWrap(text, maxCharsPerLine, true);
    }

    /**
     * Processes a given text and wraps it word boundary according to a defined line width.
     *
     * @param text            the raw text to be processed
     * @param maxCharsPerLine the line width
     * @param wrapLineFull    whether wrapped lines should be explicitly divided by a newline character if the computed
     *                        line is exactly as long as the line width. This must be set {@code false} if the processed
     *                        text should be printed to a terminal which automatically wraps to the new line after the
     *                        last character has been printed at the last column.
     * @return the processed text
     */
    public static String wordWrap(String text, int maxCharsPerLine, boolean wrapLineFull)
    {
        StringBuilder sb = new StringBuilder();
        for (String line : wordWrap(new ByteArrayInputStream(text.getBytes()), maxCharsPerLine))
        {
            sb.append(line);
            if (line.length() < maxCharsPerLine || wrapLineFull)
            {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public static List<String> wordWrap(InputStream in, int maxCharsPerLine)
    {
        List<String> lines = new ArrayList<String>();
        Scanner scanner = new Scanner(in);

        while (scanner.hasNextLine())
        {
            String readLine = scanner.nextLine();
            StringBuilder sb = new StringBuilder();

            StringTokenizer tokenizer = new StringTokenizer(readLine);
            // Add explicit line breaks from original document
            if (tokenizer.countTokens() == 0)
            {
                lines.add(" ");
            }

            while (tokenizer.hasMoreTokens())
            {
                String word = tokenizer.nextToken();
                int len = word.length();
                if (sb.length() > 0)
                {
                    if (len + 1 > maxCharsPerLine - sb.length())
                    {
                        lines.add(sb.toString());
                        sb = new StringBuilder();
                    }
                    else
                    {
                        if (len < maxCharsPerLine)
                        {
                            sb.append(' ');
                        }
                    }
                }
                sb.append(word);
            }
            if (sb.length() > 0)
            {
                lines.add(sb.toString());
            }
        }

        return lines;
    }
}
