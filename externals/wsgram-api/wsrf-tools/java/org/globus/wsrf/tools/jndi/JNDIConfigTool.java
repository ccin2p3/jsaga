/*
 * Portions of this file Copyright 1999-2005 University of Chicago
 * Portions of this file Copyright 1999-2005 The University of Southern California.
 *
 * This file or a portion of this file is licensed under the
 * terms of the Globus Toolkit Public License, found at
 * http://www.globus.org/toolkit/download/license.html.
 * If you redistribute this file, with or without
 * modifications, you must include this notice in the file.
 */
package org.globus.wsrf.tools.jndi;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.tools.CommandLineTool;

// TODO: needs to be i18n
public class JNDIConfigTool extends CommandLineTool
{
    private static Log logger =
        LogFactory.getLog(JNDIConfigTool.class.getName());

    public static void main(String[] args)
    {
        boolean quiet = false;
        boolean overwrite = false;
        File configFile = null;
        FileInputStream configInput = null;
        Digester digester = null;
        ConfigContext inContext = null;
        CommandLine commandLine = null;
        Options options = new Options();
        Option option = null;
        HelpFormatter formatter = new HelpFormatter();

        option = new Option("a", "add", true,
            "Add the contents of the specified file");
        option.setArgName("FILE");
        options.addOption(option);
        option= new Option("r", "remove", true,
            "Remove the entry for the given service name");
        option.setArgName("SERVICENAME");
        options.addOption(option);
        option= new Option("f", "file", true,
            "Specifies the JNDI configuration file");
        option.setArgName("FILE");
        option.setRequired(true);
        options.addOption(option);
        options.addOption("h", "help", false, "Prints this help message");
        options.addOption("q", "quiet", false, "Don't print warnings");
        options.addOption("o", "overwrite", false, "Overwrite sections");
        CommandLineParser parser = new GnuParser();

        try
        {
            commandLine = parser.parse(options, args);
        }
        catch(Exception e)
        {
            logger.error("", e);
            // internationalize
            formatter.printHelp(JNDIConfigTool.class.getName(), options, true);
            sysExit(1);
        }

        args = commandLine.getArgs();

        if(args.length != 0)
        {
            // internationalize
            formatter.printHelp(JNDIConfigTool.class.getName(), options, false);
            sysExit(1);
        }

        if(commandLine.hasOption('h'))
        {
            formatter.printHelp(JNDIConfigTool.class.getName(), options, true);
            sysExit(0);
        }

        if(commandLine.hasOption('q'))
        {
            quiet = true;
        }

        if(commandLine.hasOption('o'))
        {
            overwrite = true;
        }

        try
        {
            configFile = new File(commandLine.getOptionValue('f')).getCanonicalFile();
        }
        catch(Exception e)
        {
            String msg = "ERROR: Failed to get canonical file name for configuration file";
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }

        try
        {
            configInput = new FileInputStream(configFile);
        }
        catch(FileNotFoundException e)
        {
            if(commandLine.hasOption('a'))
            {
                configInput = null;
            }
            else
            {
                String msg = "ERROR: Failed to open configuration file.";
                logger.error(msg, e);
                System.err.println(msg);
                sysExit(1);
            }
        }

        if(configInput != null)
        {
            digester = new Digester();

            // Don't do any validation for now
            // TODO: Need to write a real schema for this stuff
            digester.setNamespaceAware(true);
            digester.setValidating(false);
            digester.addRuleSet(new JNDIConfigRuleSet("jndiConfig/"));
            inContext = new ConfigContext(true);
            digester.push(inContext);

            try
            {
                digester.parse(configInput);
                digester.clear();
            }
            catch(Exception e)
            {
                String msg = "ERROR: Failed to parse configuration file.";
                logger.error(msg, e);
                System.err.println(msg);
                sysExit(1);
            }
        }

        if(commandLine.hasOption('a'))
        {
            File addFile = null;

            try
            {
                addFile = new File(commandLine.getOptionValue('a')).getCanonicalFile();
            }
            catch(Exception e)
            {
                String msg = "ERROR: Failed to get canonical file name for additions file";
                logger.error(msg, e);
                System.err.println(msg);
                sysExit(1);
            }

            if(configInput == null)
            {
                try
                {
                    copy(addFile, configFile);
                }
                catch(Exception e)
                {
                    String msg = "ERROR: Failed to copy additions file to configuration file.";
                    logger.error(msg, e);
                    System.err.println(msg);
                    sysExit(1);
                }
                sysExit(0);
            }
            else
            {
                InputStream addInput = null;
                ConfigContext addContext = null;

                try
                {
                    addInput = new FileInputStream(addFile);
                }
                catch(FileNotFoundException e)
                {
                    String msg = "ERROR: Failed to open additions file.";
                    logger.error(msg, e);
                    System.err.println(msg);
                    sysExit(1);
                }

                addContext = new ConfigContext(true);
                digester.push(addContext);

                try
                {
                    digester.parse(addInput);
                    digester.clear();
                }
                catch(Exception e)
                {
                    String msg = "ERROR: Failed to parse additions file.";
                    logger.error(msg, e);
                    System.err.println(msg);
                    sysExit(1);
                }

                try
                {
                    merge(inContext, addContext, null, null, overwrite);
                }
                catch(Exception e)
                {
                    String msg = "ERROR: Failed to merge additions file.";
                    logger.error(msg, e);
                    System.err.println(msg);
                    sysExit(1);
                }
            }
        }

        if(commandLine.hasOption('r'))
        {
            String serviceName;
            serviceName = commandLine.getOptionValue('r');
            inContext.removeSubContext(serviceName);
        }

        try
        {
            OutputStream configOutput = new FileOutputStream(configFile);
            PrintWriter configWriter = new PrintWriter(configOutput);
            configWriter.write(inContext.toString());
            configWriter.close();
        }
        catch (Exception e)
        {
            String msg = "ERROR: Failed to write processed configuration file.";
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }

        sysExit(0);
    }

