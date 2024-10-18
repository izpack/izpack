/*
 * Copyright 2016 Julien Ponge, René Krell and the IzPack team.
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

import java.util.regex.Pattern;

/**
 * A representation of a version string for an implementation of the Java SE Platform.<br/>
 * Only the feature (aka major) part of the version is implemented for IzPack purposes.<br/>
 *
 * It supports the version scheme that has been in use since JDK 9
 * (see <a href='https://openjdk.org/jeps/223'>JEP 223: New Version-String Scheme</a>)
 * and the scheme that was used before JDK 9.<br/>
 * In fact, it strips the "1." prefix from the version string if it is present.<br/>
 * E.g. it will treat equally versions "1.8.0_402" and "8.0_402".
 */
public class JavaVersion
{
    /**
     * Current JDK version
     */
    public static final JavaVersion CURRENT = JavaVersion.parse(System.getProperty("java.version"));

    private final String src; //For equals() only while
    private final int feature;

    private JavaVersion(int feature, String src) {
        this.feature = feature;
        this.src = src;
    }
    /**
     * Parses the given string as a version string<br/>
     *
     * @param versionString
     *         A string to interpret as a version
     *
     * @throws NullPointerException
     *          If the given string is {@code null}
     *
     * @throws NumberFormatException
     *          If an element of the version number cannot be represented as an {@link Integer}
     * @return
     *          The JavaVersion for the given string
     */
    public static JavaVersion parse(String versionString) {
        if (versionString == null) throw new NullPointerException();

        if (versionString.startsWith("1.")) {
            versionString = versionString.substring(2);
        }
        String[] split = VersionPatterns.VERSION_PARS_SEPARATORS.split(versionString, 2);
        return new JavaVersion(Integer.parseInt(split[0]), versionString);
    }

    /**
     * Get feature (aka major) part of the version.<br/>
     * E.g. it will return 8 for versions "1.8" or "8.0_402".
     * @return
     *      The feature (aka major) part of the version
     */
    public int feature()
    {
        return feature;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (!(obj instanceof JavaVersion)) {
            return false;
        } else {
            JavaVersion that = (JavaVersion)obj;
            return this.src.equals(that.src);
        }
    }

    @Override
    public int hashCode()
    {
        return src.hashCode();
    }

    @Override
    public String toString() {
        return src;
    }

    private static class VersionPatterns {
        private static final Pattern VERSION_PARS_SEPARATORS = Pattern.compile("[.+_-]+");
    }
}
