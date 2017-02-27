package com.izforge.izpack.test;

import com.izforge.izpack.api.merge.Mergeable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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
        File tempFile = File.createTempFile("test", ".zip");
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
        mergeable.merge(outputStream);
        mergeable.merge(outputStream);
        outputStream.close();
        return tempFile;
    }

    public static File doMerge(Mergeable mergeable)
            throws IOException
    {
        File tempFile = File.createTempFile("test", ".zip");
        ZipOutputStream outputStream = new ZipOutputStream(new FileOutputStream(tempFile));
        mergeable.merge(outputStream);
        outputStream.close();
        return tempFile;
    }

}
