package com.izforge.izpack.test.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;

public class JarFileProvider implements Provider<JarFile> {
    public static final String JAR_FILE = "jarFile";

    private final File file;

    @Inject
    public JarFileProvider(@Named(JAR_FILE) File file) {
        this.file = file;
    }

    @Override
    public JarFile get() {
        try {
            return new JarFile(file, true);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to open jar file: " + file, e);
        }
    }
}
