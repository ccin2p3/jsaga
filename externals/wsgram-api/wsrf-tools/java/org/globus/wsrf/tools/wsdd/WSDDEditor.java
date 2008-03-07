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
package org.globus.wsrf.tools.wsdd;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDDocument;
import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.axis.utils.LockableHashtable;
import org.apache.axis.utils.XMLUtils;

import org.w3c.dom.Document;

import org.globus.common.ChainedException;
import org.globus.util.Util;

// FIXME:
// Better error handling for the class
// Debug mode for command line client
// Merging to and reading from properties file, is only parameters
// (no attributes incliuded and service is expected to be deployed
// for both cases. So cannot be used to bootstrap deployment of a new
// service).
public class WSDDEditor {

    private static Log logger =
        LogFactory.getLog(WSDDEditor.class.getName());

    private final static String CONFIG_FILE_NAME = "server-config.wsdd";
    private static File configFile = new File(CONFIG_FILE_NAME);
    private final static String GLOBUS_LOCATION = "GLOBUS_LOCATION";
    private static final String GLOBAL_CONFIG = "globalConfiguration";

    private WSDDDocument wsddDoc;
    private WSDDDeployment deployment;
    private WSDDGlobalConfiguration globalConfig;
    private String configFileName;

    private static final String editorOptPrompt =
        "This is an interactive edit mode. Use -help for other options\n" +
        "Main menu:\n" +
        "Enter (i) for interactive prompt (or)\n" +
        "      (p) for use of properties file options (or)\n" +
        "      (q) to quit editor tool. \n Value: ";

    private static final String propertiesFilePrompt =
        "\nOptions:\n" +
        "write[w] : Writes to property file \n" +
        "merge[m] : Merges properties file entries with server config\n" +
        "Value: ";

    private static final String filePrompt =
        "Enter file name to process. If empty, server-config.wsdd in \n" +
        "current directory is used. If not server-config.wsdd in \n" +
        "GLOBUS_LOCATION is used.\nValue:  ";

    private static final String optionPrompt =
        "\nOptions:\n"
        + "add[a]: Adds the value of parameter using comma separator if \n"
        + "        parameter exists. If not a new parameter is added.\n"
        + "set[s]: Sets the value of parameter, overwrites the old parameter\n"
        + "        if required. New parameter will be added if not present. \n"
        + "query[q]: Returns the value of the parameter \nValue:  ";

    private static final String continuePrompt =
        "\nOptions:\n" +
        " Enter (c) to continue (or)\n" +
        "       (s) to save and quit (or)\n" +
        "       (q) to just quit.\n Value: ";

    /**
     * Looks for 'server-config.wsdd' first in $GLOBUS_LOCATION, second in
     * current dir.
     */
    public WSDDEditor() throws Exception {

        if (configFile.exists()) {
            logger.debug("Current directory");
            getConfigurationDocument(CONFIG_FILE_NAME);
        } else {
            String globusLocation = System.getProperty(GLOBUS_LOCATION);
            if ((globusLocation == null) ||
                (globusLocation.trim().equals(""))) {
                    String err = CONFIG_FILE_NAME + " does not exist in current dir"
                                 + " nor GLOBUS_LOCATION has been set";
                    logger.error(err);
                    throw new Exception(err);
                } else {
                    if (!globusLocation.endsWith(File.separator))
                        globusLocation = globusLocation + File.separator;
                    getConfigurationDocument(globusLocation + CONFIG_FILE_NAME);
                }
        }
    }

    /**
     * Parameter is path and name of file
     */
    public WSDDEditor(String pathToConfigFile) throws Exception {
        getConfigurationDocument(pathToConfigFile);
    }

    /**
     * Document object representing the server config
     */
    public WSDDEditor(Document doc) throws Exception {
        setWSDDValues(doc, null);
    }

