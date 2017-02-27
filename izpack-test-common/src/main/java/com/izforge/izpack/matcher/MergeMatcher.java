package com.izforge.izpack.matcher;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.hamcrest.Description;
import org.hamcrest.Factory;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.mock.MockOutputStream;

/**
 * Matcher for mergeable
 *
 * @author Anthonin Bonnefoy
 */
public class MergeMatcher extends TypeSafeMatcher<Mergeable>
{

    private static final Logger LOGGER = Logger.getLogger(MergeMatcher.class.getName());

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
            mergeable.merge(outputStream);
            List<String> entryName = outputStream.getListEntryName();

            boolean match = listMatcher.matches(entryName);
            if (LOGGER.isLoggable(Level.FINE) && !match)
            {
                LOGGER.fine("++++++++++++++++++++++++++++++++++++++");
                LOGGER.fine("\nContents of mergeable " + mergeable + ":\n");
                for (String entry : entryName)
                {
                    LOGGER.fine("\t" + entry);
                }
                LOGGER.fine("\nMATCH: " + match + "\n");
                LOGGER.fine("++++++++++++++++++++++++++++++++++++++");
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
        return new MergeMatcher(IsCollectionContaining.hasItem(Is.is(fileName)));
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
