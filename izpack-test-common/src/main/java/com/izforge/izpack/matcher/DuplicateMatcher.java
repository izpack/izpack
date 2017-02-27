package com.izforge.izpack.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.Is;

/**
 * List matcher for double item detection
 *
 * @author Anthonin Bonnefoy
 */
public class DuplicateMatcher extends TypeSafeMatcher<Iterable<String>>
{

    public Matcher<String> itemMatcher;

    public DuplicateMatcher(Matcher<String> itemMatcher)
    {
        this.itemMatcher = itemMatcher;
    }

    @Override
    public boolean matchesSafely(Iterable<String> strings)
    {
        boolean alreadyFound = false;
        for (String string : strings)
        {
            if (itemMatcher.matches(string))
            {
                if (alreadyFound)
                {
                    return false;
                }
                alreadyFound = true;
            }
        }
        return alreadyFound;
    }

    public void describeTo(Description description)
    {
        description.appendText("List containing a unique entity ").appendValue(itemMatcher);
    }

    public static DuplicateMatcher isEntryUnique(String entry)
    {
        return new DuplicateMatcher(Is.is(entry));
    }
}
