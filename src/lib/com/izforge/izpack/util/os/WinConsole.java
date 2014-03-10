package com.izforge.izpack.util.os;


import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

/**
 * @author Ange POZZO
 */
public interface WinConsole extends StdCallLibrary
{
    WinConsole INSTANCE = (WinConsole) Native.loadLibrary("kernel32", WinConsole.class, W32APIOptions.UNICODE_OPTIONS);
    
    public boolean SetConsoleOutputCP(int codePage);
    
    public int GetConsoleOutputCP();
    
    public Pointer GetStdHandle(int stream);
    
    public boolean WriteConsoleW(Pointer stream, char[] text, int textLen, IntByReference caretPosition, Pointer reservedNull);
}