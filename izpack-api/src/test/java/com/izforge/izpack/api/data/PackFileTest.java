package com.izforge.izpack.api.data;

import com.izforge.izpack.api.data.binding.OsModel;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

public class PackFileTest
{
    @Test
    public void shouldAllowRegularTargetPath() throws Exception
    {
        File source = createTempSourceFile();
        new PackFile(source.getParentFile(), source, "subdir/file.txt", Collections.<OsModel>emptyList(), null,
                null, Blockable.BLOCKABLE_NONE, null, null);
    }

    @Test(expected = IOException.class)
    public void shouldRejectTraversalWithForwardSlashes() throws Exception
    {
        File source = createTempSourceFile();
        new PackFile(source.getParentFile(), source, "subdir/../escape/file.txt", Collections.<OsModel>emptyList(),
                null, null, Blockable.BLOCKABLE_NONE, null, null);
    }

    @Test(expected = IOException.class)
    public void shouldRejectTraversalWithBackslashes() throws Exception
    {
        File source = createTempSourceFile();
        new PackFile(source.getParentFile(), source, "subdir\\..\\escape\\file.txt", Collections.<OsModel>emptyList(),
                null, null, Blockable.BLOCKABLE_NONE, null, null);
    }

    private File createTempSourceFile() throws IOException
    {
        File source = File.createTempFile("packfile-test", ".txt");
        source.deleteOnExit();
        return source;
    }
}
