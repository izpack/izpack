package com.izforge.izpack.matcher;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.izforge.izpack.util.IoHelper;
import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.mock.MockOutputStream;

/**
 * Matcher for mergeable
 *
 * @author Anthonin Bonnefoy
 */
public class MergeMatcher extends TypeSafeMatcher<Mergeable>
{
    private static final Logger logger = Logger.getLogger(MergeMatcher.class.getName());

    private Matcher<Iterable<String>> listMatcher;

    MergeMatcher(Matcher<Iterable<String>> matcher)
    {
        this.listMatcher = matcher;
    }

    @Override
    public boolean matchesSafely(Mergeable mergeable)
    {
        try
        {
            MockOutputStream outputStream = new MockOutputStream();
            mergeable.merge(IoHelper.mergeTarget(outputStream));
            List<String> entryName = outputStream.getListEntryName();

            boolean match = listMatcher.matches(entryName);
            if (logger.isLoggable(Level.FINE) && !match)
            {
                logger.fine("++++++++++++++++++++++++++++++++++++++");
                logger.fine("\nContents of mergeable " + mergeable + ":\n");
                for (String entry : entryName) {
                  logger.fine("\t" + entry);
                }
                logger.fine("\nMATCH: " + match + "\n");
                logger.fine("++++++++++++++++++++++++++++++++++++++");
            }
            return match;
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void describeTo(Description description)
    {
        description.appendText("Expecting mergeable containing ").appendValue(listMatcher);
    }

    @Factory
    public static Matcher<Mergeable> isMergeableContainingFile(String fileName)
    {
        return new MergeMatcher(IsCollectionContaining.hasItems(Is.is(fileName)));
    }

    @Factory
    public static Matcher<Mergeable> isMergeableMatching(Matcher<Iterable<String>> matcher)
    {
        return new MergeMatcher(matcher);
    }

    @Factory
    public static Matcher<Mergeable> isMergeableContainingFiles(String... files)
    {
        return new MergeMatcher(IsCollectionContaining.hasItems(files));
    }
}