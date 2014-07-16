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

import static org.fusesource.hawtjni.runtime.ArgFlag.BY_VALUE;
import static org.fusesource.hawtjni.runtime.ArgFlag.CRITICAL;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_IN;
import static org.fusesource.hawtjni.runtime.ArgFlag.NO_OUT;
import static org.fusesource.hawtjni.runtime.ArgFlag.POINTER_ARG;
import static org.fusesource.hawtjni.runtime.ArgFlag.SENTINEL;
import static org.fusesource.hawtjni.runtime.ArgFlag.UNICODE;
import static org.fusesource.hawtjni.runtime.ClassFlag.STRUCT;
import static org.fusesource.hawtjni.runtime.ClassFlag.TYPEDEF;
import static org.fusesource.hawtjni.runtime.FieldFlag.CONSTANT;
import static org.fusesource.hawtjni.runtime.MethodFlag.CONSTANT_INITIALIZER;
import static org.fusesource.hawtjni.runtime.MethodFlag.POINTER_RETURN;

import java.io.IOException;

import org.fusesource.hawtjni.runtime.ClassFlag;
import org.fusesource.hawtjni.runtime.JniArg;
import org.fusesource.hawtjni.runtime.JniClass;
import org.fusesource.hawtjni.runtime.JniField;
import org.fusesource.hawtjni.runtime.JniMethod;
import org.fusesource.hawtjni.runtime.Library;
import org.fusesource.hawtjni.runtime.PointerMath;

/**
 *
 * @author <a href="http://hiramchirino.com">Hiram Chirino</a>
 */
@JniClass(conditional="defined(_WIN32) || defined(_WIN64)")
public class Kernel32 {

    private static final Library LIBRARY = new Library("jansi", Kernel32.class);
    static {
        LIBRARY.load();
        init();
    }

    @JniMethod(flags={CONSTANT_INITIALIZER})
    private static final native void init();

    @JniField(flags={CONSTANT})
    public static short FOREGROUND_BLUE;
    @JniField(flags={CONSTANT})
    public static short FOREGROUND_GREEN;
    @JniField(flags={CONSTANT})
    public static short FOREGROUND_RED;
    @JniField(flags={CONSTANT})
    public static short FOREGROUND_INTENSITY;
    @JniField(flags={CONSTANT})
    public static short BACKGROUND_BLUE;
    @JniField(flags={CONSTANT})
    public static short BACKGROUND_GREEN;
    @JniField(flags={CONSTANT})
    public static short BACKGROUND_RED;
    @JniField(flags={CONSTANT})
    public static short BACKGROUND_INTENSITY;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_LEADING_BYTE;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_TRAILING_BYTE;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_GRID_HORIZONTAL;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_GRID_LVERTICAL;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_GRID_RVERTICAL;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_REVERSE_VIDEO;
    @JniField(flags={CONSTANT})
    public static short COMMON_LVB_UNDERSCORE;
    @JniField(flags={CONSTANT})
    public static int FORMAT_MESSAGE_FROM_SYSTEM;
    @JniField(flags={CONSTANT})
    public static int STD_INPUT_HANDLE;
    @JniField(flags={CONSTANT})
    public static int STD_OUTPUT_HANDLE;
    @JniField(flags={CONSTANT})
    public static int STD_ERROR_HANDLE;
    @JniField(flags={CONSTANT})
    public static int INVALID_HANDLE_VALUE;


    @JniMethod(cast="void *")
    public static final native long malloc(
            @JniArg(cast="size_t") long size);

    public static final native void free(
            @JniArg(cast="void *") long ptr);

