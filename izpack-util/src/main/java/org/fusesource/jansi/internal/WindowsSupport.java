/*
 * IzPack - Copyright 2001-2014 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2014 René Krell
 * Copyright (C) 2009, Progress Software Corporation and/or its
 * subsidiaries or affiliates.  All rights reserved.
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

package org.fusesource.jansi.internal;

import static org.fusesource.jansi.internal.Kernel32.FORMAT_MESSAGE_FROM_SYSTEM;
import static org.fusesource.jansi.internal.Kernel32.FlushConsoleInputBuffer;
import static org.fusesource.jansi.internal.Kernel32.FormatMessageW;
import static org.fusesource.jansi.internal.Kernel32.GetConsoleMode;
import static org.fusesource.jansi.internal.Kernel32.GetConsoleScreenBufferInfo;
import static org.fusesource.jansi.internal.Kernel32.GetLastError;
import static org.fusesource.jansi.internal.Kernel32.GetStdHandle;
import static org.fusesource.jansi.internal.Kernel32.INVALID_HANDLE_VALUE;
import static org.fusesource.jansi.internal.Kernel32.STD_INPUT_HANDLE;
import static org.fusesource.jansi.internal.Kernel32.STD_OUTPUT_HANDLE;
import static org.fusesource.jansi.internal.Kernel32.SetConsoleMode;
import static org.fusesource.jansi.internal.Kernel32.WriteConsoleW;
import static org.fusesource.jansi.internal.Kernel32._getch;
import static org.fusesource.jansi.internal.Kernel32.readConsoleKeyInput;

import java.io.IOException;

import org.fusesource.jansi.internal.Kernel32.CONSOLE_SCREEN_BUFFER_INFO;
import org.fusesource.jansi.internal.Kernel32.INPUT_RECORD;

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
public class WindowsSupport {

    public static String getLastErrorMessage() {
        int errorCode = GetLastError();
        int bufferSize = 160;
        byte data[] = new byte[bufferSize];
        FormatMessageW(FORMAT_MESSAGE_FROM_SYSTEM, 0, errorCode, 0, data, bufferSize, null);
        return new String(data);
    }

    //////////////////////////////////////////////////////////////////////////                                       00
    //
    // The following helper methods are for jline
    //
    //////////////////////////////////////////////////////////////////////////

    public static int readByte() {
        return _getch();
    }

    public static int getConsoleMode() {
       long hConsole = GetStdHandle (STD_INPUT_HANDLE);
        if (hConsole == INVALID_HANDLE_VALUE)
            return -1;
        int mode[] = new int[1];
        if (GetConsoleMode (hConsole, mode)==0)
            return -1;
        return mode[0];
    }

    public static void setConsoleMode(int mode) {
        long hConsole = GetStdHandle (STD_INPUT_HANDLE);
        if (hConsole == INVALID_HANDLE_VALUE)
            return;
        SetConsoleMode (hConsole, mode);
    }

    public static int getWindowsTerminalWidth() {
        long outputHandle = GetStdHandle (STD_OUTPUT_HANDLE);
        CONSOLE_SCREEN_BUFFER_INFO info = new CONSOLE_SCREEN_BUFFER_INFO();
        GetConsoleScreenBufferInfo (outputHandle, info);
        return info.windowWidth();
    }

    public static int getWindowsTerminalHeight() {
        long outputHandle = GetStdHandle (STD_OUTPUT_HANDLE);
        CONSOLE_SCREEN_BUFFER_INFO info = new CONSOLE_SCREEN_BUFFER_INFO();
        GetConsoleScreenBufferInfo (outputHandle, info);
        return info.windowHeight();
    }

    public static int writeConsole(String msg) {
        long hConsole = GetStdHandle (STD_OUTPUT_HANDLE);
        if (hConsole == INVALID_HANDLE_VALUE)
            return 0;
        char[] chars = msg.toCharArray();
        int[] written =  new int[1];
        if (WriteConsoleW(hConsole, chars, chars.length, written, 0) != 0) {
            return written[0];
        } else {
            return 0;
        }
    }

    public static INPUT_RECORD[] readConsoleInput(int count) throws IOException {
        long hConsole = GetStdHandle (STD_INPUT_HANDLE);
        if (hConsole == INVALID_HANDLE_VALUE)
            return null;
        return readConsoleKeyInput(hConsole, count, false);
    }

    public static INPUT_RECORD[] peekConsoleInput(int count) throws IOException {
        long hConsole = GetStdHandle (STD_INPUT_HANDLE);
        if (hConsole == INVALID_HANDLE_VALUE)
            return null;
        return readConsoleKeyInput(hConsole, count, true);
    }

    public static void flushConsoleInputBuffer() {
        long hConsole = GetStdHandle (STD_INPUT_HANDLE);
        if (hConsole == INVALID_HANDLE_VALUE)
            return;
        FlushConsoleInputBuffer(hConsole);
    }
}
