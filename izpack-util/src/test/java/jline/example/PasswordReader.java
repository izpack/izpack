/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright (c) 2002-2012, the original authors of JLine
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

package jline.example;

import java.io.IOException;

import jline.console.ConsoleReader;

public class PasswordReader
{
    public static void usage() {
        System.out.println("Usage: java "
            + PasswordReader.class.getName() + " [mask]");
    }

    public static void main(String[] args) throws IOException {
        ConsoleReader reader = new ConsoleReader();

        Character mask = (args.length == 0)
            ? new Character((char) 0)
            : new Character(args[0].charAt(0));

        String line;
        do {
            line = reader.readLine("Enter password> ", mask);
            System.out.println("Got password: " + line);
        }
        while (line != null && line.length() > 0);
    }
}
