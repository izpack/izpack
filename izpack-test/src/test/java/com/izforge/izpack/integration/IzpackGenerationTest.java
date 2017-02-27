package com.izforge.izpack.integration;

import static org.hamcrest.MatcherAssert.assertThat;

import java.util.concurrent.TimeUnit;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;

import com.izforge.izpack.compiler.container.TestCompilationContainer;
import com.izforge.izpack.matcher.ZipMatcher;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.InstallFile;
import com.izforge.izpack.test.junit.PicoRunner;

/**
 * Test for an installation
 */
@RunWith(PicoRunner.class)
@Container(TestCompilationContainer.class)
public class IzpackGenerationTest
{

    @Rule
    public TestRule globalTimeout = new Timeout(HelperTestMethod.TIMEOUT, TimeUnit.MILLISECONDS);

    private JarFile jar;

    private TestCompilationContainer container;

    public IzpackGenerationTest(TestCompilationContainer container)
    {
        this.container = container;
    }

    @Before
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
