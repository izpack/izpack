package com.izforge.izpack.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;

/**
 * Matcher for zip files
 *
 * @author Anthonin Bonnefoy
 */
public class ZipMatcher extends TypeSafeMatcher<ZipFile>
{
    private static final Logger logger = Logger.getLogger(ZipMatcher.class.getName());

    private Matcher<Iterable<String>> listMatcher;

    ZipMatcher(Matcher<Iterable<String>> matcher)
    {
        this.listMatcher = matcher;
    }

    @Override
    public boolean matchesSafely(ZipFile file)
    {
        try
        {
            List<String> fileList = getFileNameListFromZip(file);
            // MatcherAssert.assertThat(fileList, listMatcher); // This prevents the use of IsNot. TODO
            boolean match = listMatcher.matches(fileList);
            if (logger.isLoggable(Level.FINE) && !match)
            {
                logger.fine("++++++++++++++++++++++++++++++++++++++");
                logger.fine("\nContents of zip file " + file.getName() + ":\n");
                for (String f : fileList) {
                  logger.fine("\t" + f);
                }
                logger.fine("\nMATCH: " + match + "\n");
                logger.fine("++++++++++++++++++++++++++++++++++++++");
            }
            return match;
        }
        catch (IOException e)
        {
            throw new AssertionError(e);
        }
    }


    public static List<String> getFileNameListFromZip(ZipFile file)
            throws IOException
    {
        List<String> entryList = new ArrayList<String>();
        Enumeration<? extends ZipEntry> zipEntries = file.entries();
        while (zipEntries.hasMoreElements()) {
          ZipEntry zipEntry = zipEntries.nextElement();
          entryList.add(zipEntry.getName());
        }
        return entryList;
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Expecting ").appendValue(listMatcher);
    }

    @Factory
    public static Matcher<ZipFile> isZipContainingFile(String fileName)
    {
        return new ZipMatcher(IsCollectionContaining.hasItems(Is.is(fileName)));
    }

    @Factory
    public static Matcher<ZipFile> isZipContainingFiles(String... fileNames)
    {
        return new ZipMatcher(IsCollectionContaining.hasItems(fileNames));
    }

    @Factory
    public static Matcher<ZipFile> isZipMatching(Matcher<Iterable<String>> matcher)
    {
        return new ZipMatcher(matcher);
    }
}