    /**
     * http://msdn.microsoft.com/en-us/library/ms686311%28VS.85%29.aspx
     */
    @JniClass(flags={STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    static public class SMALL_RECT {
        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(SMALL_RECT)")
        public static int SIZEOF;

        @JniField(accessor="Left")
        public short left;
        @JniField(accessor="Top")
        public short top;
        @JniField(accessor="Right")
        public short right;
        @JniField(accessor="Bottom")
        public short bottom;

        public short width() {
            return (short) (right-left);
        }
        public short height() {
            return (short) (bottom-top);
        }
    }

    /**
     * see http://msdn.microsoft.com/en-us/library/ms686047%28VS.85%29.aspx
     * @param consoleOutput
     * @param attributes
     * @return
     */
    public static final native int SetConsoleTextAttribute(
            @JniArg(cast="HANDLE")long consoleOutput,
            short attributes);

    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class COORD {

        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(COORD)")
        public static int SIZEOF;

        @JniField(accessor="X")
        public short x;
        @JniField(accessor="Y")
        public short y;

        public COORD copy() {
          COORD rc = new COORD();
          rc.x = x;
          rc.y = y;
          return rc;
        }
    }

    /**
     * http://msdn.microsoft.com/en-us/library/ms682093%28VS.85%29.aspx
     */
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class CONSOLE_SCREEN_BUFFER_INFO {

        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(CONSOLE_SCREEN_BUFFER_INFO)")
        public static int SIZEOF;

        @JniField(accessor="dwSize")
        public COORD      size = new COORD();
        @JniField(accessor="dwCursorPosition")
        public COORD      cursorPosition = new COORD();
        @JniField(accessor="wAttributes")
        public short      attributes;
        @JniField(accessor="srWindow")
        public SMALL_RECT window = new SMALL_RECT();
        @JniField(accessor="dwMaximumWindowSize")
        public COORD      maximumWindowSize = new COORD();

        public int windowWidth() {
            return window.width() + 1;
        }

        public int windowHeight() {
            return window.height() + 1;
        }
    }


    /**
     * see: http://msdn.microsoft.com/en-us/library/ms724211%28VS.85%29.aspx
     *
     * @param handle
     * @return
     */
    public static final native int CloseHandle(@JniArg(cast="HANDLE")long handle);


    /**
     * see: http://msdn.microsoft.com/en-us/library/ms679360(VS.85).aspx
     *
     * @param handle
     * @return
     */
    public static final native int GetLastError();

    /**
     *
     * @param flags
     * @param source
     * @param messageId
     * @param languageId
     * @param buffer
     * @param size
     * @param arguments
     * @return
     */
    public static final native int FormatMessageW(
            int flags,
            @JniArg(cast="void *")long  source,
            int messageId,
            int languageId,
            @JniArg(cast="void *", flags={NO_IN, CRITICAL})byte[] buffer,
            int size,
            @JniArg(cast="void *", flags={NO_IN, CRITICAL, SENTINEL})long []args
            );


    /**
     * See: http://msdn.microsoft.com/en-us/library/ms683171%28VS.85%29.aspx
     * @param consoleOutput
     * @param consoleScreenBufferInfo
     * @return
     */
    public static final native int GetConsoleScreenBufferInfo(
            @JniArg(cast="HANDLE", flags={POINTER_ARG})long consoleOutput,
            CONSOLE_SCREEN_BUFFER_INFO consoleScreenBufferInfo);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683231%28VS.85%29.aspx
     * @param stdHandle
     * @return
     */
    @JniMethod(cast="HANDLE", flags={POINTER_RETURN})
    public static final native long GetStdHandle(int stdHandle);

    /**
     * http://msdn.microsoft.com/en-us/library/ms686025%28VS.85%29.aspx
     * @param consoleOutput
     * @param cursorPosition
     * @return
     */
    public static final native int SetConsoleCursorPosition(
            @JniArg(cast="HANDLE", flags={POINTER_ARG})long consoleOutput,
            @JniArg(flags={BY_VALUE}) COORD cursorPosition);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms682663%28VS.85%29.aspx
     *
     * @param consoleOutput
     * @param character
     * @param length
     * @param dwWriteCoord
     * @param numberOfCharsWritten
     * @return
     */
    public static final native int FillConsoleOutputCharacterW(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long consoleOutput,
            char character,
            int length,
            @JniArg(flags={BY_VALUE}) COORD writeCoord,
            int[] numberOfCharsWritten);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms687401(v=VS.85).aspx
     *
     * @param consoleOutput
     * @param buffer
     * @param numberOfCharsToWrite
     * @param numberOfCharsWritten
     * @param reserved must be null
     * @return
     */
    public static final native int WriteConsoleW(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long consoleOutput,
            char[] buffer,
            int numberOfCharsToWrite,
            int[] numberOfCharsWritten,
            @JniArg(cast="LPVOID", flags={POINTER_ARG})long reserved);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683167%28VS.85%29.aspx
     * @param handle
     * @param mode
     * @return
     */
    public static final native int GetConsoleMode(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long handle,
            int[] mode);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms686033%28VS.85%29.aspx
     * @param handle
     * @param mode
     * @return
     */
    public static final native int SetConsoleMode(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long handle,
            int mode);

    /**
     * see: http://msdn.microsoft.com/en-us/library/078sfkak(VS.80).aspx
     * @return
     */
    public static final native int _getch();


   /**
    * see: http://msdn.microsoft.com/en-us/library/ms686050%28VS.85%29.aspx
    * @param title
    * @return 0 if title was set successfully
    */
   public static final native int SetConsoleTitle(
               @JniArg(flags={UNICODE}) String title);


    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683169(v=VS.85).aspx
     * @return the current output code page
     */
    public static final native int GetConsoleOutputCP();

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms686036(v=VS.85).aspx
     * @param codePageID
     * @return non 0 if code page was set
     */
    public static final native int SetConsoleOutputCP(int codePageID);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms684166(v=VS.85).aspx
     */
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class KEY_EVENT_RECORD {

        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(KEY_EVENT_RECORD)")
        public static int SIZEOF;
        @JniField(flags={CONSTANT}, accessor="CAPSLOCK_ON")
        public static int CAPSLOCK_ON;
        @JniField(flags={CONSTANT}, accessor="NUMLOCK_ON")
        public static int NUMLOCK_ON;
        @JniField(flags={CONSTANT}, accessor="SCROLLLOCK_ON")
        public static int SCROLLLOCK_ON;
        @JniField(flags={CONSTANT}, accessor="ENHANCED_KEY")
        public static int ENHANCED_KEY;
        @JniField(flags={CONSTANT}, accessor="LEFT_ALT_PRESSED")
        public static int LEFT_ALT_PRESSED;
        @JniField(flags={CONSTANT}, accessor="LEFT_CTRL_PRESSED")
        public static int LEFT_CTRL_PRESSED;
        @JniField(flags={CONSTANT}, accessor="RIGHT_ALT_PRESSED")
        public static int RIGHT_ALT_PRESSED;
        @JniField(flags={CONSTANT}, accessor="RIGHT_CTRL_PRESSED")
        public static int RIGHT_CTRL_PRESSED;
        @JniField(flags={CONSTANT}, accessor="SHIFT_PRESSED")
        public static int SHIFT_PRESSED;

        @JniField(accessor="bKeyDown")
        public boolean keyDown;
        @JniField(accessor="wRepeatCount")
        public short repeatCount;
        @JniField(accessor="wVirtualKeyCode")
        public short keyCode;
        @JniField(accessor="wVirtualScanCode")
        public short scanCode;
        @JniField(accessor="uChar.UnicodeChar")
        public char uchar;
        @JniField(accessor="dwControlKeyState")
        public int controlKeyState;

        @Override
        public String toString() {
            return "KEY_EVENT_RECORD{" +
                    "keyDown=" + keyDown +
                    ", repeatCount=" + repeatCount +
                    ", keyCode=" + keyCode +
                    ", scanCode=" + scanCode +
                    ", uchar=" + uchar +
                    ", controlKeyState=" + controlKeyState +
                    '}';
        }
    }

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms684239(v=VS.85).aspx
     */
    /*
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class MOUSE_EVENT_RECORD {

        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(MOUSE_EVENT_RECORD)")
        public static int SIZEOF;
        @JniField(flags={CONSTANT}, accessor="FROM_LEFT_1ST_BUTTON_PRESSED")
        public static int FROM_LEFT_1ST_BUTTON_PRESSED;
        @JniField(flags={CONSTANT}, accessor="FROM_LEFT_2ND_BUTTON_PRESSED")
        public static int FROM_LEFT_2ND_BUTTON_PRESSED;
        @JniField(flags={CONSTANT}, accessor="FROM_LEFT_3RD_BUTTON_PRESSED")
        public static int FROM_LEFT_3RD_BUTTON_PRESSED;
        @JniField(flags={CONSTANT}, accessor="FROM_LEFT_4TH_BUTTON_PRESSED")
        public static int FROM_LEFT_4TH_BUTTON_PRESSED;
        @JniField(flags={CONSTANT}, accessor="RIGHTMOST_BUTTON_PRESSED")
        public static int RIGHTMOST_BUTTON_PRESSED;

        @JniField(flags={CONSTANT}, accessor="CAPSLOCK_ON")
        public static int CAPSLOCK_ON;
        @JniField(flags={CONSTANT}, accessor="NUMLOCK_ON")
        public static int NUMLOCK_ON;
        @JniField(flags={CONSTANT}, accessor="SCROLLLOCK_ON")
        public static int SCROLLLOCK_ON;
        @JniField(flags={CONSTANT}, accessor="ENHANCED_KEY")
        public static int ENHANCED_KEY;
        @JniField(flags={CONSTANT}, accessor="LEFT_ALT_PRESSED")
        public static int LEFT_ALT_PRESSED;
        @JniField(flags={CONSTANT}, accessor="LEFT_CTRL_PRESSED")
        public static int LEFT_CTRL_PRESSED;
        @JniField(flags={CONSTANT}, accessor="RIGHT_ALT_PRESSED")
        public static int RIGHT_ALT_PRESSED;
        @JniField(flags={CONSTANT}, accessor="RIGHT_CTRL_PRESSED")
        public static int RIGHT_CTRL_PRESSED;
        @JniField(flags={CONSTANT}, accessor="SHIFT_PRESSED")
        public static int SHIFT_PRESSED;

        @JniField(flags={CONSTANT}, accessor="DOUBLE_CLICK")
        public static int DOUBLE_CLICK;
        @JniField(flags={CONSTANT}, accessor="MOUSE_HWHEELED")
        public static int MOUSE_HWHEELED;
        @JniField(flags={CONSTANT}, accessor="MOUSE_MOVED")
        public static int MOUSE_MOVED;
        @JniField(flags={CONSTANT}, accessor="MOUSE_WHEELED")
        public static int MOUSE_WHEELED;

        @JniField(accessor="dwMousePosition")
        public COORD mousePosition = new COORD();
        @JniField(accessor="dwButtonState")
        public int buttonState;
        @JniField(accessor="dwControlKeyState")
        public int controlKeyState;
        @JniField(accessor="dwEventFlags")
        public int eventFlags;

        public String toString() {
            return "MOUSE_EVENT_RECORD{" +
                    "mousePosition=" + mousePosition +
                    ", buttonState=" + buttonState +
                    ", controlKeyState=" + controlKeyState +
                    ", eventFlags=" + eventFlags +
                    '}';
        }
    }
    */

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms687093(v=VS.85).aspx
     */
    /*
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class WINDOW_BUFFER_SIZE_RECORD {

        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(WINDOW_BUFFER_SIZE_RECORD)")
        public static int SIZEOF;

        @JniField(accessor="dwSize")
        public COORD size = new COORD();

        public String toString() {
            return "WINDOW_BUFFER_SIZE_RECORD{size=" + size + '}';
        }
    }
    */

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683149(v=VS.85).aspx
     */
    /*
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class FOCUS_EVENT_RECORD {
        static {
            LIBRARY.load();
            init();
        }
        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(WINDOW_BUFFER_SIZE_RECORD)")
        public static int SIZEOF;
        @JniField(accessor="bSetFocus")
        public boolean setFocus;
    }
    */

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms684213(v=VS.85).aspx
     */
    /*
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class MENU_EVENT_RECORD {
        static {
            LIBRARY.load();
            init();
        }
        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(MENU_EVENT_RECORD)")
        public static int SIZEOF;
        @JniField(accessor="dwCommandId")
        public int commandId;
    }
    */

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683499(v=VS.85).aspx
     */
    @JniClass(flags={ClassFlag.STRUCT,TYPEDEF}, conditional="defined(_WIN32) || defined(_WIN64)")
    public static class INPUT_RECORD {

        static {
            LIBRARY.load();
            init();
        }

        @JniMethod(flags={CONSTANT_INITIALIZER})
        private static final native void init();
        @JniField(flags={CONSTANT}, accessor="sizeof(INPUT_RECORD)")
        public static int SIZEOF;
        @JniField(flags={CONSTANT}, accessor="KEY_EVENT")
        public static short KEY_EVENT;
        /*
        @JniField(flags={CONSTANT}, accessor="MOUSE_EVENT")
        public static short MOUSE_EVENT;
        @JniField(flags={CONSTANT}, accessor="WINDOW_BUFFER_SIZE_EVENT")
        public static short WINDOW_BUFFER_SIZE_EVENT;
        @JniField(flags={CONSTANT}, accessor="FOCUS_EVENT")
        public static short FOCUS_EVENT;
        @JniField(flags={CONSTANT}, accessor="MENU_EVENT")
        public static short MENU_EVENT;
        */
        @JniField(accessor="EventType")
        public short eventType;
        @JniField(accessor="Event.KeyEvent")
        public KEY_EVENT_RECORD keyEvent = new KEY_EVENT_RECORD();
        /*
        @JniField(accessor="Event.MouseEvent")
        public MOUSE_EVENT_RECORD mouseEvent = new MOUSE_EVENT_RECORD();
        @JniField(accessor="Event.WindowBufferSizeEvent")
        public WINDOW_BUFFER_SIZE_RECORD windowBufferSizeEvent = new WINDOW_BUFFER_SIZE_RECORD();
        @JniField(accessor="Event.MenuEvent")
        public MENU_EVENT_RECORD menuEvent = new MENU_EVENT_RECORD();
        @JniField(accessor="Event.FocusEvent")
        public FOCUS_EVENT_RECORD focusEvent = new FOCUS_EVENT_RECORD();
        */
        public static final native void memmove (
          @JniArg(cast="void *", flags={NO_IN, CRITICAL}) INPUT_RECORD dest,
          @JniArg(cast="const void *", flags={NO_OUT, CRITICAL}) long src,
          @JniArg(cast="size_t") long size);

    }

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms684961(v=VS.85).aspx
     * @param handle
     * @param length
     * @param eventsCount
     * @return
     */
    private static final native int ReadConsoleInputW(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long handle,
            @JniArg(cast="PINPUT_RECORD", flags={POINTER_ARG}) long inputRecord,
            int length,
            int[] eventsCount);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms684344(v=VS.85).aspx
     * @param handle
     * @param length
     * @param eventsCount
     * @return
     */
    private static final native int PeekConsoleInputW(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long handle,
            @JniArg(cast="PINPUT_RECORD", flags={POINTER_ARG}) long inputRecord,
            int length,
            int[] eventsCount);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683207(v=VS.85).aspx
     * @param handle
     * @param numberOfEvents number of unread input records in the console's input buffer
     * @return
     */
    public static final native int GetNumberOfConsoleInputEvents(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long handle,
            int[] numberOfEvents);

    /**
     * see: http://msdn.microsoft.com/en-us/library/ms683147(v=VS.85).aspx
     * @param handle
     * @return
     */
    public static final native int FlushConsoleInputBuffer(
            @JniArg(cast="HANDLE", flags={POINTER_ARG}) long handle);

    /**
     * Return console input events.
     *
     * @param handle
     * @param count requested number of events
     * @return null on read errors
     */
    public static INPUT_RECORD[] readConsoleInputHelper(
            long handle, int count, boolean peek) throws IOException {
        int[] length = new int[1];
        int res;
        long inputRecordPtr = 0;
        try {
            inputRecordPtr = malloc(INPUT_RECORD.SIZEOF * count);
            if (inputRecordPtr == 0) {
                throw new IOException("cannot allocate memory with JNI");
            }
            res = peek ?
                    PeekConsoleInputW(handle, inputRecordPtr, count, length)
                    : ReadConsoleInputW(handle, inputRecordPtr, count, length);
            if (res == 0) {
                throw new IOException("ReadConsoleInputW failed");
            }
            if (length[0] <= 0) {
                return new INPUT_RECORD[0];
            }
            INPUT_RECORD[] records = new INPUT_RECORD[length[0]];
            for (int i = 0; i < records.length; i++) {
                records[i] = new INPUT_RECORD();
                INPUT_RECORD.memmove(records[i], PointerMath.add(inputRecordPtr, i*INPUT_RECORD.SIZEOF), INPUT_RECORD.SIZEOF);
            }
            return records;
        } finally {
            if (inputRecordPtr != 0) {
                free(inputRecordPtr);
            }
        }
    }

    /**
     * Return console input key events (discard other events).
     *
     * @param count requested number of events
     * @return array possibly of size smaller then count
     */
    public static INPUT_RECORD[] readConsoleKeyInput(long handle, int count, boolean peek)
            throws IOException {
        while (true) {
            // read events until we have keyboard events, the queue could be full
            // of mouse events.
            INPUT_RECORD[] evts = readConsoleInputHelper(handle, count, peek);
            int keyEvtCount = 0;
            for (INPUT_RECORD evt : evts) {
                if (evt.eventType == INPUT_RECORD.KEY_EVENT) keyEvtCount++;
            }
            if (keyEvtCount > 0) {
                INPUT_RECORD[] res = new INPUT_RECORD[keyEvtCount];
                int i = 0;
                for (INPUT_RECORD evt : evts) {
                    if (evt.eventType == INPUT_RECORD.KEY_EVENT) {
                        res[i++] = evt;
                    }
                }
                return res;
            }
        }
    }

}
