package org.izpack.mojo;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.project.DefaultProjectBuilderConfiguration;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectBuilder;
import org.apache.maven.project.ProjectBuilderConfiguration;
import org.hamcrest.collection.IsCollectionContaining;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.matcher.ZipMatcher;

/**
 * Test of new IzPack mojo
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackNewMojoTest extends AbstractMojoTestCase
{

    @Test
    public void testExecute() throws Exception
    {
        File testPom = new File(Thread.currentThread().getContextClassLoader().getResource("basic-pom.xml").toURI());
        IzPackNewMojo mojo = (IzPackNewMojo) lookupMojo("izpack", testPom);
        assertThat(mojo, IsNull.notNullValue());
        initIzpackMojo(mojo);

        mojo.execute();

        File file = new File("target/sample/izpackResult.jar");
        assertThat(file.exists(), Is.is(true));
        JarFile jar = new JarFile(file);
        assertThat((ZipFile) jar, ZipMatcher.isZipMatching(IsCollectionContaining.hasItems(
                "com/izforge/izpack/core/container/AbstractContainer.class",
                "com/izforge/izpack/uninstaller/Destroyer.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class")));
    }

    @Test
    public void testFixIZPACK_903() throws Exception
    {
        String classifier = "install";
        File file = new File("target/sample/izpackResult-" + classifier + ".jar");

        // Cleanup from any previous runs.
        file.delete();
        assertThat(file.exists(), Is.is(false));

        // Create and configure the mojo.
        File testPom = new File(Thread.currentThread().getContextClassLoader().getResource("basic-pom.xml").toURI());
        IzPackNewMojo mojo = (IzPackNewMojo) lookupMojo("izpack", testPom);
        assertThat(mojo, IsNull.notNullValue());
        initIzpack5Mojo(mojo);

        // In this case the classifier should be set.
        setVariableValueToObject(mojo, "classifier", classifier);

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertEquals(classifier, getVariableValueFromObject(mojo, "classifier"));

        // Verify the generated file exists.
        assertThat(file.exists(), Is.is(true));
    }

    @Test
    public void testFixIZPACK_1400() throws Exception
    {

        // Create and configure the mojo.
        File testPom = new File(Thread.currentThread().getContextClassLoader().getResource("pom-izpack-1400.xml").toURI());
        IzPackNewMojo mojo = (IzPackNewMojo) lookupMojo("izpack", testPom);
        assertThat(mojo, IsNull.notNullValue());
        initIzpack5Mojo(mojo);

        Properties userProps = new Properties();
        userProps.setProperty("property1", "value1");       // simulates "-Dproperty1=value1" on mvn commandline

        MavenSession session = new MavenSession(null, // PlexusContainer container
                null, // Settings settings
                null, // ArtifactRepository localRepository
                null, // EventDispatcher eventDispatcher
                null, // ReactorManager reactorManager
                null, // List goals
                null, // String executionRootDir
                null, // Properties executionProperties
                userProps, // Properties userProperties
                null // Date startTime
        );
        setVariableValueToObject(mojo, "session", session);

        ProjectBuilderConfiguration builderConfig = new DefaultProjectBuilderConfiguration();
        builderConfig.setUserProperties(userProps);
        MavenProjectBuilder builder = (MavenProjectBuilder) lookup(MavenProjectBuilder.ROLE);
        MavenProject project = builder.build(testPom, builderConfig);
        setVariableValueToObject(mojo, "project", project);

        // Execute the mojo.
        mojo.execute();

        // first verify the default behavior of maven
        Properties props = project.getProperties();
        // project.Properties do not reflect the user properties, so property1 reflects pom.xml
        assertEquals("default", props.getProperty("property1"));
        // but computated properties do reflect the user property
        assertEquals("value1", props.getProperty("property2"));

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager, IsNull.notNullValue());
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertEquals("value1", propertyManager.getProperty("property1"));
        assertEquals("value1", propertyManager.getProperty("property2"));
    }

    private void initIzpackMojo(IzPackNewMojo mojo) throws IllegalAccessException
    {
        File installFile = new File("target/test-classes/helloAndFinish.xml");
        setVariableValueToObject(mojo, "comprFormat", "default");
        setVariableValueToObject(mojo, "installFile", installFile.getAbsolutePath());
        setVariableValueToObject(mojo, "kind", "standard");
        setVariableValueToObject(mojo, "baseDir", new File("target/test-classes/").getAbsolutePath());
        setVariableValueToObject(mojo, "output", "target/sample/izpackResult.jar");
        setVariableValueToObject(mojo, "comprLevel", -1);
        setVariableValueToObject(mojo, "mkdirs", true); // autoboxing
    }

    private void initIzpack5Mojo(IzPackNewMojo mojo) throws IllegalAccessException
    {
        File installFile = new File("target/test-classes/helloAndFinish.xml");
        setVariableValueToObject(mojo, "comprFormat", "default");
        setVariableValueToObject(mojo, "installFile", installFile.getAbsolutePath());
        setVariableValueToObject(mojo, "kind", "standard");
        setVariableValueToObject(mojo, "baseDir", new File("target/test-classes/").getAbsolutePath());
        setVariableValueToObject(mojo, "outputDirectory", new File("target/sample").getAbsoluteFile());
        setVariableValueToObject(mojo, "finalName", "izpackResult");
        setVariableValueToObject(mojo, "comprLevel", -1);
        setVariableValueToObject(mojo, "mkdirs", true); // autoboxing
    }

}
