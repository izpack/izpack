package org.izpack.mojo;

import static com.izforge.izpack.matcher.ZipMatcher.getFileNameListFromZip;
import static java.lang.Thread.currentThread;
import static java.net.URI.create;
import static java.nio.file.FileSystems.newFileSystem;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newInputStream;
import static org.apache.maven.api.plugin.testing.MojoExtension.getVariableValueFromObject;
import static org.apache.maven.api.plugin.testing.MojoExtension.setVariableValueToObject;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.compiler.data.PropertyManager;
import java.io.File;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystemSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/**
 * Test of new IzPack mojo
 *
 * @author Anthonin Bonnefoy
 */
public class IzPackNewMojoTest
{
    @RegisterExtension
    final MavenMojoFixture fixture = new MavenMojoFixture();

    /**
     * The Maven Project.
     */
    @Component
    protected MavenProject project;

    @Test
    public void testExecute() throws Exception
    {
        Path file = Path.of( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        mojo.execute();

        assertThat(file).exists();
        JarFile jar = new JarFile(file.toFile());
        assertThat(getFileNameListFromZip((ZipFile)jar)).contains("com/izforge/izpack/core/container/AbstractContainer.class",
                "com/izforge/izpack/uninstaller/Destroyer.class",
                "com/izforge/izpack/panels/checkedhello/CheckedHelloPanel.class");
    }

    @Test
    public void testExecuteNoFinalNameForIzPackPackaging() throws Exception
    {
        String classifier = "install";
        Path file = Path.of( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);
        project.setPackaging("izpack-jar");

        // In this case the classifier should be set.
        setVariableValueToObject( mojo, "classifier", classifier );

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertThat((Object) getVariableValueFromObject( mojo, "classifier" )).isEqualTo(classifier);

        // Verify the generated file exists.
        assertThat(file).exists();
    }

    @Test
    public void testExecuteNoFinalNameWithClassifier() throws Exception
    {
        String classifier = "install";
        Path file = Path.of( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT-" + classifier + ".jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        // In this case the classifier should be set.
        setVariableValueToObject( mojo, "classifier", classifier );

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertThat((Object) getVariableValueFromObject( mojo, "classifier" )).isEqualTo(classifier);

        // Verify the generated file exists.
        assertThat(file).exists();
    }

    @Test
    public void testExecuteNoFinalNameWithoutClassifier() throws Exception
    {
        Path file = Path.of( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT-installer.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();

        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        // Execute the mojo.
        mojo.execute();

        // Ensure that the classifier value is still set correctly.
        assertThat((Object) getVariableValueFromObject( mojo, "classifier" )).isEqualTo("installer");

        // Verify the generated file exists.
        assertThat(file).exists();
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
        assertThat(props.getProperty("property1")).isEqualTo("default");
        // but computed properties do reflect the user property
        assertThat(props.getProperty("property2")).isEqualTo("value1");

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager).isNotNull();
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertThat(propertyManager.getProperty("property1")).isEqualTo("value1");
        assertThat(propertyManager.getProperty("property2")).isEqualTo("value1");
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
        assertThat(props.getProperty("property1")).isEqualTo("default");

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager).isNotNull();
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertThat(propertyManager.getProperty("property1")).isEqualTo("value1");
        assertThat(propertyManager.getProperty("password")).isEqualTo("NoOneKnows");
        assertThat(propertyManager.getProperty("passphrase")).isEqualTo("Secret");
        assertThat(propertyManager.getProperty("sensitive.data")).isEqualTo("Through command line");
    }

    @Test
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
        assertThat(props.getProperty("property1")).isEqualTo("default");

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager).isNotNull();
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertThat(propertyManager.getProperty("property1")).isEqualTo("value1");
        assertThat(propertyManager.getProperty("password")).isNull(); // property name with pass word in it is excluded
        assertThat(propertyManager.getProperty("passphrase")).isNull(); // property name with pass word in it  is excluded
        assertThat(propertyManager.getProperty("sensitive.data")).isNull(); // property name with sensitive word in it is excluded
    }

    @Test
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
        assertThat(props.getProperty("property1")).isEqualTo("default");

        // verify the behavior of IzPack Maven Plugin
        PropertyManager propertyManager = (PropertyManager) getVariableValueFromObject(mojo, "propertyManager");
        assertThat(propertyManager).isNotNull();
        // The IzPackMaven plugin should honor the user property set with "-Dproperty1=value1"
        assertThat(propertyManager.getProperty("property1")).isEqualTo("value1"); // property1 should get added
        assertThat(propertyManager.getProperty("staging.dir")).isNotNull(); //  staging.dir should get added
        assertThat(propertyManager.getProperty("password")).isNull(); // password is not in the includeProperties, should not be added
        assertThat(propertyManager.getProperty("passphrase")).isNull(); // passphrase is not in the includeProperties, should not be added
        assertThat(propertyManager.getProperty("sensitive.data")).isNull(); // sensitive.data is not in the includeProperties, should not be added
    }

    @Test
    public void testFixIZPACK_1525_SkipIzPackInConfiguration() throws Exception
    {
        Path file = Path.of( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();
        // Create and configure the mojo.
        IzPackNewMojo mojo = setupMojo("basic-pom.xml", null);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        setVariableValueToObject(mojo, "skipIzPack", true);
        mojo.execute();

        assertThat(file).exists();
        assertThat(file).hasSize(0);
    }

    @Test
    public void testFixIZPACK_1525_SkipIzPackAtCommandLine() throws Exception
    {
        Path file = Path.of( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("skipIzPack", "true"); // simulates "-skipIzPack=true" on mvn commandline

        IzPackNewMojo mojo = setupMojo("basic-pom.xml", userProps);

        setVariableValueToObject(mojo, "finalName", "izpackResult");
        mojo.execute();

        assertThat(file).exists();
        assertThat(file).hasSize(0);
    }


    @Test
    public void testFixIZPACK_1525_SkipIzPackAtCommandLineIzPackNoFinalName() throws Exception
    {
        Path file = Path.of( "target/sample/izpack-dist-test-harness-5.0.0-SNAPSHOT-installer.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();
        // Create and configure the mojo.
        Properties userProps = new Properties();
        userProps.setProperty("skipIzPack", "true"); // simulates "-skipIzPack=true" on mvn commandline

        IzPackNewMojo mojo = setupMojo("basic-pom.xml", userProps);

        mojo.execute();

        assertThat(file).exists();
        assertThat(file).hasSize(0);
    }

    @Test
    public void testManifestEntries() throws Exception
    {
        Path file = Path.of( "target/sample/izpackResult.jar" );

        // Cleanup from any previous runs.
        deleteIfExists(file);
        assertThat(file).doesNotExist();

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

        assertThat(file).exists();

        try (FileSystem zipFileSystem = newFileSystem(create("jar:" + file.toUri()), Collections.emptyMap()))
        {
            try (InputStream manifestInputStream = newInputStream(zipFileSystem.getPath("META-INF/MANIFEST.MF")))
            {
                Manifest manifest = new Manifest(manifestInputStream);
                final Attributes mainAttributes = manifest.getMainAttributes();
                // Manifest-Version, Created-By, and Main-Class should not get overwritten, we just verify Main-Class
                assertThat(mainAttributes.get(new Attributes.Name("Main-Class"))).isEqualTo("com.izforge.izpack.installer.bootstrap.Installer");
                assertThat(mainAttributes.get(new Attributes.Name("ManifestEntry1"))).isEqualTo("ManifestValue1");
                assertThat(mainAttributes.get(new Attributes.Name("ManifestEntry2"))).isEqualTo("ManifestValue2");
            }
            try (InputStream manifestInputStream = newInputStream(zipFileSystem.getPath("uninstaller-META-INF/MANIFEST.MF")))
            {
                Manifest manifest = new Manifest(manifestInputStream);
                final Attributes mainAttributes = manifest.getMainAttributes();
                // Manifest-Version, Created-By, and Main-Class should not get overwritten, we just verify Main-Class
                assertThat(mainAttributes.get(new Attributes.Name("Main-Class"))).isEqualTo("com.izforge.izpack.uninstaller.Uninstaller");
                assertThat(mainAttributes.get(new Attributes.Name("ManifestEntry1"))).isEqualTo("ManifestValue1");
                assertThat(mainAttributes.get(new Attributes.Name("ManifestEntry2"))).isEqualTo("ManifestValue2");
            }
        }
    }

    private IzPackNewMojo setupMojo(String testPom, Properties userProps) throws Exception
    {
        File testFile = new File(currentThread().getContextClassLoader().getResource(testPom).toURI());

        IzPackNewMojo mojo = (IzPackNewMojo) fixture.lookupMojo("izpack", testFile);
        assertThat(mojo).isNotNull();
        initIzpack5Mojo(mojo);

        MavenSession session = new MavenSession(fixture.getContainer(),       // PlexusContainer container
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
        ProjectBuilder projectBuilder = fixture.getContainer().lookup(ProjectBuilder.class);
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
