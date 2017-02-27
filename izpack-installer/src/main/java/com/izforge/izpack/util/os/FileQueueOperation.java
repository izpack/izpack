package com.izforge.izpack.util.os;

import java.io.IOException;

public interface FileQueueOperation
{

    public abstract void addTo(WinSetupFileQueue filequeue) throws IOException;
}
