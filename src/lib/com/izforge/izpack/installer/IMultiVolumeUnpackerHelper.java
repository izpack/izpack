package com.izforge.izpack.installer;

import java.io.File;

import com.izforge.izpack.util.AbstractUIProgressHandler;

public interface IMultiVolumeUnpackerHelper
{
    public void init(AutomatedInstallData idata, AbstractUIProgressHandler handler);
    public File enterNextMediaMessage(String volumename, boolean lastcorrupt);
    public File enterNextMediaMessage(String volumename);
}