package com.izforge.izpack.integration;

import static com.izforge.izpack.matcher.ZipMatcher.getFileNameListFromZip;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.compiler.container.TestCompilationContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.TestTimeout;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for an installation
 */

@Container(TestCompilationContainer.class)
@TestTimeout(HelperTestMethod.TIMEOUT)
public class IzpackGenerationTest
{

    private JarFile jar;

    private TestCompilationContainer container;

    public IzpackGenerationTest(TestCompilationContainer container)
    {
        this.container = container;
    }

    @BeforeEach
    public void before()
    {
        container.launchCompilation();
        jar = container.getComponent(JarFile.class);
    }

    @Test
    @InstallFile("samples/izpack/install.xml")
    public void testGeneratedIzpackInstaller() throws Exception
    {
        assertThat(getFileNameListFromZip((ZipFile) jar)).contains("com/izforge/izpack/panels/hello/HelloPanel.class");
    }
}