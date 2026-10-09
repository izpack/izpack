package com.izforge.izpack.matcher;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides ZIP entry names for assertions.
 */
public final class ZipMatcher
{
    private ZipMatcher()
    {
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

}
