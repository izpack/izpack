package com.izforge.izpack.integration;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

import com.izforge.izpack.compiler.container.TestCompilationContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoExtension;
/**
 * Test for an installation
 */

@ExtendWith(PicoExtension.class)
@Container(TestCompilationContainer.class)
@Timeout(value = HelperTestMethod.TIMEOUT, unit = TimeUnit.MILLISECONDS) // Global timeout applied to all tests
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
        assertThat((ZipFile) jar, ZipMatcher.isZipContainingFiles(
                "com/izforge/izpack/panels/hello/HelloPanel.class"
        ));
    }
}
