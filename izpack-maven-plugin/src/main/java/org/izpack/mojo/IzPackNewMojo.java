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
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

/**
 * Mojo for izpack
 *
 * @author Anthonin Bonnefoy
 */
@Mojo( name = "izpack", defaultPhase = LifecyclePhase.PACKAGE, threadSafe = true,
       requiresDependencyResolution = ResolutionScope.TEST)
public class IzPackNewMojo extends AbstractMojo
{
    /**
     * The Maven Session Object
     */
    @Parameter( property = "session", required = true, readonly = true, defaultValue = "${session}" )
    private MavenSession session;
	
    /**
     * The Maven Project Object
     */
    @Parameter( property = "project", required = true, readonly = true, defaultValue = "${project}" )
    private MavenProject project;

    /**
     * Maven ProjectHelper.
     */
    @Component
    private MavenProjectHelper projectHelper;

    /**
     * Format compression. Choices are default (no compression), gzip, bzip2, xz, lzma, deflate
     */
    @Parameter( defaultValue = "default" )
    private String comprFormat;

    /**
     * Kind of installation. Choices are standard (default - file installer) or web
     */
    @Parameter( defaultValue = "standard" )
    private String kind;

    /**
     * Location of the IzPack installation file
     */
    @Parameter( required = true, defaultValue = "${basedir}/src/main/izpack/install.xml" )
    private File installFile;

    /**
     * Base directory of compilation process
     */
    @Parameter( defaultValue = "${project.build.directory}/staging" )
    private File baseDir;

    /**
     * Whether to automatically create parent directories of the output file
     */
    @Parameter( defaultValue = "false" )
    private boolean mkdirs;

    /**
     * Compression level of the installation. Deactivated by default (-1)
     */
    @Parameter( defaultValue = "-1" )
    private int comprLevel;

    /**
     * Whether to automatically include project.url from Maven into
     * IzPack info header
     */
    @Parameter( defaultValue = "false" )
    private boolean autoIncludeUrl;

    /**
     * Whether to automatically include developer list from Maven into
     * IzPack info header
     */
    @Parameter( defaultValue = "false" )
    private boolean autoIncludeDevelopers;

    /**
     * Directory containing the generated JAR.
     */
    @Parameter( defaultValue = "${project.build.directory}", required = true)
    private File outputDirectory;

    /**
     * Name of the compiled installer jar.
     */
    @Parameter
    private String finalName;

    /**
     * When packaging type is not izpack-jar and <i>enableAttachArtifact</i> is
     * set to true, classifier to add to the artifact attached. If not given
     * then it defaults to <i>installer</i>. If the <i>finalName</i> is not
     * given then -<i>classifier</i> is added as a suffix to the module jar name
     * for the installer jar.
     */
    @Parameter
    private String classifier;

    /**
     * When packaging type is not izpack-jar, whether to attach the generated
     * installer jar to the project artifact.
     */
    @Parameter( defaultValue = "true")
    private boolean enableAttachArtifact;

    /**
     * Comma separated list of words, from which any of the word is present in
     * the property name then that property is excluded.
     * By default, the list is empty.
     */
    @Parameter
    private Set<String> excludeProperties;

    private Set<String> trimmedExcludeProperties;

    /**
     * Comma separated list of Maven property names to be included in the installer.
     * By default, the list is empty.
     */
    @Parameter
    private Set<String> includeProperties;

    /**
     * Whether to skip IzPack creation or not. This can be overridden by setting
     * command line parameter skipIzPack. At the command line if the value is
     * not provided or value is other than false then it is assumed to be
     * skipIzPack is set to true.
     * By default, the skipIzPack is false.
     */
    @Parameter( defaultValue = "false")
    private boolean skipIzPack;

    /**
     * A list of key/value pairs to add to the manifest.
     */
    @Parameter
    private Map<String, String> manifestEntries;

    private PropertyManager propertyManager;

    public void execute() throws MojoExecutionException, MojoFailureException
    {
        File jarFile = getJarFile();
        if (isSkipIzPack())
        {
            getLog().info("Skipping IzPack creation.");
            // We need empty file, so that install phase does not have any error
            // for izpack-jar packaging. Also, empty file will be useful for
            // unit tests.
            createEmptyFile(jarFile);
        }
        else
        {
            createIzPack(jarFile);
        }

        if (project.getPackaging().equals("izpack-jar"))
        {
            project.getArtifact().setFile(jarFile);
        }
        else if (enableAttachArtifact)
        {
            projectHelper.attachArtifact(project, "jar", classifier, jarFile);
        }
    }

    private void createIzPack(File jarFile) throws MojoFailureException, MojoExecutionException {
        CompilerData compilerData = initCompilerData(jarFile);
        CompilerContainer compilerContainer = new CompilerContainer();
        compilerContainer.addConfig("installFile", installFile.getPath());
        compilerContainer.getComponent(IzpackProjectInstaller.class);
        compilerContainer.addComponent(CompilerData.class, compilerData);
        compilerContainer.addComponent(Handler.class, createLogHandler());

        CompilerConfig compilerConfig = compilerContainer.getComponent(CompilerConfig.class);

        propertyManager = compilerContainer.getComponent(PropertyManager.class);

        addMavenProperties();

        try
        {
            compilerConfig.executeCompiler();
        }
        catch ( CompilerException e )
        {
            //TODO: This might be enhanced with other exceptions which
            // should be handled like CompilerException
            throw new MojoFailureException( "Failure during compilation process", e );
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Failure", e );
        }
    }

