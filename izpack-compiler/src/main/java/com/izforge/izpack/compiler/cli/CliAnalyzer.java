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

import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;

import com.izforge.izpack.compiler.data.CompilerData;
import com.izforge.izpack.compiler.exception.HelpRequestedException;
import com.izforge.izpack.compiler.exception.NoArgumentException;


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
        printHeader();
        CompilerData result = parseArgs(args);
        printTail(result);
        return result;
    }

    private void printHeader() {
        // Outputs some informations
        System.out.println("");
        System.out.println(".::  IzPack - Version " + CompilerData.IZPACK_VERSION + " ::.");
        System.out.println("");
        System.out.println("< compiler specifications version: " + CompilerData.VERSION + " >");
        System.out.println("");
        System.out.println("- Copyright (c) 2001-2010 Julien Ponge and others. All Rights Reserved.");
        System.out.println("- Visit http://izpack.org/ for the latest releases");
        System.out
                .println("- Released under the terms of the Apache Software License version 2.0.");
        System.out.println("");
    }

    /**
     * Print the result of parse analysis
     *
     * @param result Compile data created from arguments
     */
    private void printTail(CompilerData result) {
        // Outputs what we are going to do
        System.out.println("-> Processing  : " + result.getInstallFile());
        System.out.println("-> Output      : " + result.getOutput());
        System.out.println("-> Base path   : " + result.getBasedir());
        System.out.println("-> Kind        : " + result.getKind());
        System.out.println("-> Compression : " + result.getComprFormat());
        System.out.println("-> Compr. level: " + result.getComprLevel());
        System.out.println("-> IzPack home : " + CompilerData.IZPACK_HOME);
        System.out.println("");
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

        if (commandLine.hasOption("?")) {
            printHelp();
            throw new HelpRequestedException();
        }
        List argList = commandLine.getArgList();
        installFile = (String) argList.get(0);
        if (commandLine.hasOption(ARG_BASEDIR)) {
            baseDir = commandLine.getOptionValue(ARG_BASEDIR).trim();
        }
        if (commandLine.hasOption(ARG_OUTPUT)) {
            output = commandLine.getOptionValue(ARG_OUTPUT).trim();
        }
        CompilerData compilerData = new CompilerData(installFile, baseDir, output, false);


        if (commandLine.hasOption(ARG_COMPRESSION_FORMAT)) {
            compilerData.setComprFormat(commandLine.getOptionValue(ARG_COMPRESSION_FORMAT).trim());
        }
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
        if (commandLine.getArgList().size() == 0) {
            printHelp();
            throw new NoArgumentException();
        }
    }

}
