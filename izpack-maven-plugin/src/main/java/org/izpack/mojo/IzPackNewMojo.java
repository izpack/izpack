package org.izpack.mojo;

import java.io.File;
import java.util.List;
import java.util.Properties;

import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;

/**
 * Mojo for izpack
 *
 * @author Anthonin Bonnefoy
 * @goal izpack
 * @phase package
 * @requiresProject
 * @threadSafe
 * @requiresDependencyResolution test
 */
public class IzPackNewMojo extends AbstractMojo
{
    /**
     * The Maven Project Object
     *
     * @parameter expression="${project}" default-value="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * Maven ProjectHelper.
     *
     * @component
     * @readonly
     */
    private MavenProjectHelper projectHelper;

    /**
     * Format compression. Choices are bzip2, default
     *
     * @parameter default-value="default"
     */
    private String comprFormat;

    /**
     * Kind of installation. Choices are standard (default) or web
     *
     * @parameter default-value="standard"
     */
    private String kind;

    /**
     * Location of the IzPack installation file
     *
     * @parameter default-value="${basedir}/src/main/izpack/install.xml"
     */
    private String installFile;

    /**
     * Base directory of compilation process
     *
     * @parameter default-value="${project.build.directory}/staging"
     */
    private String baseDir;

    /**
     * Output where compilation result will be situate
     *
     * @parameter
     * @deprecated Use outputDirectory, finalName and optional classifier instead
     */
    private String output;

    /**
     * Whether to automatically create parent directories of the output file
     *
     * @parameter default-value="false"
     */
    private boolean mkdirs;

    /**
     * Compression level of the installation. Desactivated by default (-1)
     *
     * @parameter default-value="-1"
     */
    private int comprLevel;

    /**
     * Whether to automatically include project.url from Maven into
     * IzPack info header
     *
     * @parameter default-value="false"
     */
    private boolean autoIncludeUrl;

    /**
     * Whether to automatically include developer list from Maven into
     * IzPack info header
     *
     * @parameter default-value="false"
     */
    private boolean autoIncludeDevelopers;

    /**
     * Directory containing the generated JAR.
     *
     * @parameter default-value="${project.build.directory}"
     * @required
     */
    private File outputDirectory;

    /**
     * Name of the generated JAR.
     *
     * @parameter alias="jarName" expression="${jar.finalName}" default-value="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * Classifier to add to the artifact generated. If given, the artifact is attachable.
     * Furthermore, the output file name gets -<i>classifier</i> as suffix.
     * If this is not given,it will merely be written to the output directory
     * according to the finalName.
     *
     * @parameter
     */
    private String classifier;

    /**
     * Whether to attach the generated installer jar to the project
     * as artifact if a classifier is specified.
     * This has no effect if no classifier was specified.
     *
     * @parameter default-value="true"
     */
    private boolean enableAttachArtifact;

    /**
     * Whether to override the artifact file by the generated installer jar,
     * if no classifier is specified.
     * This will set the artifact file to the given name based on
     * <i>outputDirectory</i> + <i>finalName</i> or on <i>output</i>.
     * This has no effect if a classifier was specified.
     *
     * @parameter default-value="false"
     */
    private boolean enableOverrideArtifact;


    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File jarFile = getJarFile();

        CompilerData compilerData = initCompilerData(jarFile);
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.addConfig("installFile", installFile);
        compilerContainer.getComponent(IzpackProjectInstaller.class);
        compilerContainer.addComponent(CompilerData.class, compilerData);

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);

        PropertyManager propertyManager = compilerContainer.getComponent(PropertyManager.class);
        initMavenProperties(propertyManager);

        try
        {
            compilerConfig.executeCompiler();
        }
        catch (Exception e)
        {
            throw new AssertionError(e);
        }

        if (classifier != null && !classifier.isEmpty())
        {
            if (enableAttachArtifact)
            {
                projectHelper.attachArtifact(project, getType(), classifier, jarFile);
            }
        }
        else
        {
            if (enableOverrideArtifact)
            {
                project.getArtifact().setFile(jarFile);
            }
        }
    }

    /**
     * @return type of the generated artifact - is "jar"
     */
    protected static final String getType()
    {
        return "jar";
    }

    private File getJarFile()
    {
        File file;

        if (output != null)
        {
            file = new File(output);
        }
        else
        {
            String localClassifier = classifier;
            if (classifier == null || classifier.trim().isEmpty())
            {
                localClassifier = "";
            }
            else if (!classifier.startsWith("-"))
            {
                localClassifier = "-" + classifier;
            }
            file = new File(outputDirectory, finalName + localClassifier + ".jar");
        }

        return file;
    }

    private void initMavenProperties(PropertyManager propertyManager)
    {
        if (project != null)
        {
            Properties properties = project.getProperties();
            for (String propertyName : properties.stringPropertyNames())
            {
                String value = properties.getProperty(propertyName);
                if (propertyManager.addProperty(propertyName, value))
                {
                    getLog().debug("Maven property added: " + propertyName + "=" + value);
                }
                else
                {
                    getLog().warn("Maven property " + propertyName + " could not be overridden");
                }
            }
        }
    }

    private CompilerData initCompilerData(File jarFile)
    {
        Info info = new Info();

        if (project != null)
        {
            if (autoIncludeDevelopers)
            {
                if (project.getDevelopers() != null)
                {
                    for (Developer dev : (List<Developer>) project.getDevelopers())
                    {
                        info.addAuthor(new Info.Author(dev.getName(), dev.getEmail()));
                    }
                }
            }
            if (autoIncludeUrl)
            {
                info.setAppURL(project.getUrl());
            }
        }
        return new CompilerData(comprFormat, kind, installFile, null, baseDir, jarFile.getPath(),
                                mkdirs, comprLevel, info);
    }

}
