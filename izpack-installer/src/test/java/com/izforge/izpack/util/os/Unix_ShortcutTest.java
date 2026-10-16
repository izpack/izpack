/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2013 Anthonin Bonnefoy
 * Copyright 2015 Bill Root
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.izforge.izpack.util.os;

import static java.lang.System.getProperty;
import static org.assertj.core.api.Assertions.assertThat;

import com.izforge.izpack.api.container.Container;
import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.core.container.AbstractContainer;
import com.izforge.izpack.core.container.PlatformProvider;
import com.izforge.izpack.core.data.DefaultVariables;
import com.izforge.izpack.core.factory.DefaultObjectFactory;
import com.izforge.izpack.core.resource.ResourceManager;
import com.izforge.izpack.installer.data.InstallData;
import com.izforge.izpack.test.util.TestLibrarian;
import com.izforge.izpack.util.DefaultTargetPlatformFactory;
import com.izforge.izpack.util.Housekeeper;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.Platforms;
import com.izforge.izpack.util.TargetFactory;
import com.izforge.izpack.util.TargetPlatformFactory;
import java.io.IOException;
import java.util.Properties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.injectors.ProviderAdapter;

/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 * @author Bill Root
 */
public class Unix_ShortcutTest
{
    private final String NOT_FOUND = "!!!NOT FOUND!!!";


    /**
     * The factory.
     */
    private TargetPlatformFactory factory;

    private Container container;


    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @BeforeEach
    public void setUp() throws Exception
    {
        container = new AbstractContainer()
        {
            {
                initialise();
            }

            @Override
            protected void fillContainer(MutablePicoContainer container)
            {
                addComponent(Properties.class);
                addComponent(DefaultVariables.class);
                addComponent(ResourceManager.class);
                addComponent(InstallData.class);
                addComponent(TestLibrarian.class);
                addComponent(Housekeeper.class);
                addComponent(TargetFactory.class);
                addComponent(DefaultObjectFactory.class);
                addComponent(DefaultTargetPlatformFactory.class);
                addComponent(Platforms.class);
                addComponent(Container.class, this);
                container.addAdapter(new ProviderAdapter(new PlatformProvider()));
            }
        };
        factory = container.getComponent(TargetPlatformFactory.class);
    }


    @Test
    @Disabled
    public void main() throws IOException, ResourceNotFoundException
    {
//        Unix_Shortcut aSample = new Unix_Shortcut(idata);
//        System.out.println(">>" + aSample.getClass().getName() + "- Test Main Program\n\n");

//        try
//        {
//            aSample.initialize(Unix_Shortcut.APPLICATIONS, "Start Tomcat");
//        }
//        catch (Exception exc)
//        {
//            System.err.println("Could not init Unix_Shourtcut");
//        }
//
//        aSample.replace();
//        System.out.println(aSample);

        //
        //
        //
        // File targetFileName = new File(getProperty("user.home") + File.separator
        // + "Start Tomcat" + DESKTOP_EXT);
        // FileWriter fileWriter = null;
        //
        // try
        // {
        // fileWriter = new FileWriter(targetFileName);
        // }
        // catch (IOException e1)
        // {
        // e1.printStackTrace();
        // }
        //
        // try
        // {
        // fileWriter.write( aSample.toString() );
        // }
        // catch (IOException e)
        // {
        // e.printStackTrace();
        // }
        //
        // try
        // {
        // fileWriter.close();
        // }
        // catch (IOException e2)
        // {
        // e2.printStackTrace();
        // }

//        aSample.createExtXdgDesktopIconCmd(new File(getProperty("user.home")));
//        System.out.println("DONE.\n");
    }


    /**
     * Returns the value for the specified keyName in a string of key=value lines.
     * @param source
     * @param keyName
     * @return value for specified keyName, or NOT_FOUND
     */
    private String getValue(String source, String keyName)
    {
        String start = keyName + "=";
        String lines[] = source.split("\n");
        for (String line: lines)
            if (line.startsWith(start))
                return line.substring(start.length());

        return NOT_FOUND;
    }


