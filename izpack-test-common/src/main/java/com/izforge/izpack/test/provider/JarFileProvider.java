package com.izforge.izpack.test.provider;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import jakarta.enterprise.inject.Produces;


public class JarFileProvider
{
    @Produces
    public JarFile provide(File file) throws IOException
    {
      return new JarFile(file, true);
    }
}
