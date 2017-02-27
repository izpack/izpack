package com.izforge.izpack.test.provider;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.injectors.ProviderAdapter;

public class JarFileProvider extends ProviderAdapter
{

    public JarFile provide(File file) throws IOException
    {
        return new JarFile(file, true);
    }

    @Override
    public boolean isLazy(ComponentAdapter<?> adapter)
    {
        return true;
    }
}
