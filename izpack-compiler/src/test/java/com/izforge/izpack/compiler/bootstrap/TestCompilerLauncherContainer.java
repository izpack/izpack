package com.izforge.izpack.compiler.bootstrap;

import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.logging.MavenStyleLogFormatter;

import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;

public class TestCompilerLauncherContainer extends CompilerContainer {



    @Override
    protected void fillContainer() {
        super.fillContainer();

        final ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(Level.INFO);
        consoleHandler.setFormatter(new MavenStyleLogFormatter());
        addComponent(Handler.class, consoleHandler);

        addProvider(CompilerData.class, TestCompilerDataProvider.class);
    }
}