    /**
     * Verifies that the correct desktop file contents are created for a link
     * shortcut.
     *
     * This does <b>not</b> verify that the produced desktop file launches the
     * intended link.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLink() throws Exception
    {
      Platform platform = new Platform(Platform.Name.LINUX);
      Unix_Shortcut shortcut = (Unix_Shortcut) factory.create(Shortcut.class, platform);

      final String arguments        = "";
      final String categories       = "Office;";
      final String description      = "This is the description";
      final String encoding         = "UTF-8";
      final String iconLocation     = "/home/bill/folder/the_icon.png";
      final int    iconIndex        = 1;
      final String targetPath       = "";
      final String terminal         = "false";
      final String kdeSubstUID      = "false";
      final String kdeUserName      = "bill";
      final String linkName         = "testLink";
      final int    linkType         = Shortcut.DESKTOP;
      final String mimeType         = "application/x-dummy";
      final String programGroup     = "MyProgramGroup";
      final int    showCommand      = Shortcut.NORMAL;
      final String terminalOptions  = "not used";
      final String tryExec          = "ignored";
      final String type             = "Link";
      final String url              = "/home/bill/folder/document.html";
      final int    userType         = Shortcut.ALL_USERS;
      final String workingDirectory = "/home/bill/folder";

      shortcut.setArguments(arguments);
      shortcut.setCategories(categories);
      shortcut.setDescription(description);
      shortcut.setEncoding(encoding);
      shortcut.setIconLocation(iconLocation, iconIndex);
      shortcut.setKdeSubstUID(kdeSubstUID);
      shortcut.setKdeUserName(kdeUserName);
      shortcut.setLinkName(linkName);
      shortcut.setLinkType(linkType);
      shortcut.setMimetype(mimeType);
      shortcut.setProgramGroup(programGroup);
      shortcut.setShowCommand(showCommand);
      shortcut.setTargetPath(targetPath);
      shortcut.setTerminal(terminal);
      shortcut.setTerminalOptions(terminalOptions);
      shortcut.setTryExec(tryExec);
      shortcut.setType(type);
      shortcut.setURL(url);
      shortcut.setUserType(userType);
      shortcut.setWorkingDirectory(workingDirectory);

      final String result = shortcut.build();
      final String userLanguage = getProperty("user.language", "en");

      assertThat(result).startsWith("[Desktop Entry]");

      assertThat(getValue(result, "Categories")).isEqualTo(categories);
      assertThat(getValue(result, "Comment")).isEqualTo(description);
      assertThat(getValue(result, "Comment[" + userLanguage + "]")).isEqualTo(description);
      assertThat(getValue(result, "Encoding")).isEqualTo(encoding);
      // TryExec is not used -- "causes too many problems"
      assertThat(getValue(result, "TryExec")).isEqualTo(NOT_FOUND);

      assertThat(getValue(result, "Exec")).isEqualTo(NOT_FOUND);

      assertThat(getValue(result, "GenericName")).isEmpty();
      assertThat(getValue(result, "GenericName[" + userLanguage + "]")).isEmpty();
      assertThat(getValue(result, "Icon")).isEqualTo(iconLocation);
      assertThat(getValue(result, "MimeType")).isEqualTo(mimeType);
      assertThat(getValue(result, "Name")).isEqualTo(linkName);
      assertThat(getValue(result, "Name[" + userLanguage + "]")).isEqualTo(linkName);
      assertThat(getValue(result, "Path")).isEqualTo(workingDirectory);
      assertThat(getValue(result, "ServiceTypes")).isEmpty();
      assertThat(getValue(result, "SwallowExec")).isEmpty();
      assertThat(getValue(result, "SwallowTitle")).isEmpty();
      assertThat(getValue(result, "Terminal")).isEqualTo(terminal);
      assertThat(getValue(result, "TerminalOptions")).isEqualTo(terminalOptions);
      assertThat(getValue(result, "Type")).isEqualTo(type);
      assertThat(getValue(result, "URL")).isEqualTo(url);
      assertThat(getValue(result, "X-KDE-SubstituteUID")).isEqualTo(kdeSubstUID);
      assertThat(getValue(result, "X-KDE-Username")).isEqualTo(kdeUserName);
    }


    /**
     * Verifies that the correct desktop file contents are created for a simple
     * shortcut.
     *
     * This does <b>not</b> verify that the produced desktop file launches the
     * intended program.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSimple() throws Exception
    {
      Platform platform = new Platform(Platform.Name.LINUX);
      Unix_Shortcut shortcut = (Unix_Shortcut) factory.create(Shortcut.class, platform);

      final String arguments        = "abc";
      final String categories       = "Office;";
      final String description      = "This is the description";
      final String encoding         = "UTF-8";
      final String iconLocation     = "/home/bill/folder/the_icon.png";
      final int    iconIndex        = 1;
      final String targetPath       = "/home/bill/folder/the_file.sh";
      final String terminal         = "false";
      final String kdeSubstUID      = "false";
      final String kdeUserName      = "bill";
      final String linkName         = "testSimple";
      final int    linkType         = Shortcut.DESKTOP;
      final String mimeType         = "application/x-dummy";
      final String programGroup     = "MyProgramGroup";
      final int    showCommand      = Shortcut.NORMAL;
      final String terminalOptions  = "not used";
      final String tryExec          = "ignored";
      final String type             = "Application";
      final String url              = "";
      final int    userType         = Shortcut.ALL_USERS;
      final String workingDirectory = "/home/bill/folder";

      shortcut.setArguments(arguments);
      shortcut.setCategories(categories);
      shortcut.setDescription(description);
      shortcut.setEncoding(encoding);
      shortcut.setIconLocation(iconLocation, iconIndex);
      shortcut.setKdeSubstUID(kdeSubstUID);
      shortcut.setKdeUserName(kdeUserName);
      shortcut.setLinkName(linkName);
      shortcut.setLinkType(linkType);
      shortcut.setMimetype(mimeType);
      shortcut.setProgramGroup(programGroup);
      shortcut.setShowCommand(showCommand);
      shortcut.setTargetPath(targetPath);
      shortcut.setTerminal(terminal);
      shortcut.setTerminalOptions(terminalOptions);
      shortcut.setTryExec(tryExec);
      shortcut.setType(type);
      shortcut.setURL(url);
      shortcut.setUserType(userType);
      shortcut.setWorkingDirectory(workingDirectory);

      final String result = shortcut.build();
      final String userLanguage = getProperty("user.language", "en");

      assertThat(result).startsWith("[Desktop Entry]");

      assertThat(getValue(result, "Categories")).isEqualTo(categories);
      assertThat(getValue(result, "Comment")).isEqualTo(description);
      assertThat(getValue(result, "Comment[" + userLanguage + "]")).isEqualTo(description);
      assertThat(getValue(result, "Encoding")).isEqualTo(encoding);
      // TryExec is not used -- "causes too many problems"
      assertThat(getValue(result, "TryExec")).isEqualTo(NOT_FOUND);

      // since targetPath contains no spaces, it will not be quoted
      String exec = targetPath + " " + arguments;
      assertThat(getValue(result, "Exec")).isEqualTo(exec);

      assertThat(getValue(result, "GenericName")).isEmpty();
      assertThat(getValue(result, "GenericName[" + userLanguage + "]")).isEmpty();
      assertThat(getValue(result, "Icon")).isEqualTo(iconLocation);
      assertThat(getValue(result, "MimeType")).isEqualTo(mimeType);
      assertThat(getValue(result, "Name")).isEqualTo(linkName);
      assertThat(getValue(result, "Name[" + userLanguage + "]")).isEqualTo(linkName);
      assertThat(getValue(result, "Path")).isEqualTo(workingDirectory);
      assertThat(getValue(result, "ServiceTypes")).isEmpty();
      assertThat(getValue(result, "SwallowExec")).isEmpty();
      assertThat(getValue(result, "SwallowTitle")).isEmpty();
      assertThat(getValue(result, "Terminal")).isEqualTo(terminal);
      assertThat(getValue(result, "TerminalOptions")).isEqualTo(terminalOptions);
      assertThat(getValue(result, "Type")).isEqualTo(type);
      assertThat(getValue(result, "URL")).isEqualTo(NOT_FOUND);
      assertThat(getValue(result, "X-KDE-SubstituteUID")).isEqualTo(kdeSubstUID);
      assertThat(getValue(result, "X-KDE-Username")).isEqualTo(kdeUserName);
    }


    /**
     * Verifies that the correct desktop file contents are created for a
     * shortcut with a space in the path.
     *
     * This does <b>not</b> verify that the produced desktop file launches the
     * intended program.
     *
     * @throws Exception for any error
     */
    @Test
    public void testSpaceInPath() throws Exception
    {
      Platform platform = new Platform(Platform.Name.LINUX);
      Unix_Shortcut shortcut = (Unix_Shortcut) factory.create(Shortcut.class, platform);

      final String workingDirectory = "/home/bill/folder with spaces";

      final String arguments        = "abc";
      final String categories       = "Office;";
      final String description      = "This is the description";
      final String encoding         = "UTF-8";
      final String iconLocation     = workingDirectory + "/the_icon.png";
      final int    iconIndex        = 1;
      final String targetPath       = workingDirectory + "/the_file.sh";
      final String terminal         = "false";
      final String kdeSubstUID      = "false";
      final String kdeUserName      = "bill";
      final String linkName         = "testSpaceInPath";
      final int    linkType         = Shortcut.DESKTOP;
      final String mimeType         = "application/x-dummy";
      final String programGroup     = "MyProgramGroup";
      final int    showCommand      = Shortcut.NORMAL;
      final String terminalOptions  = "not used";
      final String tryExec          = "ignored";
      final String type             = "Application";
      final String url              = "";
      final int    userType         = Shortcut.ALL_USERS;

      shortcut.setArguments(arguments);
      shortcut.setCategories(categories);
      shortcut.setDescription(description);
      shortcut.setEncoding(encoding);
      shortcut.setIconLocation(iconLocation, iconIndex);
      shortcut.setKdeSubstUID(kdeSubstUID);
      shortcut.setKdeUserName(kdeUserName);
      shortcut.setLinkName(linkName);
      shortcut.setLinkType(linkType);
      shortcut.setMimetype(mimeType);
      shortcut.setProgramGroup(programGroup);
      shortcut.setShowCommand(showCommand);
      shortcut.setTargetPath(targetPath);
      shortcut.setTerminal(terminal);
      shortcut.setTerminalOptions(terminalOptions);
      shortcut.setTryExec(tryExec);
      shortcut.setType(type);
      shortcut.setURL(url);
      shortcut.setUserType(userType);
      shortcut.setWorkingDirectory(workingDirectory);

      final String result = shortcut.build();
      final String userLanguage = getProperty("user.language", "en");

      assertThat(result).startsWith("[Desktop Entry]");

      assertThat(getValue(result, "Categories")).isEqualTo(categories);
      assertThat(getValue(result, "Comment")).isEqualTo(description);
      assertThat(getValue(result, "Comment[" + userLanguage + "]")).isEqualTo(description);
      assertThat(getValue(result, "Encoding")).isEqualTo(encoding);
      // TryExec is not used -- "causes too many problems"
      assertThat(getValue(result, "TryExec")).isEqualTo(NOT_FOUND);

      String exec = targetPath + " " + arguments;
      // since targetPath contains no spaces, it will not be quoted
      assertThat(getValue(result, "Exec")).isEqualTo(exec);

      assertThat(getValue(result, "GenericName")).isEmpty();
      assertThat(getValue(result, "GenericName[" + userLanguage + "]")).isEmpty();
      assertThat(getValue(result, "Icon")).isEqualTo(iconLocation);
      assertThat(getValue(result, "MimeType")).isEqualTo(mimeType);
      assertThat(getValue(result, "Name")).isEqualTo(linkName);
      assertThat(getValue(result, "Name[" + userLanguage + "]")).isEqualTo(linkName);
      assertThat(getValue(result, "Path")).isEqualTo(workingDirectory);
      assertThat(getValue(result, "ServiceTypes")).isEmpty();
      assertThat(getValue(result, "SwallowExec")).isEmpty();
      assertThat(getValue(result, "SwallowTitle")).isEmpty();
      assertThat(getValue(result, "Terminal")).isEqualTo(terminal);
      assertThat(getValue(result, "TerminalOptions")).isEqualTo(terminalOptions);
      assertThat(getValue(result, "Type")).isEqualTo(type);
      assertThat(getValue(result, "URL")).isEqualTo(NOT_FOUND);
      assertThat(getValue(result, "X-KDE-SubstituteUID")).isEqualTo(kdeSubstUID);
      assertThat(getValue(result, "X-KDE-Username")).isEqualTo(kdeUserName);
    }
}
