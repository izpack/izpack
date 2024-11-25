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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.izforge.izpack.api.exception.ResourceNotFoundException;
import com.izforge.izpack.installer.container.TestLanguageContainer;
import com.izforge.izpack.test.Container;
import com.izforge.izpack.test.junit.PicoRunner;
import com.izforge.izpack.util.Platform;
import com.izforge.izpack.util.TargetPlatformFactory;

import jakarta.inject.Inject;


/**
 * Created by IntelliJ IDEA.
 *
 * @author Anthonin Bonnefoy
 * @author Bill Root
 */
@RunWith(PicoRunner.class)
@Container(TestLanguageContainer.class)
public class Unix_ShortcutTest
{
    private final String NOT_FOUND = "!!!NOT FOUND!!!";

    /**
     * The factory.
     */
    @Inject
    private TargetPlatformFactory factory;

    @Test
    @Ignore
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
        // File targetFileName = new File(System.getProperty("user.home") + File.separator
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

//        aSample.createExtXdgDesktopIconCmd(new File(System.getProperty("user.home")));
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
      final String userLanguage = System.getProperty("user.language", "en");

      assertTrue(result.startsWith("[Desktop Entry]"));

      assertEquals(categories, getValue(result, "Categories"));
      assertEquals(description, getValue(result, "Comment"));
      assertEquals(description, getValue(result, "Comment[" + userLanguage + "]"));
      assertEquals(encoding, getValue(result, "Encoding"));
      // TryExec is not used -- "causes too many problems"
      assertEquals(NOT_FOUND, getValue(result, "TryExec"));

      assertEquals(NOT_FOUND, getValue(result, "Exec"));

      assertEquals("", getValue(result, "GenericName"));
      assertEquals("", getValue(result, "GenericName[" + userLanguage + "]"));
      assertEquals(iconLocation, getValue(result, "Icon"));
      assertEquals(mimeType, getValue(result, "MimeType"));
      assertEquals(linkName, getValue(result, "Name"));
      assertEquals(linkName, getValue(result, "Name[" + userLanguage + "]"));
      assertEquals(workingDirectory, getValue(result, "Path"));
      assertEquals("", getValue(result, "ServiceTypes"));
      assertEquals("", getValue(result, "SwallowExec"));
      assertEquals("", getValue(result, "SwallowTitle"));
      assertEquals(terminal, getValue(result, "Terminal"));
      assertEquals(terminalOptions, getValue(result, "TerminalOptions"));
      assertEquals(type, getValue(result, "Type"));
      assertEquals(url, getValue(result, "URL"));
      assertEquals(kdeSubstUID, getValue(result, "X-KDE-SubstituteUID"));
      assertEquals(kdeUserName, getValue(result, "X-KDE-Username"));
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
      final String userLanguage = System.getProperty("user.language", "en");

      assertTrue(result.startsWith("[Desktop Entry]"));

      assertEquals(categories, getValue(result, "Categories"));
      assertEquals(description, getValue(result, "Comment"));
      assertEquals(description, getValue(result, "Comment[" + userLanguage + "]"));
      assertEquals(encoding, getValue(result, "Encoding"));
      // TryExec is not used -- "causes too many problems"
      assertEquals(NOT_FOUND, getValue(result, "TryExec"));

      // since targetPath contains no spaces, it will not be quoted
      String exec = targetPath + " " + arguments;
      assertEquals(exec, getValue(result, "Exec"));

      assertEquals("", getValue(result, "GenericName"));
      assertEquals("", getValue(result, "GenericName[" + userLanguage + "]"));
      assertEquals(iconLocation, getValue(result, "Icon"));
      assertEquals(mimeType, getValue(result, "MimeType"));
      assertEquals(linkName, getValue(result, "Name"));
      assertEquals(linkName, getValue(result, "Name[" + userLanguage + "]"));
      assertEquals(workingDirectory, getValue(result, "Path"));
      assertEquals("", getValue(result, "ServiceTypes"));
      assertEquals("", getValue(result, "SwallowExec"));
      assertEquals("", getValue(result, "SwallowTitle"));
      assertEquals(terminal, getValue(result, "Terminal"));
      assertEquals(terminalOptions, getValue(result, "TerminalOptions"));
      assertEquals(type, getValue(result, "Type"));
      assertEquals(NOT_FOUND, getValue(result, "URL"));
      assertEquals(kdeSubstUID, getValue(result, "X-KDE-SubstituteUID"));
      assertEquals(kdeUserName, getValue(result, "X-KDE-Username"));
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
      final String userLanguage = System.getProperty("user.language", "en");

      assertTrue(result.startsWith("[Desktop Entry]"));

      assertEquals(categories, getValue(result, "Categories"));
      assertEquals(description, getValue(result, "Comment"));
      assertEquals(description, getValue(result, "Comment[" + userLanguage + "]"));
      assertEquals(encoding, getValue(result, "Encoding"));
      // TryExec is not used -- "causes too many problems"
      assertEquals(NOT_FOUND, getValue(result, "TryExec"));

      String exec = targetPath + " " + arguments;
      // since targetPath contains no spaces, it will not be quoted
      assertEquals(exec, getValue(result, "Exec"));

      assertEquals("", getValue(result, "GenericName"));
      assertEquals("", getValue(result, "GenericName[" + userLanguage + "]"));
      assertEquals(iconLocation, getValue(result, "Icon"));
      assertEquals(mimeType, getValue(result, "MimeType"));
      assertEquals(linkName, getValue(result, "Name"));
      assertEquals(linkName, getValue(result, "Name[" + userLanguage + "]"));
      assertEquals(workingDirectory, getValue(result, "Path"));
      assertEquals("", getValue(result, "ServiceTypes"));
      assertEquals("", getValue(result, "SwallowExec"));
      assertEquals("", getValue(result, "SwallowTitle"));
      assertEquals(terminal, getValue(result, "Terminal"));
      assertEquals(terminalOptions, getValue(result, "TerminalOptions"));
      assertEquals(type, getValue(result, "Type"));
      assertEquals(NOT_FOUND, getValue(result, "URL"));
      assertEquals(kdeSubstUID, getValue(result, "X-KDE-SubstituteUID"));
      assertEquals(kdeUserName, getValue(result, "X-KDE-Username"));
    }
}
