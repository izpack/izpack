/*
 * IzPack - Copyright 2001-2016 The IzPack project team.
 * All Rights Reserved.
 *
 * http://izpack.org/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.izpack.mojo;

import com.izforge.izpack.api.data.Info;
import com.izforge.izpack.api.data.binding.IzpackProjectInstaller;
import com.izforge.izpack.api.exception.CompilerException;
import com.izforge.izpack.compiler.CompilerConfig;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.data.PropertyManager;
import com.izforge.izpack.compiler.logging.MavenStyleLogFormatter;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Developer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

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
     * The Maven Session Object
     *
     * @parameter property="session" default-value="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;

    /**
     * The Maven Project Object
     *
     * @parameter property="project" default-value="${project}"
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
     * @deprecated Use outputDirectory, finalName and optional classifier
     * instead
     */
    private String output;

    /**
     * Whether to automatically create parent directories of the output file
     *
     * @parameter default-value="false"
     */
    private boolean mkdirs;

    /**
     * Specifies that the XML parser will validate each descriptor using W3C XML
     * Schema as they are parsed. By default the value of this is set to false.
     *
     * @parameter default-value="true"
     */
    private boolean validating;

    /**
     * Compression level of the installation. Deactivated by default (-1)
     *
     * @parameter default-value="-1"
     */
    private int comprLevel;

    /**
     * Whether to automatically include project.url from Maven into IzPack info
     * header
     *
     * @parameter default-value="false"
     */
    private boolean autoIncludeUrl;

    /**
     * Whether to automatically include developer list from Maven into IzPack
     * info header
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
     * @parameter alias="jarName" property="jar.finalName"
     * default-value="${project.build.finalName}"
     * @required
     */
    private String finalName;

    /**
     * Classifier to add to the artifact generated. If given, the artifact is
     * attachable. Furthermore, the output file name gets -<i>classifier</i> as
     * suffix. If this is not given,it will merely be written to the output
     * directory according to the finalName.
     *
     * @parameter
     */
    private String classifier;

    /**
     * Whether to attach the generated installer jar to the project as artifact
     * if a classifier is specified. This has no effect if no classifier was
     * specified.
     *
     * @parameter default-value="true"
     */
    private boolean enableAttachArtifact;

    /**
     * Whether to override the artifact file by the generated installer jar, if
     * no classifier is specified. This will set the artifact file to the given
     * name based on
     * <i>outputDirectory</i> + <i>finalName</i> or on <i>output</i>. This has
     * no effect if a classifier was specified.
     *
     * @parameter default-value="false"
     */
    private boolean enableOverrideArtifact;

    private PropertyManager propertyManager;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File jarFile = getJarFile();

        CompilerData compilerData = initCompilerData(jarFile);
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.addConfig("installFile", installFile);
        compilerContainer.getComponent(IzpackProjectInstaller.class);
        compilerContainer.addComponent(CompilerData.class, compilerData);
        compilerContainer.addComponent(Handler.class, createLogHandler());

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);

        propertyManager = compilerContainer.getComponent(PropertyManager.class);
        initMavenProperties(propertyManager);

        try
        {
            compilerConfig.executeCompiler();
        }
        catch (CompilerException e)
        {
            //TODO: This might be enhanced with other exceptions which
            // should be handled like CompilerExecptions
            throw new MojoFailureException("Failure during compilation process", e);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Failure", e);
        }

        if (classifier != null && !classifier.isEmpty())
        {
            if (enableAttachArtifact)
            {
                projectHelper.attachArtifact(project, "jar", classifier, jarFile);
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
        //TODO - project is annotated as @required, so the check project!=null should be useless!?!
        if (project != null)
        {
            Properties properties = project.getProperties();
            Properties userProps = session.getUserProperties();
            for (String propertyName : properties.stringPropertyNames())
            {
                String value;
                // TODO: should all user properties be provided as property?
                // Intentionally user properties are searched for properties defined in pom.xml only
                // see https://izpack.atlassian.net/browse/IZPACK-1402 for discussion
                if (userProps.containsKey(propertyName))
                {
                    value = userProps.getProperty(propertyName);
                }
                else
                {
                    value = properties.getProperty(propertyName);
                }
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
                mkdirs, validating, comprLevel, info);
    }

    private Handler createLogHandler()
    {
        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new MavenStyleLogFormatter());
        Log log = getLog();
        Level level = Level.OFF;
        if (log.isDebugEnabled())
        {
            level = Level.FINE;
        }
        else if (log.isInfoEnabled())
        {
            level = Level.INFO;
        }
        else if (log.isWarnEnabled())
        {
            level = Level.WARNING;
        }
        else if (log.isErrorEnabled())
        {
            level = Level.SEVERE;
        }
        consoleHandler.setLevel(level);
        return consoleHandler;
    }

}
