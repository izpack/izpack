/*
 * IzPack - Copyright 2001-2020 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2020 Patrick Reinhart
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

package com.izforge.izpack.util;

import static org.junit.Assert.assertEquals;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Test;

public class FileUtilTest extends AbstractPlatformTest
{
  @Test
  public void testConvertUrlToFilePathFromUNCJar() throws MalformedURLException
  {
    URL uncUrl = new URL("jar:file://somehost.somedomain/share/setup.jar!/some%20path/somefile.txt");
    assertEquals("//somehost.somedomain/share/setup.jar!/some path/somefile.txt",
        FileUtil.convertUrlToFilePath(uncUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromUNCFile() throws MalformedURLException
  {
    URL uncUrl = new URL("file://somehost.somedomain/share/somefile.txt");
    assertEquals("//somehost.somedomain/share/somefile.txt", FileUtil.convertUrlToFilePath(uncUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromLinuxJar() throws MalformedURLException
  {
    URL linuxUrl = new URL("jar:file:/somedirectory/setup.jar!/some%20path/somefile.txt");
    assertEquals("/somedirectory/setup.jar!/some path/somefile.txt",
        FileUtil.convertUrlToFilePath(linuxUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromLinuxFile() throws MalformedURLException
  {
    URL linuxUrl = new URL("file:/somedirectory/somefile.txt");
    assertEquals("/somedirectory/somefile.txt", FileUtil.convertUrlToFilePath(linuxUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromWindowsJar() throws MalformedURLException
  {
    URL windowsUrl = new URL("jar:file:/C:/somedirectory/setup.jar!/some%20path/");
    assertEquals("C:/somedirectory/setup.jar!/some path/", FileUtil.convertUrlToFilePath(windowsUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromWindowsFile() throws MalformedURLException
  {
    URL windowsUrl = new URL("file:/C:/somedirectory/somefile.txt");
    assertEquals("C:/somedirectory/somefile.txt", FileUtil.convertUrlToFilePath(windowsUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromWindowsJarSpecial() throws MalformedURLException
  {
    URL windowsUrl = new URL("jar:file:C:/somedirectory/setup.jar!/some%20path/");
    assertEquals("C:/somedirectory/setup.jar!/some path/", FileUtil.convertUrlToFilePath(windowsUrl));
  }

  @Test
  public void testConvertUrlToFilePathFromWindowsFileSpefial() throws MalformedURLException
  {
    URL windowsUrl = new URL("file:C:/somedirectory/somefile.txt");
    assertEquals("C:/somedirectory/somefile.txt", FileUtil.convertUrlToFilePath(windowsUrl));
  }

  @Test
  public void testSpecialCharacterInURL() throws MalformedURLException
  {
    URL windowsUrl = new URL("file:C:/some directory/some file.txt"); // space is special character
    assertEquals("C:/some directory/some file.txt", FileUtil.convertUrlToFilePath(windowsUrl));
  }
}
