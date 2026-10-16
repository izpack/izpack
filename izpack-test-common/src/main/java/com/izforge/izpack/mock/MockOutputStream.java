package com.izforge.izpack.mock;

import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Mock for outputStream
 *
 * @author Anthonin Bonnefoy
 */
public class MockOutputStream extends ZipOutputStream
{

    private final List<String> listEntryName = new ArrayList<String>();

    public List<String> getListEntryName()
    {
        return listEntryName;
    }

    @Override
    public void putNextEntry(ZipEntry ze) throws IOException
    {
        listEntryName.add(ze.getName());
    }

    public MockOutputStream() throws IOException
    {
        super(Files.newOutputStream(Files.createTempFile("test", "test"), StandardOpenOption.DELETE_ON_CLOSE));
    }

    @Override
    public void write(int b) throws IOException
    {
    }

    @Override
    public void write(byte[] b) throws IOException
    {
    }

    @Override
    public void write(byte[] b, int offset, int length) throws IOException
    {
    }
}