    private boolean isSkipIzPack()
    {
        Properties userProperties = session.getUserProperties();
        boolean skipIzPack = this.skipIzPack;
        String skipIzPackStr = userProperties.getProperty("skipIzPack");
        // if skipIzPack is specified on command line then only we will override
        // or else we will be using what is specified under configuration or the
        // default value
        if (skipIzPackStr != null)
        {
            skipIzPack = Boolean.parseBoolean(skipIzPackStr);
        }
        return skipIzPack;
    }

    private static void createEmptyFile(File jarFile) throws MojoExecutionException {
        try
        {
            jarFile.createNewFile();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Failure", e);
        }
    }

    private File getJarFile()
    {
        if (classifier == null || classifier.trim().isEmpty())
        {
            classifier = "installer";
        }

        String installerFileName = finalName;
        if (finalName == null)
        {
            installerFileName = project.getBuild().getFinalName();
            if (!project.getPackaging().equals("izpack-jar"))
            {
                installerFileName += "-" + classifier;
            }
        }
        return new File(outputDirectory, installerFileName + ".jar");
    }

    private void addMavenProperties() {
        if (includeProperties == null)
        {
            trimExcludeProperties();
            List<String> includedProperties = initMavenProperties(project.getProperties().stringPropertyNames());
            if (!includedProperties.isEmpty())
            {
                includedProperties.sort(String.CASE_INSENSITIVE_ORDER);
                getLog().warn("You have not provided list of Maven properties to be included in the installer." +
                        " Some of the sensitive maven properties may get included in the list." +
                        " It is recommended to use 'includedProperties' to avoid this." +
                        " Following Maven properties got included in the installer:\n" +
                        String.join(", ", includedProperties));
            }
        }
        else
        {
            List<String> includedProperties = initMavenProperties(trimIncludeProperties());
            if (!includedProperties.isEmpty())
            {
                includedProperties.sort(String.CASE_INSENSITIVE_ORDER);
                getLog().info("Following Maven properties got included in the installer:\n" +
                        String.join(", ", includedProperties));
            }
        }
    }

    private List<String> initMavenProperties(Set<String> propertyNames)
    {
        Properties properties = project.getProperties();
        Properties userProps  = session.getUserProperties();
        List<String> includedProperties = new ArrayList<>();
        for (String propertyName : propertyNames)
        {
            if (containsExcludedProperty(propertyName))
            {
                getLog().warn("Excluding Maven property: " + propertyName);
                continue;
            }
            // TODO: should all user properties be provided as property?
            // Intentionally user properties are searched for properties defined in pom.xml only
            // see https://izpack.atlassian.net/browse/IZPACK-1402 for discussion
            String value = userProps.getProperty(propertyName);
            if (value == null)
            {
                value = properties.getProperty(propertyName);
            }
            String existingValue = propertyManager.getProperty(propertyName);
            if (existingValue != null && existingValue.equals(value))
            {
                includedProperties.add(propertyName + "=" + value);
                getLog().debug("Maven property exists: " + propertyName + "=" + value);
            }
            else if (value == null)
            {
                getLog().warn("Specified property: " + propertyName + " does not exist");
            }
            else
            {
                if (propertyManager.addProperty(propertyName, value))
                {
                    includedProperties.add(propertyName + "=" + value);
                    getLog().debug("Maven property added: " + propertyName + "=" + value);
                }
                else
                {
                    includedProperties.add(propertyName + "=" + existingValue);
                    getLog().warn("Property " + propertyName + "=" + existingValue +
                            " could not be overridden with maven property " + propertyName + "=" + value);
                }
            }
        }
        return includedProperties;
    }

    private CompilerData initCompilerData(File jarFile)
    {
        Info info = new Info();

        if (autoIncludeDevelopers)
        {
            if (project.getDevelopers() != null)
            {
                for (Developer dev : project.getDevelopers())
                {
                    info.addAuthor(new Info.Author(dev.getName(), dev.getEmail()));
                }
            }
        }
        if (autoIncludeUrl)
        {
            info.setAppURL(project.getUrl());
        }
        return new CompilerData(comprFormat, kind, installFile.getPath(), null, baseDir.getPath(), jarFile.getPath(),
                                mkdirs, comprLevel, info, manifestEntries);
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

    private void trimExcludeProperties() {
        if (excludeProperties != null)
        {
            trimmedExcludeProperties = new HashSet<>();
            for (String word : excludeProperties)
            {
                trimmedExcludeProperties.add(word.trim().toLowerCase());
            }
        }
    }

    private boolean containsExcludedProperty(String property) {
        if (trimmedExcludeProperties != null)
        {
            for (String word : trimmedExcludeProperties)
            {
                if (property.toLowerCase().contains(word))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private Set<String> trimIncludeProperties()
    {
        Set<String> trimmedIncludeProperties = new HashSet<>();
        if (includeProperties != null)
        {
            for (String word : includeProperties) {
                trimmedIncludeProperties.add(word.trim());
            }
        }
        return trimmedIncludeProperties;
    }
}
