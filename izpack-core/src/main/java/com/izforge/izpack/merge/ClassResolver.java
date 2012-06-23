/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.merge;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.izforge.izpack.api.exception.MergeException;
import com.izforge.izpack.merge.resolve.ResolveUtils;
import com.izforge.izpack.util.FileUtil;

/**
 * Helper methods for class
 *
 * @author Anthonin Bonnefoy
 */
public class ClassResolver
{
    public static final List<String> packageBegin = Arrays.asList("com/", "org/", "net/");

    /**
     * Search for a standard package begin like com/ org/ net/
     *
     * @param file File to process
     * @return Full className
     */
    public static String processFileToClassName(File file)
    {
        String absolutePath = ResolveUtils.convertPathToPosixPath(file.getAbsolutePath());
        for (String packageString : packageBegin)
        {
            if (!absolutePath.contains(packageString))
            {
                continue;
            }
            return absolutePath.substring(absolutePath.lastIndexOf(packageString)).replaceAll("\\.class", "").replaceAll("/", ".");
        }
        throw new MergeException("No standard package begin found in " + file.getPath());
    }

    public static String processFileToClassName(File file, Package aPackage)
    {
        String absolutePath = ResolveUtils.convertPathToPosixPath(file.getAbsolutePath());
        String packagePath = convertPackageToPath(aPackage.getName());
        if (ClassResolver.isFilePathContainingPackage(file.getAbsolutePath(), aPackage))
        {
            return absolutePath.substring(absolutePath.lastIndexOf(packagePath)).replaceAll("\\.class", "").replaceAll("/", ".");
        }
        throw new MergeException("The file " + file + " does not contains the package " + aPackage);
    }

    public static String processURLToClassName(URL url)
    {
        return processFileToClassName(FileUtil.convertUrlToFile(url));
    }

    public static boolean isFullClassName(String className)
    {
        return className.contains(".");
    }

    public static String getLastPackagePart(String packageName)
    {
        String[] packages = packageName.split("\\.");
        return packages[packages.length - 1];
    }

    public static String convertPackageToPath(String packageName)
    {
        return packageName.replaceAll("\\.", "/");
    }

    public static boolean isFilePathContainingPackage(String filePath, Package aPackage)
    {
        return filePath.contains(aPackage.getName().replaceAll("\\.", "/"));
    }

    public static boolean isFilePathInsidePackageSet(String filePath, Set<Package> packageSet)
    {
        for (Package aPackage : packageSet)
        {
            if (filePath.contains(aPackage.getName().replaceAll("\\.", "/")))
            {
                return true;
            }
        }
        return false;
    }

    public static boolean isUrlContainingPackage(URL url, String aPackage)
    {
        return FileUtil.convertUrlToFilePath(url).contains(aPackage.replaceAll("\\.", "/"));
    }
}
