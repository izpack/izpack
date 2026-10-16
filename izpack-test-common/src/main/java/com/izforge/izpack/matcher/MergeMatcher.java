package com.izforge.izpack.matcher;

import static com.izforge.izpack.util.IoHelper.mergeTarget;

import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.mock.MockOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * Provides merge entry names for assertions.
 */
public final class MergeMatcher
{
    private MergeMatcher()
    {
    }

    public static List<String> getEntryNames(Mergeable mergeable)
    {
        try
        {
            MockOutputStream outputStream = new MockOutputStream();
            mergeable.merge(mergeTarget(outputStream));
            return outputStream.getListEntryName();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