    private static void copy(File inFile, File outFile)
        throws IOException, FileNotFoundException
    {
        BufferedInputStream inStream =
            new BufferedInputStream(new FileInputStream(inFile));
        BufferedOutputStream outStream =
            new BufferedOutputStream(new FileOutputStream(outFile));
        int b;
        while((b = inStream.read()) != -1)
        {
            outStream.write(b);
        }
        inStream.close();
        outStream.close();
    }

    private static void merge(ConfigContext targetContext,
        ConfigContext inContext, String section,
        String serviceName, boolean overwrite)
        throws Exception
    {
        Set names = null;
        Iterator nameIterator = null;
        String name;
        ConfigContext serviceContext = null;
        ConfigContext targetServiceContext = null;

        if(section == null || section.equals("service"))
        {
            if(serviceName != null)
            {
                if(inContext.getSubContext(serviceName) == null)
                {
                    throw new Exception(
                        "Couldn't find service node with name: " +
                        serviceName);
                }
                names = new HashSet();
                names.add(serviceName);
            }
            else
            {
                names = inContext.getSubContextNames();
            }

            nameIterator = names.iterator();

            while(nameIterator.hasNext())
            {
                name = (String) nameIterator.next();
                serviceContext = inContext.getSubContext(name);

                if(overwrite == true ||
                    (targetServiceContext =
                        targetContext.getSubContext(name)) == null)
                {
                    targetContext.addSubContext(serviceContext);
                }
                else
                {
                    mergeContexts(serviceContext,
                        targetServiceContext);
                }
            }
        }

        if(section == null || section.equals("global"))
        {
            if(overwrite == true)
            {
                names = targetContext.getSubContextNames();
                nameIterator = names.iterator();
                while(nameIterator.hasNext())
                {
                    name = (String) nameIterator.next();
                    if(inContext.getSubContext(name) == null)
                    {
                        inContext.addSubContext(
                            targetContext.getSubContext(name));
                    }
                }
            }
            else
            {
                mergeContexts(inContext, targetContext);
            }
        }
    }

    private static void mergeContexts(ConfigContext inContext, ConfigContext targetContext)
    {
        Set names = null;
        Iterator nameIterator = null;

        names = inContext.getEnvironmentNames();
        nameIterator = names.iterator();

        while(nameIterator.hasNext())
        {
            targetContext.addEnvironment(
                inContext.getEnvironment(
                    (String) nameIterator.next()));
        }

        names = inContext.getResourceNames();
        nameIterator = names.iterator();

        while(nameIterator.hasNext())
        {
            targetContext.addResource(
                inContext.getResource(
                    (String) nameIterator.next()));
        }

        names = inContext.getResourceLinkNames();
        nameIterator = names.iterator();

        while(nameIterator.hasNext())
        {
            targetContext.addResourceLink(
                inContext.getResourceLink(
                    (String) nameIterator.next()));
        }
    }
}
