/*
 * IzPack - Copyright 2001-2026 The IzPack project team.
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

import static java.lang.Thread.currentThread;
import static org.apache.maven.api.plugin.testing.MojoExtension.extractPluginConfiguration;
import static org.codehaus.plexus.util.xml.Xpp3DomBuilder.build;

import java.io.File;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import java.util.List;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.descriptor.PluginDescriptorBuilder;
import org.apache.maven.plugin.testing.ResolverExpressionEvaluatorStub;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.classworlds.ClassWorld;
import org.codehaus.plexus.component.configurator.ComponentConfigurator;
import org.codehaus.plexus.configuration.xml.XmlPlexusConfiguration;
import org.codehaus.plexus.util.InterpolationFilterReader;
import org.codehaus.plexus.util.xml.XmlStreamReader;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

/** Jupiter-owned Maven container; keeps real project-building semantics of the mojo tests. */
public class MavenMojoFixture implements BeforeEachCallback, AfterEachCallback
{
    private DefaultPlexusContainer container;
    private PluginDescriptor descriptor;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception
    {
        try
        {
            container = new DefaultPlexusContainer(new DefaultContainerConfiguration()
                    .setClassWorld(new ClassWorld("plexus.core", currentThread().getContextClassLoader()))
                    .setClassPathScanning(PlexusConstants.SCANNING_INDEX).setAutoWiring(true).setName("maven"));
            try (InputStream stream = context.getRequiredTestClass().getResourceAsStream("/META-INF/maven/plugin.xml");
                 Reader reader = new XmlStreamReader(stream);
                 Reader interpolated = new InterpolationFilterReader(reader, container.getContext().getContextData(), "${", "}"))
            {
                descriptor = new PluginDescriptorBuilder().build(interpolated);
            }
            DefaultArtifact artifact = new DefaultArtifact(descriptor.getGroupId(), descriptor.getArtifactId(),
                    descriptor.getVersion(), null, "jar", null, new DefaultArtifactHandler("jar"));
            artifact.setFile(Path.of("target/classes").toFile().getCanonicalFile());
            descriptor.setPluginArtifact(artifact);
            descriptor.setArtifacts(List.of(artifact));
            descriptor.getComponents().forEach(container::addComponentDescriptor);
        }
        catch (Exception failure)
        {
            close();
            throw failure;
        }
    }

    public PlexusContainer getContainer()
    {
        return container;
    }

    public Mojo lookupMojo(String goal, File pom) throws Exception
    {
        Xpp3Dom document;
        try (Reader reader = new XmlStreamReader(pom))
        {
            document = build(reader);
        }
        Xpp3Dom configuration = extractPluginConfiguration(descriptor.getArtifactId(), document);
        Mojo mojo = container.lookup(Mojo.class, descriptor.getGroupId() + ":" + descriptor.getArtifactId()
                + ":" + descriptor.getVersion() + ":" + goal);
        container.lookup(ComponentConfigurator.class, "basic").configureComponent(mojo,
                new XmlPlexusConfiguration(configuration), new ResolverExpressionEvaluatorStub(), container.getContainerRealm());
        return mojo;
    }

    @Override
    public void afterEach(ExtensionContext context)
    {
        close();
    }

    private void close()
    {
        if (container != null)
        {
            container.dispose();
            container = null;
        }
    }
}
