/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright (c) 2002-2012, the original authors of the JLine project
 * Copyright 2014 René Krell
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

package jline.internal;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * URL helpers.
 *
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 * @author <a href="mailto:gnodet@gmail.com">Guillaume Nodet</a>
 * @since 2.7
 */
public class Urls
{
    public static URL create(final String input) {
        if (input == null) {
            return null;
        }
        try {
            return new URL(input);
        }
        catch (MalformedURLException e) {
            return create(new File(input));
        }
    }

    public static URL create(final File file) {
        try {
            return file != null ? file.toURI().toURL() : null;
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }
}