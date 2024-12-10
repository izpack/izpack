package com.izforge.izpack.compiler.bootstrap;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.izforge.izpack.compiler.cli.CliAnalyzer;
import com.izforge.izpack.compiler.container.CompilerContainer;
import com.izforge.izpack.compiler.data.CompilerData;
import org.apache.commons.cli.ParseException;

@Singleton
public class TestCompilerDataProvider implements Provider<CompilerData> {
    public static String[] args;
    public static CompilerData compilerData;

    private final CliAnalyzer cliAnalyzer;
    private final CompilerContainer compilerContainer;

    @Inject
    public TestCompilerDataProvider(CliAnalyzer cliAnalyzer,
                                    CompilerContainer compilerContainer) {
        this.cliAnalyzer = cliAnalyzer;
        this.compilerContainer = compilerContainer;
    }

    @Override
    public CompilerData get() {
        if(compilerData != null) {
            return compilerData;
        }

        try {
            CompilerData compilerData = cliAnalyzer.printAndParseArgs(args);
            compilerContainer.addConfig("installFile", compilerData.getInstallFile());
            // REFACTOR : find a way to test with a fake home
            // compilerData.resolveIzpackHome();
            return compilerData;

        } catch (ParseException e) {
            throw new IllegalStateException("Error while parsing the command line", e);
        }
    }

    public static void reset() {
        args = null;
        compilerData = null;
    }
}
