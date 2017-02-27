package com.izforge.izpack.matcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.MatcherAssert;
import org.hamcrest.TypeSafeMatcher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Matcher for object serialized inside a jar
 *
 * @author Anthonin Bonnefoy
 */
public class ObjectInputMatcher extends TypeSafeMatcher<ZipFile>
{

    private Matcher<Object> listMatcher;
    private String resourceId;

    ObjectInputMatcher(String resourceId, Matcher<Object> listMatcher)
    {
        this.listMatcher = listMatcher;
        this.resourceId = resourceId;
    }

    @Override
    public boolean matchesSafely(ZipFile file)
    {
        try
        {
            Object object = getObjectFromZip(file, resourceId);
            MatcherAssert.assertThat(object, listMatcher);
            return true;
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }
    }

    public static Object getObjectFromZip(ZipFile file, String resourceId)
            throws IOException, ClassNotFoundException
    {
        Object result = null;
        Enumeration<? extends ZipEntry> zipEntries = file.entries();
        while (zipEntries.hasMoreElements())
        {
            ZipEntry zipEntry = zipEntries.nextElement();
            if (zipEntry.getName().equals(resourceId))
            {
                ObjectInputStream inputStream = new ObjectInputStream(file.getInputStream(zipEntry));
                result = inputStream.readObject();
            }
        }
        return result;
    }

    public void describeTo(Description description)
    {
        description.appendText("Excepting file containing ").appendValue(listMatcher);
    }

    public static ObjectInputMatcher isInputMatching(String resourceId, Matcher<Object> objectMatcher)
    {
        return new ObjectInputMatcher(resourceId, objectMatcher);
    }

}
