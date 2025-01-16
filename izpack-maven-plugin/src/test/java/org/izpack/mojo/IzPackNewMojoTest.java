package org.izpack.mojo;

import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.matcher.ZipMatcher;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.hamcrest.core.Is;
import org.hamcrest.core.IsCollectionContaining;
import org.hamcrest.core.IsNull;
import org.junit.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;

import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Test of new IzPack mojo
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackNewMojoTest extends AbstractMojoTestCase
{

    /**
     * The Maven Project.
     */
    @Component
    protected MavenProject project;

    @Test
    public void testExecute() throws Exception
    {
        File file = new File( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        mojo.execute();

        assertThat( file.exists(), Is.is( true ) );
        JarFile jar = new JarFile( file );
        assertThat( (ZipFile)jar, ZipMatcher.isZipMatching( IsCollectionContaining.hasItems(
                "com/izforge/izpack/core/container/AbstractContainer.class",
                "com/izforge/izpack/uninstaller/Destroyer.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class")));
    }

    @Test
    public void testExecuteNoFinalNameForIzPackPackaging() throws Exception
    {
        String classifier = "install";
        File file = new File( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);
        project.setPackaging("izpack-jar");

        // In this case the classifier should be set.
        setVariableValueToObject( mojo, "classifier", classifier );

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertEquals( classifier, getVariableValueFromObject( mojo, "classifier" ) );

        // Verify the generated file exists.
        assertThat( file.exists(), Is.is( true ) );
    }

    @Test
    public void testExecuteNoFinalNameWithClassifier() throws Exception
    {
        String classifier = "install";
        File file = new File( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT-" + classifier + ".jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        // In this case the classifier should be set.
        setVariableValueToObject( mojo, "classifier", classifier );

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertEquals( classifier, getVariableValueFromObject( mojo, "classifier" ) );

        // Verify the generated file exists.
        assertThat( file.exists(), Is.is( true ) );
    }

    @Test
    public void testExecuteNoFinalNameWithoutClassifier() throws Exception
    {
        File file = new File( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT-installer.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertEquals( "installer", getVariableValueFromObject( mojo, "classifier" ) );

        // Verify the generated file exists.
        assertThat( file.exists(), Is.is( true ) );
    }

    @Test
    public void testFixIZPACK_1400() throws Exception
    {
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("property1", "value1");       // simulates "-Dproperty1=value1" on mvn commandline

        IzPackNewMojo mojo = setupMojo("pom-izpack-1400.xml", userProps);

        // Execute the mojo.
        mojo.execute();

        // first verify the default behavior of maven
        Properties props = project.getProperties();
        // project.Properties do not reflect the user properties, so property1 reflects pom.xml
        assertEquals("default", props.getProperty("property1"));
        // but computed properties do reflect the user property
        assertEquals("value1",  props.getProperty("property2"));

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager, IsNull.notNullValue() );
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertEquals("value1" , propertyManager.getProperty("property1"));
        assertEquals("value1" , propertyManager.getProperty("property2"));
    }

    @Test
    public void testFixIZPACK_1655_withoutExclusion() throws Exception
    {
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("property1", "value1"); // simulates "-Dproperty1=value1" on mvn commandline
        userProps.setProperty("sensitive.data", "Through command line"); // simulates "-Dsensitive.data=Through command line" on mvn commandline

        IzPackNewMojo mojo = setupMojo("pom-izpack-1655.xml", userProps);

        // Execute the mojo.
        mojo.execute();

        // first verify the default behavior of maven
        Properties props = project.getProperties();
        // project.Properties do not reflect the user properties, so property1 reflects pom.xml
        assertEquals("default", props.getProperty("property1"));

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager, IsNull.notNullValue() );
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertEquals("value1" , propertyManager.getProperty("property1"));
        assertEquals("NoOneKnows" , propertyManager.getProperty("password"));
        assertEquals("Secret" , propertyManager.getProperty("passphrase"));
        assertEquals("Through command line" , propertyManager.getProperty("sensitive.data"));
    }

    public void testFixIZPACK_1655_withExclusion() throws Exception
    {
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("property1", "value1"); // simulates "-Dproperty1=value1" on mvn commandline
        userProps.setProperty("sensitive.data", "Through command line"); // simulates "-Dsensitive.data=Through command line" on mvn commandline

        IzPackNewMojo mojo = setupMojo("pom-izpack-1655.xml", userProps);
        setVariableValueToObject(mojo, "excludeProperties", new HashSet<>(Arrays.asList("pass", "sensitive")));

        // Execute the mojo.
        mojo.execute();

        // first verify the default behavior of maven
        Properties props = project.getProperties();
        // project.Properties do not reflect the user properties, so property1 reflects pom.xml
        assertEquals("default", props.getProperty("property1"));

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager, IsNull.notNullValue() );
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertEquals("value1" , propertyManager.getProperty("property1"));
        assertNull(propertyManager.getProperty("password")); // property name with pass word in it is excluded
        assertNull(propertyManager.getProperty("passphrase")); // property name with pass word in it  is excluded
        assertNull(propertyManager.getProperty("sensitive.data")); // property name with sensitive word in it is excluded
    }

    public void testFixIZPACK_1402() throws Exception
    {
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("property1", "value1"); // simulates "-Dproperty1=value1" on mvn commandline
        userProps.setProperty("sensitive.data", "Through command line"); // simulates "-Dsensitive.data=Through command line" on mvn commandline

        IzPackNewMojo mojo = setupMojo("pom-izpack-1655.xml", userProps);
        setVariableValueToObject(mojo, "includeProperties", new HashSet<>(Arrays.asList("property1", "staging.dir")));

        // Execute the mojo.
        mojo.execute();

        // first verify the default behavior of maven
        Properties props = project.getProperties();
        // project.Properties do not reflect the user properties, so property1 reflects pom.xml
        assertEquals("default", props.getProperty("property1"));

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager, IsNull.notNullValue() );
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertEquals("value1" , propertyManager.getProperty("property1")); // property1 should get added
        assertThat(propertyManager.getProperty("staging.dir"), IsNull.notNullValue()); //  staging.dir should get added
        assertNull(propertyManager.getProperty("password")); // password is not in the includeProperties, should not be added
        assertNull(propertyManager.getProperty("passphrase")); // passphrase is not in the includeProperties, should not be added
        assertNull(propertyManager.getProperty("sensitive.data")); // sensitive.data is not in the includeProperties, should not be added
    }

    public void testFixIZPACK_1525_SkipIzPackInConfiguration() throws Exception
    {
        File file = new File( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );
        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        setVariableValueToObject(mojo, "skipIzPack", true);
        mojo.execute();

        assertThat( file.exists(), Is.is( true ) );
        assertEquals(0, file.length());
    }

    public void testFixIZPACK_1525_SkipIzPackAtCommandLine() throws Exception
    {
        File file = new File( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("skipIzPack", "true"); // simulates "-skipIzPack=true" on mvn commandline

        IzPackNewMojo mojo = setupMojo("basic-pom.xml", userProps);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        mojo.execute();

        assertThat( file.exists(), Is.is( true ) );
        assertEquals(0, file.length());
    }


    public void testFixIZPACK_1525_SkipIzPackAtCommandLineIzPackNoFinalName() throws Exception
    {
        File file = new File( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT-installer.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("skipIzPack", "true"); // simulates "-skipIzPack=true" on mvn commandline

        IzPackNewMojo mojo = setupMojo("basic-pom.xml", userProps);

        mojo.execute();

        assertThat( file.exists(), Is.is( true ) );
        assertEquals(0, file.length());
    }

    @Test
    public void testManifestEntries() throws Exception
    {
        File file = new File( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        file.delete();
        assertThat( file.exists(), Is.is( false ) );

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        Map<String, String> manifestEntries = new HashMap<>();
        manifestEntries.put("Main-Class", "MyClass");
        manifestEntries.put("ManifestEntry1", "ManifestValue1");
        manifestEntries.put("ManifestEntry2", "ManifestValue2");
        setVariableValueToObject(mojo, "manifestEntries", manifestEntries);

        // Execute the mojo.
        mojo.execute();

        assertThat( file.exists(), Is.is( true ) );

        try (FileSystem zipFileSystem = FileSystems.newFileSystem(URI.create("jar:" + file.toURI()), Collections.emptyMap()))
        {
            try (InputStream manifestInputStream = Files.newInputStream(zipFileSystem.getPath("META-INF/MANIFEST.MF")))
            {
                Manifest manifest = new Manifest(manifestInputStream);
                final Attributes mainAttributes = manifest.getMainAttributes();
                // Manifest-Version, Created-By, and Main-Class should not get overwritten, we just verify Main-Class
                assertEquals("com.izforge.izpack.installer.bootstrap.Installer", mainAttributes.get(new Attributes.Name("Main-Class")));
                assertEquals("ManifestValue1", mainAttributes.get(new Attributes.Name("ManifestEntry1")));
                assertEquals("ManifestValue2", mainAttributes.get(new Attributes.Name("ManifestEntry2")));
            }
            try (InputStream manifestInputStream = Files.newInputStream(zipFileSystem.getPath("uninstaller-META-INF/MANIFEST.MF")))
            {
                Manifest manifest = new Manifest(manifestInputStream);
                final Attributes mainAttributes = manifest.getMainAttributes();
                // Manifest-Version, Created-By, and Main-Class should not get overwritten, we just verify Main-Class
                assertEquals("com.izforge.izpack.uninstaller.Uninstaller", mainAttributes.get(new Attributes.Name("Main-Class")));
                assertEquals("ManifestValue1", mainAttributes.get(new Attributes.Name("ManifestEntry1")));
                assertEquals("ManifestValue2", mainAttributes.get(new Attributes.Name("ManifestEntry2")));
            }
        }
    }

    private IzPackNewMojo setupMojo(String testPom, Properties userProps) throws Exception
    {
        File testFile = new File(Thread.currentThread().getContextClassLoader().getResource(testPom).toURI());

        IzPackNewMojo mojo = (IzPackNewMojo) lookupMojo("izpack", testFile);
        assertThat(mojo, IsNull.notNullValue());
        initIzpack5Mojo(mojo);

        MavenSession session = new MavenSession(getContainer(),       // PlexusContainer container
                null,       // Settings settings
                null,       // ArtifactRepository localRepository
                null,       // EventDispatcher eventDispatcher
                null,       // ReactorManager reactorManager
                null,       // List goals
                null,       // String executionRootDir
                null,       // Properties executionProperties
                userProps,  // Properties userProperties
                null        // Date startTime
        );
        setVariableValueToObject(mojo, "session", session);

        MavenExecutionRequest mavenRequest = session.getRequest();
        ProjectBuildingRequest projectBuildingRequest = mavenRequest.getProjectBuildingRequest();
        RepositorySystemSession repositorySystemSession = new DefaultRepositorySystemSession();
        projectBuildingRequest.setRepositorySession(repositorySystemSession);
        projectBuildingRequest.setUserProperties(userProps);
        ProjectBuilder projectBuilder = lookup(ProjectBuilder.class);
        ProjectBuildingResult result = projectBuilder.build(testFile, projectBuildingRequest);
        project = result.getProject();

        setVariableValueToObject(mojo, "project", project);

        return mojo;
    }

    private void initIzpack5Mojo( IzPackNewMojo mojo ) throws IllegalAccessException
    {
        File installFile = new File( "target/test-classes/helloAndFinish.xml" );
        setVariableValueToObject( mojo, "comprFormat", "default" );
        setVariableValueToObject( mojo, "installFile", installFile );
        setVariableValueToObject( mojo, "kind", "standard" );
        setVariableValueToObject( mojo, "baseDir", new File( "target/test-classes/" ) );
        setVariableValueToObject( mojo, "outputDirectory", new File( "target/sample" ).getAbsoluteFile() );
        setVariableValueToObject( mojo, "comprLevel", -1 );
        setVariableValueToObject( mojo, "mkdirs", true ); // autoboxing
    }

}
