/*
 * IzPack - Copyright 2001-2012 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

package com.izforge.izpack.compiler.cli;

import com.izforge.izpack.api.data.PackCompression;
import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.exception.HelpRequestedException;
import com.izforge.izpack.compiler.exception.NoArgumentException;
import org.apache.commons.cli.*;

import java.io.PrintStream;
import java.util.List;


/**
 * Parse and analyze cli
 *
 * @author Anthonin Bonnefoy
 */
public class CliAnalyzer {
    private static final String ARG_IZPACK_HOME = "h";
    private static final String ARG_BASEDIR = "b";
    private static final String ARG_KIND = "k";
    private static final String ARG_OUTPUT = "o";
    private static final String ARG_COMPRESSION_FORMAT = "c";
    private static final String ARG_COMPRESSION_LEVEL = "l";


    public static CompilerData parseArguments(String... args) throws ParseException {
        CliAnalyzer cliAnalyzer = new CliAnalyzer();
        return cliAnalyzer.printAndParseArgs(args);
    }

    /**
     * Get options for the command line parser
     *
     * @return Options
     */
    private Options getOptions() {
        Options options = new Options();
        options.addOption("?", false, "Print help");
        options.addOption(ARG_IZPACK_HOME, true, "IzPack home : the root path of IzPack. This will be needed if the compiler " +
                "is not called in the root directory of IzPack." +
                "Do not forget quotations if there are blanks in the path.");
        options.addOption(ARG_BASEDIR, true, "base : indicates the base path that the compiler will use for filenames."
                + " of sources. Default is the current path. Attend to -h.");
        options.addOption(ARG_KIND, true, "kind : indicates the kind of installer to generate, default is standard");
        options.addOption(ARG_OUTPUT, true, "out  : indicates the output file name default is the xml file name\n");
        options.addOption(ARG_COMPRESSION_FORMAT, true, "compression : indicates the compression format to be used for packs " +
                "default is the internal deflate compression\n");
        options.addOption(ARG_COMPRESSION_LEVEL, true, "compression-level : indicates the level for the used compression format"
                + " if supported. Only integer are valid\n");
        return options;
    }

    /**
     * Parse args and print information
     *
     * @param args Command line arguments
     * @return Compile data with informations
     */
    public CompilerData printAndParseArgs(String[] args) throws ParseException {
        final PrintStream printStream = System.out;
        printHeader(printStream);
        CompilerData result = parseArgs(args);
        printTail(printStream, result);
        return result;
    }

    private void printHeader(PrintStream out) {
        // Outputs some informations
        out.println();
        out.format(".::  IzPack - Version %s ::.%n", CompilerData.IZPACK_VERSION);
        out.println();
        out.format("< compiler specifications version: %s >%n", CompilerData.VERSION);
        out.println();
        out.format("- Copyright (c) %s Julien Ponge and others. All Rights Reserved.%n", CompilerData.IZPACK_COPYYEARS);
        out.println("- Visit http://izpack.org/ for the latest releases");
        out.println("- Released under the terms of the Apache Software License version 2.0.");
        out.println();
    }

    /**
     * Print the result of parse analysis
     *
     * @param result Compile data created from arguments
     */
    private void printTail(PrintStream out, CompilerData result) {
        // Outputs what we are going to do
        out.format("-> Processing   : %s%n", result.getInstallFile());
        out.format("-> Output       : %s%n", result.getOutput());
        out.format("-> Base path    : %s%n", result.getBasedir());
        out.format("-> Kind         : %s%n", result.getKind());
        out.format("-> Compression  : %s%n", result.getComprFormat());
        out.format("-> Compr. level : %s%n", result.getComprLevel());
        out.format("-> IzPack home  : %s%n", CompilerData.IZPACK_HOME);
        out.println();
    }


    public CompilerData parseArgs(String[] args) throws ParseException {
        CommandLineParser parser = new PosixParser();
        CommandLine commandLine = parser.parse(getOptions(), args);
        return analyzeCommandLine(commandLine);
    }

    /**
     * Print help
     */
    private void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        String cmdLineUsage = "IzPack -> Command line parameters are : (xml file) [args]";
        String header = "(xml file): the xml file describing the installation";
        String footer = "When using vm option -DSTACKTRACE=true there is all kind of debug info ";
        formatter.printHelp(cmdLineUsage, header, getOptions(), footer);
    }

    /**
     * Analyze commandLine and fill compilerData.
     *
     * @param commandLine CommandLine to analyze
     * @return filled compilerData with informations
     */
    private CompilerData analyzeCommandLine(CommandLine commandLine) {
        validateCommandLine(commandLine);
        String installFile;
        String baseDir = ".";
        String output = "install.jar";
        String compression = PackCompression.DEFAULT.toName();

        if (commandLine.hasOption("?")) {
            printHelp();
            throw new HelpRequestedException();
        }
        List<String> argList = commandLine.getArgList();
        installFile = argList.get(0);
        if (commandLine.hasOption(ARG_BASEDIR)) {
            baseDir = commandLine.getOptionValue(ARG_BASEDIR).trim();
        }
        if (commandLine.hasOption(ARG_OUTPUT)) {
            output = commandLine.getOptionValue(ARG_OUTPUT).trim();
        }
        if (commandLine.hasOption(ARG_COMPRESSION_FORMAT)) {
            compression = commandLine.getOptionValue(ARG_COMPRESSION_FORMAT).trim();
        }
        CompilerData compilerData = new CompilerData(compression, installFile, baseDir, output, false);
        if (commandLine.hasOption(ARG_COMPRESSION_LEVEL)) {
            compilerData.setComprLevel(Integer.parseInt(commandLine.getOptionValue(ARG_COMPRESSION_LEVEL).trim()));
        }
        if (commandLine.hasOption(ARG_IZPACK_HOME)) {
            CompilerData.setIzpackHome(commandLine.getOptionValue(ARG_IZPACK_HOME).trim());
        }
        if (commandLine.hasOption(ARG_KIND)) {
            compilerData.setKind(commandLine.getOptionValue(ARG_KIND).trim());
        }

        return compilerData;
    }

    /**
     * Validate that a xml installation file is given in argument
     *
     * @param commandLine
     */
    private void validateCommandLine(CommandLine commandLine) {
        if (commandLine.getArgList().isEmpty()) {
            printHelp();
            throw new NoArgumentException();
        }
    }

}
