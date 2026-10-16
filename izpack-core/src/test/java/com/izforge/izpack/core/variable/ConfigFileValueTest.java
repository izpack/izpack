package com.izforge.izpack.core.variable;

import static java.nio.charset.Charset.defaultCharset;
import static java.nio.file.Files.createFile;
import static java.nio.file.Files.newBufferedWriter;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class ConfigFileValueTest
{
    @TempDir
    public Path folder;

    private Path properties;
    private Path zipFile;
    private Path jarFile;

    @BeforeEach
    public void setUp() throws Exception
    {
        properties = createFile(folder.resolve("test.properties"));
        BufferedWriter out = new BufferedWriter(newBufferedWriter(properties, defaultCharset()));
        out.write("test.path = C:\\mypath\\myfile\n");
        out.write("test.path2 = C:\\\\mypath\\\\myfile\n");
        out.close();

        byte[] buf = new byte[1024];

        try {
            zipFile = createFile(folder.resolve("test.zip"));
            ZipOutputStream zout = new ZipOutputStream(newOutputStream(zipFile));
            InputStream in = newInputStream(properties);
            zout.putNextEntry(new ZipEntry("test.properties"));
            int len;
            while ((len = in.read(buf)) > 0) {
                zout.write(buf, 0, len);
            }
            zout.closeEntry();
            in.close();
            zout.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }

        try {
            jarFile = createFile(folder.resolve("test.jar"));
            JarOutputStream jout = new JarOutputStream(newOutputStream(jarFile));
            InputStream in = newInputStream(properties);
            jout.putNextEntry(new JarEntry("test.properties"));
            int len;
            while ((len = in.read(buf)) > 0) {
                jout.write(buf, 0, len);
            }
            jout.closeEntry();
            in.close();
            jout.close();
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPlainConfigFileValue()
    {
        PlainConfigFileValue value = new PlainConfigFileValue(properties.toString(), ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "test.path", false);
        PlainConfigFileValue value2 = new PlainConfigFileValue(properties.toString(), ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "test.path2", true);
        try
        {
            assertThat(value.resolve()).isEqualTo("C:\\mypath\\myfile");
            assertThat(value2.resolve()).isEqualTo("C:\\mypath\\myfile");
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testZipConfigFileValue()
    {
        ZipEntryConfigFileValue value = new ZipEntryConfigFileValue(zipFile.toString(), "test.properties", ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "test.path", false);
        ZipEntryConfigFileValue value2 = new ZipEntryConfigFileValue(zipFile.toString(), "test.properties", ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "test.path2", true);
        try
        {
            assertThat(value.resolve()).isEqualTo("C:\\mypath\\myfile");
            assertThat(value2.resolve()).isEqualTo("C:\\mypath\\myfile");
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @Test
    public void testJarConfigFileValue()
    {
        JarEntryConfigValue value = new JarEntryConfigValue(zipFile.toString(), "test.properties", ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "test.path", false);
        JarEntryConfigValue value2 = new JarEntryConfigValue(zipFile.toString(), "test.properties", ConfigFileValue.CONFIGFILE_TYPE_OPTIONS, null, "test.path2", true);
        try
        {
            assertThat(value.resolve()).isEqualTo("C:\\mypath\\myfile");
            assertThat(value2.resolve()).isEqualTo("C:\\mypath\\myfile");
        }
        catch (Exception e)
        {
            fail(e.getMessage());
        }
    }

    @AfterEach
    public void cleanUp() {
       assertThat(properties).exists();
       assertThat(zipFile).exists();
       assertThat(jarFile).exists();
    }
}
