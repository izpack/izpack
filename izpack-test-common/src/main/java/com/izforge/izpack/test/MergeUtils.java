package com.izforge.izpack.test;

import com.izforge.izpack.api.merge.MergeTarget;
import com.izforge.izpack.api.merge.Mergeable;
import com.izforge.izpack.util.IoHelper;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipOutputStream;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 */
public class MergeUtils
{
    public static File doDoubleMerge(Mergeable mergeable)
            throws IOException
    {
        Path tempFile = Files.createTempFile("test", ".zip");
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(tempFile)))
        {
            MergeTarget mergeTarget = IoHelper.mergeTarget(outputStream);
            mergeable.merge(mergeTarget);
            mergeable.merge(mergeTarget);
        }
        return tempFile.toFile();
    }

    public static File doMerge(Mergeable mergeable)
            throws IOException
    {
        Path tempFile = Files.createTempFile("test", ".zip");
        try (ZipOutputStream outputStream = new ZipOutputStream(Files.newOutputStream(tempFile)))
        {
            MergeTarget mergeTarget = IoHelper.mergeTarget(outputStream);
            mergeable.merge(mergeTarget);
        }
        return tempFile.toFile();
    }


}
