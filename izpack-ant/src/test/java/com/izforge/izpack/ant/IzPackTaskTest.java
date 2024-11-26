package com.izforge.izpack.ant;

import com.izforge.izpack.matcher.ZipMatcher;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.PropertyHelper;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Anthonin Bonnefoy
 */
public class IzPackTaskTest
{

    @Test
    @Ignore
    public void testExecuteAntAction() throws IllegalAccessException, InterruptedException, IOException
    {

        IzPackTask task = new IzPackTask();
        initIzpackTask(task);
        task.execute();

        Thread.sleep(30000);
        File file = new File("target/izpackResult.jar");
        ZipFile zipFile = new ZipFile(file);
        assertThat(file.exists(), Is.is(true));
        assertThat(zipFile, ZipMatcher.isZipMatching(IsCollectionContaining.hasItems(
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class",
                "com/izforge/izpack/core/container/AbstractContainer.class",
                "com/izforge/izpack/uninstaller/Destroyer.class"
        )));

    }

    private void initIzpackTask(IzPackTask task) throws IllegalAccessException
    {
        File installFile = new File(getClass().getClassLoader().getResource("helloAndFinish.xml").getFile());
        task.setInput(installFile.getAbsolutePath());
        task.setBasedir(getClass().getClassLoader().getResource("").getFile());
        task.setOutput("target/izpackResult.jar");
        task.setCompression("default");
        task.setCompressionLevel(-1);
        task.setProject(createProject());
        task.setInheritAll(true);
    }

    /**
     * Create a project containing a non String property.
     *
     * The public Project.setProperty allows String properties only.
     * But internally the Hashtable allows properties of any type (Object).
     *
     * If using within a gradle context, saving non String values seems to be usual practice.
     * To put in an foreign Object here is a bit tricky...
     *
     * @return
     */
    private Project createProject() {
        final Project project = new Project();
        PropertyHelper.getPropertyHelper(project).setNewProperty("answer", 42);
        return project;
    }

    @Test
    public void testUrlsForClassloader() throws Exception {
        IzPackTask task = new IzPackTask();

        URL[] urls = task.getUrlsForClassloader();

        assertThat(hasDependency(urls, "izpack-installer"), is(true));
        assertThat(hasDependency(urls, "guava"), is(true));
        assertThat(hasDependency(urls, "guice"), is(true));
    }

    private boolean hasDependency(URL[] urls, String dependency) {
        return Arrays.stream(urls).anyMatch(url -> url.toString().contains(dependency));
    }
}