    // Sets the deployment object
    private void getConfigurationDocument(String fileName) throws Exception {

        FileInputStream input = null;
        try {
            input = new FileInputStream(fileName);
            Document doc = XMLUtils.newDocument(input);
            setWSDDValues(doc, fileName);
        }
        catch (FileNotFoundException fnfe) {
            throw new ChainedException("The configuration file " + fileName
                                       + " cannot be found. ", fnfe);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    // Sets all class variables.
    private void setWSDDValues(Document doc, String filename)
        throws Exception {

        this.wsddDoc = new WSDDDocument(doc);
        this.deployment = wsddDoc.getDeployment();
        this.globalConfig = this.deployment.getGlobalConfiguration();
        this.configFileName = filename;
    }

    /**
     * Returns the path to the output file, if any was used as input.
     */
    public String getWSDDFilename() {
        return this.configFileName;
    }

    /**
     * Returns global configuration
     */
    public WSDDGlobalConfiguration getGlobalConfiguration() {
        return this.globalConfig;
    }

    /**
     * Sets parameter in globalConfiguration. Overwrites if paramater
     * already exists. Creates a new one if does exist.
     */
    public void setGlobalParameter(String paramName, String value)
        throws Exception {
        if (this.globalConfig == null) {
            logger.error("Global configuration is null");
            throw new Exception("Global configuration is null");
        }
        globalConfig.setParameter(paramName, value);
    }

    /**
     * Returns the value of the parameter within the global configuration.
     * If parameter does not exist, null is returned.
     */
    public String getGlobalParameter(String paramName) throws Exception {
        if (this.globalConfig == null) {
            logger.error("Global configuration is null");
            throw new Exception("Global configuration is null");
        }
        return globalConfig.getParameter(paramName);
    }

    /**
     * Add parameter value. If parameter, exists, the value is appended
     * with comma separators.
     */
    public void addGlobalParameter(String paramName, String value)
        throws Exception {
        if (this.globalConfig == null) {
            logger.error("Global configuration is null");
            throw new Exception("Global configuration is null");
        }
        String paramValue = globalConfig.getParameter(paramName);
        if (paramValue == null) {
            globalConfig.setParameter(paramName, value);
        } else {
            paramValue = paramValue + "," + value;
            globalConfig.setParameter(paramName, paramValue);
        }
    }

    /**
     * Returns the properties for a service as an array of strings
     * with the following structure: propertyName=propertyValue.
     */
    public String[] getGlobalProperties() throws Exception {
        if (this.globalConfig == null) {
            logger.error("Global configuration is null");
            throw new Exception("Global configuration is null");
        }
        LockableHashtable props = this.globalConfig.getParametersTable();
        return parseAsStrings(props);
    }

    /**
     * Returns the value of the parameter within the service config block
     * If parameter does not exist, null is returned.
     */
    public String getServiceParameter(String serviceName, String paramName)
        throws Exception {
        WSDDService service = getService(serviceName);
        return service.getParameter(paramName);
    }

    /**
     * Sets the value of the parameter in the service configuration block.
     * Overwrites if paramater already exists. Creates a new one if does exist.
     */
    public void setServiceParameter(String serviceName, String paramName,
                                    String value) throws Exception {
        WSDDService service = getService(serviceName);
        setServiceParameter(service, paramName, value);
    }

    /**
     * Sets the value of the parameter in the service configuration block.
     * Overwrites if paramater already exists. Creates a new one if does exist.
     */
    public void setServiceParameter(WSDDService service, String paramName,
                                    String value) throws Exception {
        service.setParameter(paramName, value);
    }

    /**
     * Adds value to the parameter in the service configuration block.
     * If parameter, exists, the value is appended with comma separators.
     */
    public void addServiceParameter(String serviceName, String paramName,
                                    String value)
        throws Exception {
        WSDDService service = getService(serviceName);
        String paramValue = service.getParameter(paramName);

        if (paramValue == null) {
            service.setParameter(paramName, value);
        } else {
            paramValue = paramValue + "," + value;
            service.setParameter(paramName, paramValue);
        }
    }

    /**
     * Returns the properties for a service as an array of strings
     * with the following structure: propertyName=propertyValue.
     */
    public String[] getServiceProperties(String serviceName)
        throws Exception {
        WSDDService service = getService(serviceName);
        LockableHashtable props = service.getParametersTable();
        return parseAsStrings(props);
    }

    private String[] parseAsStrings(LockableHashtable props) {
        Set propSet = props.entrySet();
        if (propSet.size() <= 0)
            return null;
        String properties[] = new String[propSet.size()];
        Iterator iterator = propSet.iterator();
        int i=0;
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry)iterator.next();
            properties[i]= (String)entry.getKey() + "="
                           + (String)entry.getValue();
            i++;
        }
        return properties;
    }

    // returns service object for given service name.
    public WSDDService getService(String serviceName) throws Exception {
        // FIXME QName may need deloyment NS
        WSDDService service =
            this.deployment.getWSDDService(new QName(serviceName));
        if (service == null) {
            String err = "Service " + serviceName + " does not exist";
            logger.error(err);
            throw new Exception(err);
        }
        return service;
    }

