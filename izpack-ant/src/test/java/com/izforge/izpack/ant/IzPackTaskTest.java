package com.izforge.izpack.ant;

import static com.izforge.izpack.matcher.ZipMatcher.getFileNameListFromZip;
import static java.lang.Thread.sleep;
import static org.apache.tools.ant.PropertyHelper.getPropertyHelper;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import org.apache.tools.ant.Project;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * @author Anthonin Bonnefoy
 */
public class IzPackTaskTest
{

    @Test
    @Disabled
    public void testExecuteAntAction() throws IllegalAccessException, InterruptedException, IOException
    {

        IzPackTask task = new IzPackTask();
        initIzpackTask(task);
        task.execute();

        sleep(30000);
        Path file = Path.of("target/izpackResult.jar");
        assertThat(file).exists();
        try (ZipFile zipFile = new ZipFile(file.toFile()))
        {
            assertThat(getFileNameListFromZip(zipFile)).contains(
                    "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class",
                    "com/izforge/izpack/core/container/AbstractContainer.class",
                    "com/izforge/izpack/uninstaller/Destroyer.class");
        }

    }

    private void initIzpackTask(IzPackTask task) throws IllegalAccessException
    {
        Path installFile = Path.of(getClass().getClassLoader().getResource("helloAndFinish.xml").getFile());
        task.setInput(installFile.toAbsolutePath().toString());
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
        getPropertyHelper(project).setNewProperty("answer", new Integer(42));
        return project;
    }

}
