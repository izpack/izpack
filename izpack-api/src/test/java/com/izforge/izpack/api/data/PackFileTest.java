package com.izforge.izpack.api.data;

import static java.nio.file.Files.createTempFile;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.izforge.izpack.api.data.binding.OsModel;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PackFileTest
{
    @TempDir
    Path directory;
    @Test
    public void shouldAllowRegularTargetPath() throws Exception
    {
        File source = createTempSourceFile();
        new PackFile(source.getParentFile(), source, "subdir/file.txt", Collections.<OsModel>emptyList(), null,
                null, Blockable.BLOCKABLE_NONE, null, null);
    }

    @Test
    public void shouldRejectTraversalWithForwardSlashes() throws Exception
    {
        assertThatThrownBy(() -> {
            File source = createTempSourceFile();
            new PackFile(source.getParentFile(), source, "subdir/../escape/file.txt", Collections.<OsModel>emptyList(),
                    null, null, Blockable.BLOCKABLE_NONE, null, null);
        }).isInstanceOf(IOException.class);
    }

    @Test
    public void shouldRejectTraversalWithBackslashes() throws Exception
    {
        assertThatThrownBy(() -> {
            File source = createTempSourceFile();
            new PackFile(source.getParentFile(), source, "subdir\\..\\escape\\file.txt", Collections.<OsModel>emptyList(),
                    null, null, Blockable.BLOCKABLE_NONE, null, null);
        }).isInstanceOf(IOException.class);
    }

    private File createTempSourceFile() throws IOException
    {
        return createTempFile(directory, "packfile-test", ".txt").toFile();
    }
}