    /**
     * Writes changes back to the file that was used as input
     */
    public void writeChanges() throws Exception {
        writeChanges(this.configFileName);
    }

    /**
     * Writes changes to given filename
     */
    public void writeChanges(String pathToOutputFile) throws Exception {

        if (pathToOutputFile == null) {
            logger.error("Output file name is null");
            throw new Exception("Output file name is null");
        }
        String newDeploymentDesc =
            XMLUtils.DocumentToString(this.wsddDoc.getDOMDocument());
        writeStringToFile(pathToOutputFile, newDeploymentDesc);
    }

    /**
     * Fixes bad eol character on said file.
     */
    public void fixEolError(String pathToFile) throws Exception {

        if (pathToFile == null) {
            logger.error("File name is null");
            throw new Exception("File name is null");
        }

        String outputString = "";
        BufferedReader bufReader = null;
        try {
            bufReader = new BufferedReader(new FileReader(pathToFile));
            String line = null;
            while ((line = bufReader.readLine()) != null) {
                outputString = outputString + line + "\n";
            }
        } catch (IOException ioe) {
            logger.error("Could not read " + pathToFile + " file", ioe);
            throw new ChainedException("Could not read " + pathToFile, ioe);
        } finally {
            if (bufReader != null) {
                bufReader.close();
            }
        }
        writeStringToFile(pathToFile, outputString);
    }

    // writes given string to a file.
    private void writeStringToFile(String pathToFile, String stringToWrite)
        throws Exception {

        FileWriter output = null;
        try {
            output = new FileWriter(pathToFile);
            output.write(stringToWrite);
        }
        catch (IOException ioe) {
            logger.error("Could not write " + pathToFile + " file", ioe);
            throw new ChainedException("Could not write " + pathToFile, ioe);
        }
        finally {
            if (output != null) {
                output.close();
            }
        }
    }

