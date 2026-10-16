package com.izforge.izpack.matcher;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Reads serialized objects from ZIP entries for assertions.
 */
public final class ObjectInputMatcher
{
    private ObjectInputMatcher()
    {
    }

    public static Object getObjectFromZip(ZipFile file, String resourceId)
        throws IOException, ClassNotFoundException
    {
        Object result = null;
        Enumeration<? extends ZipEntry> zipEntries = file.entries();
        while (zipEntries.hasMoreElements()) {
          ZipEntry zipEntry = zipEntries.nextElement();
          if (zipEntry.getName().equals(resourceId))
          {
              ObjectInputStream inputStream = new ObjectInputStream(file.getInputStream(zipEntry));
              result = inputStream.readObject();
          }
        }
        return result;
    }

}