    // Command line interface for the editor.
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            processArguments(args);
        }
        do {
            int option = getEditorOption();
            switch (option) {
                case 0:
                    continue;

                case 1:
                    System.out.println("Interactive mode.\n");
                    processInteractiveMode();
                    break;

                case 2:
                    System.out.println("Properties file mode.\n");
                    processPropertiesFileMode();
                    break;

                case 3:
                    System.out.println("Quitting editor");
                    System.exit(0);
            }
        } while (true);
    }

    private static void processArguments(String args[]) throws Exception {

        String options =
            " Options are:\n" +
            " [-file fileName] <serviceOption> <operation> (or)\n"
            + " [-file fileName] -propFile <propFileName> <serviceOption> "
            + "<propFileOption>\n"
            + " where:\n"
            + "fileName : name of config file. If not used, server-config.wsdd"
            + "\n          in current directory or "
            + "\n          GLOBUS_LOCATION/server-config.wsdd is used.\n"
            + "<serviceOption> : \n"
            + "\t -service <name> : <name> is name of service \n"
            + "\t -global         : if global configuration\n"
            + "<operation> :\n"
            + "\t -set <paramName> <paramValue>\n"
            + "\t -add <paramName> <paramValue>\n"
            + "\t -get <paramName>\n"
            + "\t\t <paramName>  : Name of paramater\n"
            + "\t\t <paramValue> : Value of paramater\n"
            + "<propFileName> : Name of properties file\n"
            + "<propFileOption> :\n"
            + "\t-merge : Merge entries from properties file into config file\n"
            + "\t-write : Write entries to properties file from config file\n"
            + "To use interactive mode, use command with no arguments.";

        WSDDEditor editor = null;
        String fileName = null;
        String serviceName = null;
        // 0 = get, 1 = set, 2 = add
        int operation = -1;
        String paramName = null;
        String value = null;
        String propertiesFileName = null;
        // 0 = write, 1 = merge
        int propOpereration = -1;

        int i=0;
        int length = args.length;
        while (i < length) {

            if (args[i].equals("-help")) {
                System.out.println(options);
                System.exit(0);
            }

            if (args[i].equals("-file")) {
                if ((i+1) >= length) {
                    printAndQuit("-file option requires an argument");
                }
                fileName = args[++i];
            }

            if (args[i].equals("-propFile")) {
                if ((i+1) >= length) {
                    printAndQuit("-propFile option requires an argument");
                }
                propertiesFileName = args[++i];
            }

            if (args[i].equals("-service")) {
                if ((i+1) >= length) {
                    printAndQuit("-service option requires an argument");
                }
                serviceName = args[++i];
            }

            if (args[i].equals("-global")) {
                serviceName = WSDDEditor.GLOBAL_CONFIG;
            }

            if (args[i].equals("-write")) {
                propOpereration = 0;
            }

            if (args[i].equals("-merge")) {
                propOpereration = 1;
            }

            if (args[i].equals("-set")) {
                if ((i+2) >= length) {
                    printAndQuit("-set option requires: parameterName "
                                 + "parameterValue");
                }
                operation = 1;
                paramName = args[++i];
                value = args[++i];
            }

            if (args[i].equals("-get")) {
                if ((i+1) >= length) {
                    printAndQuit("-get option requires: parameterName");
                }
                operation = 0;
                paramName = args[++i];
            }

            if (args[i].equals("-add")) {
                if ((i+2) >= length) {
                    printAndQuit("-add option requires: parameterName "
                                 + "parameterValue");
                }
                operation = 2;
                paramName = args[++i];
                value = args[++i];
            }
            i++;
        }

        // Properties option
        if (propertiesFileName != null) {
            switch (propOpereration) {
                case -1:
                    printAndQuit("No valid operation chosen for given properties "
                                 + "file ");

                case 0:
                    // write
                    editor = getEditor(fileName);
                    processWriteToFileOption(serviceName, editor,
                                             propertiesFileName);
                    break;

                case 1:
                    // read
                    editor = getEditor(fileName);
                    processMergeFromFileOption(serviceName, editor,
                                               propertiesFileName);
                    editor.writeChanges();
                    editor.fixEolError(editor.getWSDDFilename());
            }
            System.exit(0);
        }

        // Non-properties-file option
        switch (operation) {
            case -1:
                printAndQuit("No valid operation chosen.");

            case 0:
                // get
                editor = getEditor(fileName);
                processQueryOption(editor, serviceName, paramName);
                break;

            case 1:
                // set
                editor = getEditor(fileName);
                processSetOption(editor, serviceName, paramName, value);
                editor.writeChanges();
                editor.fixEolError(editor.getWSDDFilename());
                break;

            case 2:
                // add
                editor = getEditor(fileName);
                processAddOption(editor, serviceName, paramName, value);
                editor.writeChanges();
                editor.fixEolError(editor.getWSDDFilename());
                break;
        }
        System.exit(0);
    }

    private static void printAndQuit(String err) {
        System.err.println(err);
        System.exit(-1);
    }

    // Process the option to interactively make changes to config file.
    private static void processInteractiveMode() throws Exception {
        WSDDEditor editor = getEditor();
        // Prompt and get changes, until quit is chosen.
        String loopOption = null;
        do {
            int option = processOptionPrompt();
            switch (option) {
                case 0:
                    break;

                case 1:
                    if (processAddOption(editor))
                        System.out.println("Parameter added successfully");
                    break;

                case 2:
                    if (processSetOption(editor))
                        System.out.println("Parameter value set successfully");
                    break;

                case 3:
                    processQueryOption(editor);
            }

            loopOption = processContinueOptionPrompt();
            if (loopOption.equals("s")) {
                editor.writeChanges();
                editor.fixEolError(editor.getWSDDFilename());
                System.out.println("Changes committed.");
                return;
            }
        } while (loopOption.equals("c"));
    }

    // Processes the option to write/merge to/from properties file.
    private static void processPropertiesFileMode() throws Exception {

        WSDDEditor editor = getEditor();
        // Prompt and get changes, until quit is chosen.
        String loopOption = null;
        do {
            String serviceName = getElementName();
            int option = processPropFilePrompt();
            switch (option) {
                case 0:
                    break;

                case 1:
                    processWriteToFileOption(serviceName, editor);
                    break;

                case 2:
                    processMergeFromFileOption(serviceName, editor);
                    break;
            }

            loopOption = processContinueOptionPrompt();
            if (loopOption.equals("s")) {
                editor.writeChanges();
                editor.fixEolError(editor.getWSDDFilename());
                System.out.println("Changes committed.");
                return;
            }
        } while (loopOption.equals("c"));
    }

    // process properties file manipulation potion
    private static int processPropFilePrompt() {
        String optionInput = Util.getInput(propertiesFilePrompt);
        if (optionInput.trim().equalsIgnoreCase("write") ||
            optionInput.trim().equalsIgnoreCase("w"))
            return 1;
        else if (optionInput.trim().equalsIgnoreCase("merge") ||
                 optionInput.trim().equalsIgnoreCase("m"))
            return 2;
        System.out.println("Erroneous input.\n");
        return 0;
    }

    // Merge the service parameters from file.
    private static void processMergeFromFileOption(String serviceName,
                                                   WSDDEditor editor)
        throws Exception {
        String prompt = "Enter file name to merge from. By default, "
                        + "servicename.properties (or globalConfiguration.properties)\n."
                        + "Value: ";
        // Get file name
        boolean global = isGlobal(serviceName);
        String fileName = getFilename(prompt, global, serviceName);
        processMergeFromFileOption(serviceName, editor, fileName);
    }

    private static void processMergeFromFileOption(String serviceName,
                                                   WSDDEditor editor,
                                                   String fileName)
        throws Exception {

        boolean global = isGlobal(serviceName);
        WSDDService service = null;
        if (!global) {
            service = editor.getService(serviceName);
        }
        Properties prop = new Properties();
        try {
            prop.load(new FileInputStream(fileName));
        } catch (IOException exp) {
            System.err.println("Could not load properties file " + fileName);
            return;
        }
        for (Enumeration e = prop.propertyNames(); e.hasMoreElements();) {
            String propKey = (String)e.nextElement();
            if (global) {
                editor.setGlobalParameter(propKey,
                                          (String)prop.getProperty(propKey));
            } else {
                editor.setServiceParameter(service, propKey,
                                           (String)prop.getProperty(propKey));
            }
        }
        System.out.println("Properties merged successfully");
    }

    // Writes properties of a service or global configuration to a file.
    private static void processWriteToFileOption(String serviceName,
                                                 WSDDEditor editor)
        throws Exception {

        String prompt = "Enter file name to write to. By default, "
                        + "servicename.properties\n.Value: ";
        boolean global = isGlobal(serviceName);
        String fileName = getFilename(prompt, global, serviceName);
        processWriteToFileOption(serviceName, editor, fileName);
    }

    private static void processWriteToFileOption(String serviceName,
                                                 WSDDEditor editor,
                                                 String fileName)
        throws Exception {

        String serviceProperties[] = null;
        boolean global = false;
        if (isGlobal(serviceName)) {
            serviceProperties = editor.getGlobalProperties();
            global = true;
        }
        else
            serviceProperties = editor.getServiceProperties(serviceName);

        if (serviceProperties == null) {
            System.out.println("No properties to write.\n");
            return;
        }

        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new FileWriter(fileName));
            for (int i=0; i<serviceProperties.length; i++) {
                writer.println(serviceProperties[i]);
            }
            System.out.println("Written successfully to file " + fileName);
        } catch (IOException exp) {
            System.err.println("Error writing to file. " + exp);
        } finally {
            try {
                if (writer != null)
                    writer.close();
            } catch (Exception exp) {
                System.err.println("Error closing writer. " + exp);
            }
        }
    }

    // Get file name
    private static String getFilename(String prompt, boolean global,
                                      String serviceName) {
        String fileName = Util.getInput(prompt);
        if ((fileName == null) || (fileName.trim().equals(""))) {
            if (global)
                fileName = "globalConfiguration.properties";
            else
                fileName = (serviceName.substring(serviceName.lastIndexOf("/")
                                                  + 1, serviceName.length()))
                           + ".properties";
        }
        return fileName;
    }

    // interactive or properties file.
    private static int getEditorOption() {
        String editOpts = Util.getInput(editorOptPrompt);
        if (editOpts.trim().equalsIgnoreCase("i"))
            return 1;
        else if (editOpts.trim().equalsIgnoreCase("p"))
            return 2;
        else if (editOpts.trim().equalsIgnoreCase("q"))
            return 3;
        return 0;
    }

    // file name for config file.
    private static String processFileInput() {
        String optionInput = Util.getInput(filePrompt);
        if ((optionInput == null) || (optionInput.trim().equals("")))
            return null;
        else
            return optionInput;
    }

    // add, set, get options are processed.
    private static int processOptionPrompt() {
        String optionInput = Util.getInput(optionPrompt);
        if (optionInput.trim().equalsIgnoreCase("add") ||
            optionInput.trim().equalsIgnoreCase("a"))
            return 1;
        else if (optionInput.trim().equalsIgnoreCase("set") ||
                 optionInput.trim().equalsIgnoreCase("s"))
            return 2;
        else if (optionInput.trim().equalsIgnoreCase("query") ||
                 optionInput.trim().equalsIgnoreCase("q"))
            return 3;
        else {
            System.out.println("Erroneous input.");
            return 0;
        }
    }

    // add option
    private static boolean processAddOption(WSDDEditor editor)
        throws Exception {

        String elemName = getElementName();
        String paramName = getParameterName("add to");
        String paramVal = getParameterValue("add");
        return processAddOption(editor, elemName, paramName, paramVal);
    }

    private static boolean processAddOption(WSDDEditor editor, String elemName,
                                            String paramName, String paramVal)
        throws Exception {

        try {
            if (isGlobal(elemName)) {
                editor.addGlobalParameter(paramName, paramVal);
            } else {
                editor.addServiceParameter(elemName, paramName, paramVal);
            }
        } catch(Exception exp) {
            System.err.println(exp.getMessage());
            return false;
        }
        return true;
    }

    // set option
    private static boolean processSetOption(WSDDEditor editor)
        throws Exception {

        String elemName = getElementName();
        String paramName = getParameterName("set");
        String paramVal = getParameterValue("set");
        return processSetOption(editor, elemName, paramName, paramVal);
    }

    private static boolean processSetOption(WSDDEditor editor, String elemName,
                                            String paramName, String paramVal)
        throws Exception {
        try {
            if (isGlobal(elemName)) {
                editor.setGlobalParameter(paramName, paramVal);
            } else {
                editor.setServiceParameter(elemName, paramName, paramVal);
            }
        } catch(Exception exp) {
            System.err.println(exp.getMessage());
            return false;
        }
        return true;
    }

    // query option
    private static boolean processQueryOption(WSDDEditor editor)
        throws Exception {

        String elemName = getElementName();
        String paramName = getParameterName("query");
        return processQueryOption(editor, elemName, paramName);
    }

    private static boolean processQueryOption(WSDDEditor editor,
                                              String elemName, String paramName)
        throws Exception {

        String returnString = null;
        try {
            if (isGlobal(elemName)) {
                returnString = editor.getGlobalParameter(paramName);
            } else {
                returnString = editor.getServiceParameter(elemName, paramName);
            }
            if (returnString == null) {
                System.out.println("Parameter \"" + paramName + "\" does not "
                                   + "exist in \"" + elemName + "\"");
            } else {
                System.out.println("Value of parameter is: \"" + returnString
                                   + "\"");
            }
        } catch(Exception exp) {
            System.err.println(exp.getMessage());
            return false;
        }
        return true;
    }

    // Returns true if its a globalConfiguration element
    private static boolean isGlobal(String elementName) {
        if ((elementName != null) && ((elementName.equals(GLOBAL_CONFIG))
                                      || (elementName.equalsIgnoreCase("g"))))
            return true;
        return false;
    }

    // continue, save & quit, quit.
    private static String processContinueOptionPrompt() {

        String optionInput = null;
        do {
            optionInput = Util.getInput(continuePrompt);
        } while ((optionInput == null)
                 || (!((optionInput.trim().equalsIgnoreCase("s"))
                       || (optionInput.trim().equalsIgnoreCase("c"))
                       || (optionInput.trim().equalsIgnoreCase("q")))));
        return optionInput.trim();
    }

    // gets input for element name (globalConfig or service name)
    private static String getElementName() {

        String elementName = null;
        String msg =
            "Enter service name (or) 'globalConfiguration' or 'g'\n"
            + "for global parameters: ";
        do {
            elementName = Util.getInput(msg);
        } while ((elementName == null) || (elementName.trim().equals("")));
        return elementName;
    }

    // gets name of parameter name
    private static String getParameterName(String optionName) {
        String paramName = null;
        do {
            paramName = Util.getInput("Enter name of parameter to "
                                      + optionName + ": ");
        } while ((paramName == null) || (paramName.trim().equals("")));
        return paramName;
    }

    // gets name of parameter value
    private static String getParameterValue(String optionName) {
        String paramVal = null;
        do {
            paramVal =   Util.getInput("Enter value of parameter to "
                                       + optionName + ": ");
        } while ((paramVal == null) || (paramVal.trim().equals("")));
        return paramVal;
    }

    // returns reference to editor tool
    private static WSDDEditor getEditor() throws Exception {
        String fileName = processFileInput();
        return getEditor(fileName);
    }

    private static WSDDEditor getEditor(String fileName) throws Exception {
        WSDDEditor editor = null;
        if (fileName == null)
            editor = new WSDDEditor();
        else
            editor = new WSDDEditor(fileName);
        System.out.println("Using file " + editor.getWSDDFilename());
        return editor;
    }
}
