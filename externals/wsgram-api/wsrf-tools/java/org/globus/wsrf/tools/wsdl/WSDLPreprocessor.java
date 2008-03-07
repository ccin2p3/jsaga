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
package org.globus.wsrf.tools.wsdl;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;

import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Output;
import javax.wsdl.PortType;
import javax.wsdl.Types;
import javax.wsdl.extensions.AttributeExtensible;
import javax.wsdl.extensions.ExtensionRegistry;
import javax.wsdl.extensions.UnknownExtensibilityElement;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.apache.xerces.dom.DOMInputImpl;
import org.apache.xerces.impl.xs.XMLSchemaLoader;
import org.apache.xerces.impl.xs.XSComplexTypeDecl;
import org.apache.xerces.xs.StringList;
import org.apache.xerces.xs.XSElementDeclaration;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.utils.XMLUtils;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.ls.LSInput;

import org.globus.wsrf.tools.CommandLineTool;

// TODO: need to i18n
public class WSDLPreprocessor extends CommandLineTool implements WSDLPreprocessorConstants
{
    private static int nameSpaceCounter = 0;
    private static boolean wsaImport = false;
    private static Log logger = LogFactory.getLog(WSDLPreprocessor.class.getName());
    private static boolean quiet = false;

    public static void main(String[] args)
    {
        QName portTypeName = null;
        String outFileName = null;
        String inFileName = null;
        CommandLine commandLine = null;
        WSDLFactory factory = null;
        Options options = new Options();
        Option option = null;
        HelpFormatter formatter = new HelpFormatter();
        PortType portType = null;
        Definition definition = null;
        FileOutputStream outFile = null;
        Map schemaDocumentLocations = new HashMap();

        try
        {
            factory = WSDLFactory.newInstance();
        }
        catch(Exception e)
        {
            String msg = "ERROR: Failed to load WSDL parser implementation";
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }

        WSDLReader reader = factory.newWSDLReader();
        WSDLWriter writer = factory.newWSDLWriter();

        // Don't import docs for now

        reader.setFeature("javax.wsdl.verbose", false);
        reader.setFeature("javax.wsdl.importDocuments", false);

        option = new Option("o", "out", true,
                            "Specifies the file to write the processed WSDL to (default STDOUT)");
        option.setArgName("FILE");
        options.addOption(option);
        option = new Option("p", "porttype", true,
                            "Specifies the top level porttype to process");
        option.setArgName("PORTTYPE");
        options.addOption(option);
        option = new Option("i", "in", true,
                            "Specifies the file containing the WSDL to process");
        option.setArgName("FILE");
        options.addOption(option);
        options.addOption("h", "help", false, "Prints this help message");
        options.addOption("q", "quiet", false, "Don't print warnings");
        CommandLineParser parser = new GnuParser();

        try
        {
            commandLine = parser.parse(options, args);
        }
        catch(Exception e)
        {
            logger.error("", e);
            formatter.printHelp(WSDLPreprocessor.class.getName(), options, true);
            sysExit(1);
        }

        if(commandLine.hasOption('h'))
        {
            formatter.printHelp(WSDLPreprocessor.class.getName(), options, true);
            sysExit(0);
        }

        if(commandLine.hasOption('p'))
        {
            portTypeName = new QName(commandLine.getOptionValue('p'));
        }

        inFileName = commandLine.getOptionValue('i');

        if(commandLine.hasOption('o'))
        {
            outFileName = commandLine.getOptionValue('o');
        }

        if(commandLine.hasOption('q'))
        {
            quiet = true;
        }

        args = commandLine.getArgs();

        if(args.length != 0)
        {
            // internationalize
            formatter.printHelp(WSDLPreprocessor.class.getName(), options, false);
            sysExit(1);
        }

        try
        {
            if(outFileName != null)
            {
                outFile = new FileOutputStream(outFileName);
            }
            else
            {
                outFile = new FileOutputStream(FileDescriptor.out);
            }
        }
        catch(Exception e)
        {
            String msg = "ERROR: Couldn't open output file " +
                outFileName == null ? "STDOUT" : outFileName;
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }

        try
        {
            definition = reader.readWSDL(null, inFileName);
        }
        catch(Exception e)
        {
            String msg = "ERROR: Couldn't read WSDL input file "
                + inFileName;
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }

        TypesProcessor typesProcessor = new TypesProcessor(definition);

        try
        {
            portType = typesProcessor.getPortType(portTypeName);
        }
        catch(Exception e)
        {
            String msg = "ERROR: Couldn't find "
                         + portTypeName == null ? "any" :
                         portTypeName.toString() + " porttype.";
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }
        portTypeName = portType.getQName();

        ExtensionRegistry extensionAttributes = new ExtensionRegistry();
        extensionAttributes.registerExtensionAttributeType(
            PortType.class,
            EXTENDS,
            AttributeExtensible.LIST_OF_QNAMES_TYPE);
        extensionAttributes.registerExtensionAttributeType(
            PortType.class, RP,
            AttributeExtensible.QNAME_TYPE);
        reader.setExtensionRegistry(extensionAttributes);

        try
        {
            if(!quiet)
            {
                reader.setFeature("javax.wsdl.verbose", true);
            }
            reader.setFeature("javax.wsdl.importDocuments", true);
            definition = reader.readWSDL(null, inFileName);
        }
        catch(Exception e)
        {
            String msg = "ERROR: Couldn't read WSDL input file "
                + inFileName;
            logger.error(msg, e);
            System.err.println(msg);
            sysExit(1);
        }

        typesProcessor = new TypesProcessor(definition);
        portType = definition.getPortType(portTypeName);
        List portTypeNames = getDependencies(portType);
        Map resourcePropertyElements = new HashMap();
        flatten(portType, definition, definition, portTypeNames,
                resourcePropertyElements,
                schemaDocumentLocations);


        if(!resourcePropertyElements.isEmpty())
        {
            try
            {
                typesProcessor.addResourceProperties(portTypeName,
                                                     resourcePropertyElements,
                                                     schemaDocumentLocations);
            }
            catch(Exception e)
            {
                String msg = "ERROR: Failed to add resource properties";
                logger.error(msg, e);
                System.err.println(msg);
                System.err.println(e.toString());
                sysExit(2);
            }
        }

        if(wsaImport)
        {
            try
            {
                typesProcessor.addWSAImport(schemaDocumentLocations);
            }
            catch(Exception e)
            {
                String msg = "ERROR: Failed to add WSA XSD import";
                logger.error(msg, e);
                System.err.println(msg);
                System.err.println(e.toString());
                sysExit(2);
            }
        }

        try
        {
            writer.writeWSDL(definition, outFile);
        }
        catch(Exception e)
        {
            String msg = "ERROR: Failed to process WSDL";
            logger.error(msg, e);
            System.err.println(msg);
            System.err.println(e.toString());
            sysExit(3);
        }
    }

    private static PortType flatten(
        PortType portType,
        Definition definition,
        Definition parentDefinition,
        List portTypeNames,
        Map resourcePropertyElements,
        Map schemaDocumentLocations)
    {
        Map portTypes = definition.getPortTypes();
        Iterator portTypeIterator = portTypes.values().iterator();

        while(portTypeIterator.hasNext())
        {
            PortType currentPortType = (PortType) portTypeIterator.next();

            if(portTypeNames != null
               && portTypeNames.contains(currentPortType.getQName()))
            {
                logger.debug("Found porttype: " +
                             currentPortType.getQName());

                QName resourceProperties = (QName)
                    currentPortType.getExtensionAttribute(RP);

                if(resourceProperties != null)
                {
                    logger.debug("Adding resource properties for porttype: " +
                                 currentPortType.getQName());
                    resourcePropertyElements.putAll(
                        getResourceProperties(resourceProperties,
                                              definition,
                                              schemaDocumentLocations));
                }

                List newDependencies = getDependencies(currentPortType);

                if(newDependencies != null && !newDependencies.isEmpty())
                {
                    flatten(currentPortType, definition, parentDefinition,
                            newDependencies,
                            resourcePropertyElements,
                            schemaDocumentLocations);
                    if(!newDependencies.isEmpty() &&
                       definition != parentDefinition)
                    {
                        System.err.println(
                            "WARNING: The following porttypes are missing:");
                        Iterator portTypeNamesIterator =
                            newDependencies.iterator();
                        while(portTypeNamesIterator.hasNext())
                        {
                            System.err.println(
                                "\t" + ((QName) portTypeNamesIterator.next()).toString());
                        }
                    }
                }

                fixNameSpaces(currentPortType, parentDefinition);
                List operations = currentPortType.getOperations();
                ListIterator operationsIterator = operations.listIterator();
                Operation operation;
                Input input;
                Output output;

                while(operationsIterator.hasNext())
                {
                    operation = (Operation) operationsIterator.next();
                    input = operation.getInput();
                    output = operation.getOutput();

                    if(portType.getOperation(
                        operation.getName(),
                        input == null ? null : input.getName(),
                        output == null ? null : output.getName()) == null)
                    {
                        if (input != null &&
                            input.getExtensionAttribute(WSA_ACTION) != null) {
                            wsaImport = true;
                            Element schema = getSchemaElement(definition);
                            if (schema != null) {
                                XSModel schemaModel = loadSchema(schema, definition);
                                populateLocations(schemaModel,
                                                  schemaDocumentLocations,
                                                  definition);
                            }
                        }
                        portType.addOperation(operation);
                    }
                }

                addImports(definition, parentDefinition);

                portTypeNames.remove(currentPortType.getQName());
            }
        }

        if(portTypeNames == null || portTypeNames.isEmpty())
        {
            return portType;
        }
        else
        {
            // Only go to immediate imports - nested imports are not processed
            if(definition == parentDefinition)
            {
                Map imports = new HashMap();
                imports.putAll(definition.getImports());
                Iterator importNSIterator = imports.values().iterator();

                while(importNSIterator.hasNext() && !portTypeNames.isEmpty())
                {
                    Vector importVector = (Vector) importNSIterator.next();
                    Iterator importIterator = importVector.iterator();
                    while(importIterator.hasNext() && !portTypeNames.isEmpty())
                    {
                        // Name space?
                        // I think the rule is to set the target name space to the
                        // namespace
                        // specified by import statement if not already defined
                        Import importDef = (Import) importIterator.next();
                        flatten(portType, importDef.getDefinition(),
                                parentDefinition, portTypeNames,
                                resourcePropertyElements,
                                schemaDocumentLocations);
                    }
                }

                if(!portTypeNames.isEmpty() && !quiet)
                {
                    System.err.println(
                        "WARNING: The following porttypes are missing:");
                    Iterator portTypeNamesIterator = portTypeNames.iterator();
                    while(portTypeNamesIterator.hasNext())
                    {
                        System.err.println(
                            "\t" +
                            ((QName) portTypeNamesIterator.next()).toString());
                    }
                }
            }

            return portType;
        }
    }

    private static void addImports(
        Definition definition, Definition parentDefinition)
    {
        Collection imports = definition.getImports().values();
        Iterator importsIterator = imports.iterator();

        while(importsIterator.hasNext())
        {
            Vector importsVector = (Vector) importsIterator.next();
            Iterator importsVectorIterator = importsVector.iterator();
            boolean addImport = true;
            while(importsVectorIterator.hasNext())
            {
                Import currentImport =
                    (Import) importsVectorIterator.next();
                String location = currentImport.getLocationURI();
                if(location != null && (location.startsWith(".") ||
                                        location.indexOf('/') == -1))
                {
                    location =
                        getRelativePath(
                            parentDefinition.getDocumentBaseURI(),
                            currentImport.getDefinition().getDocumentBaseURI());
                    currentImport.setLocationURI(location);
                }
                List parentImports = parentDefinition.getImports(
                    currentImport.getNamespaceURI());
                if(parentImports != null)
                {
                    Iterator parentImportsIterator = parentImports.iterator();
                    while(parentImportsIterator.hasNext())
                    {
                        Import parentImport =
                            (Import) parentImportsIterator.next();
                        if(parentImport.getLocationURI().equals(location))
                        {
                            addImport = false;
                            break;
                        }
                    }
                }
                if(addImport)
                {
                    parentDefinition.addImport(currentImport);
                }
                addImport = true;
            }
        }
    }

    private static List getDependencies(PortType portType)
    {
        List result = (List) portType.getExtensionAttribute(EXTENDS);

        if(result != null)
        {
            portType.getExtensionAttributes().remove(EXTENDS);
        }

        return result;
    }

    private static Element getSchemaElement(Definition def)
    {
        Types types = def.getTypes();
        if(types == null)
        {
            return null;
        }
        List elementList = types.getExtensibilityElements();
        if (elementList.isEmpty())
        {
            return null;
        }
        else
        {
            return ((UnknownExtensibilityElement)elementList.get(0)).getElement();
        }
    }

    private static XSModel loadSchema(Element schema, Definition def)
    {
        // add namespaces from definition element
        Map definitionNameSpaces = def.getNamespaces();
        Set nameSpaces = definitionNameSpaces.entrySet();
        Iterator nameSpacesIterator = nameSpaces.iterator();

        while(nameSpacesIterator.hasNext())
        {
            Entry nameSpaceEntry = (Entry) nameSpacesIterator.next();
            if ( !"".equals((String) nameSpaceEntry.getKey()) &&
                 !schema.hasAttributeNS("http://www.w3.org/2000/xmlns/",
                                        (String) nameSpaceEntry.getKey()))
             {
                 Attr nameSpace =
                     schema.getOwnerDocument().createAttributeNS(
                            "http://www.w3.org/2000/xmlns/",
                            "xmlns:" + nameSpaceEntry.getKey());
                 nameSpace.setValue((String) nameSpaceEntry.getValue());
                 schema.setAttributeNode(nameSpace);
             }
        }

        LSInput schemaInput = new DOMInputImpl();
        schemaInput.setStringData(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + XMLUtils.ElementToString(schema));
        logger.debug("Loading schema in types section of definition " +
                     def.getDocumentBaseURI());
        schemaInput.setSystemId(def.getDocumentBaseURI());
        XMLSchemaLoader schemaLoader = new XMLSchemaLoader();
        XSModel schemaModel = schemaLoader.load(schemaInput);
        logger.debug("Done loading");
        return schemaModel;
    }

    private static void populateLocations(XSModel schemaModel,
                                          Map documentLocations,
                                          Definition def) {
        XSNamespaceItemList namespaceItemList =
            schemaModel.getNamespaceItems();
        for(int i = 0; i < namespaceItemList.getLength(); i++)
        {
            XSNamespaceItem namespaceItem = namespaceItemList.item(i);
            Map locations = (Map) documentLocations.get(
                                  namespaceItem.getSchemaNamespace());
            if(locations == null)
            {
                locations = new HashMap();
                documentLocations.put(namespaceItem.getSchemaNamespace(),
                                      locations);
            }

            StringList list = namespaceItem.getDocumentLocations();
            for(int j = 0; j < list.getLength(); j++)
            {
                if(!list.item(j).equals(def.getDocumentBaseURI()))
                {
                    locations.put(list.item(j), null);
                }
            }
        }
    }

    public static Map getResourceProperties(
        QName resourceProperties,
        Definition def,
        Map documentLocations)
    {
        HashMap resourcePropertyElements = new HashMap();

        if(resourceProperties != null)
        {
            Types types = def.getTypes();
            if(types == null)
            {
                if(!quiet)
                {
                    System.err.println(
                        "WARNING: The " + def.getDocumentBaseURI()
                        + " definition does not have a types section");
                }
                return resourcePropertyElements;
            }

            List elementList = types.getExtensibilityElements();
            // assume only on schema element for now
            if(elementList.size() > 1)
            {
                if(!quiet)
                {
                    System.err.println(
                        "WARNING: The types section in "
                        + def.getDocumentBaseURI()
                        + " contains more than one top level element.");
                }
                // maybe throw error
            }
            Element schema =
                ((UnknownExtensibilityElement)
                elementList.get(0)).getElement();

            XSModel schemaModel = loadSchema(schema, def);

            XSElementDeclaration resourcePropertiesElement =
                schemaModel.getElementDeclaration(
                    resourceProperties.getLocalPart(),
                    resourceProperties.getNamespaceURI());

            if(resourcePropertiesElement != null)
            {
                XSComplexTypeDecl type =
                    (XSComplexTypeDecl)
                        resourcePropertiesElement.getTypeDefinition();
                XSParticle particle = type.getParticle();
                XSModelGroup sequence = (XSModelGroup) particle.getTerm();
                XSObjectList objectList = sequence.getParticles();

                for(int i = 0; i < objectList.getLength(); i++)
                {
                    XSParticle part = (XSParticle) objectList.item(i);
                    Object term = part.getTerm();
                    if (term instanceof XSElementDeclaration) {
                        XSElementDeclaration resourceProperty =
                            (XSElementDeclaration)term;
                        resourcePropertyElements.put(
                            new QName(resourceProperty.getNamespace(),
                                      resourceProperty.getName()), part);
                    }
                    else
                    {
                        System.err.println(
                            "ERROR: Invalid resource properties document "
                            + resourceProperties.toString());
                        sysExit(1);
                    }
                }
            }
            else
            {
                Map imports = def.getImports();
                Iterator importNSIterator = imports.values().iterator();
                while(importNSIterator.hasNext()
                      && resourcePropertyElements.isEmpty())
                {
                    Vector importVector = (Vector) importNSIterator.next();
                    Iterator importIterator = importVector.iterator();
                    while(importIterator.hasNext()
                          && resourcePropertyElements.isEmpty())
                    {
                        Import importDef = (Import) importIterator.next();
                        // process imports
                        resourcePropertyElements.putAll(getResourceProperties(
                            resourceProperties, importDef.getDefinition(),
                            documentLocations));
                    }
                }

                if(resourcePropertyElements.isEmpty())
                {
                    // throw error
                    System.err.println(
                        "ERROR: Unable to resolve resource properties "
                        + resourceProperties.toString());
                    //TODO: This is bad, should clean up and throw exception instead
                    sysExit(1);
                }
            }

            populateLocations(schemaModel, documentLocations, def);
        }
        return resourcePropertyElements;
    }

    private static void fixNameSpaces(
        PortType porttype, Definition definition)
    {
        List operations = porttype.getOperations();
        Iterator operationsIterator = operations.iterator();

        while(operationsIterator.hasNext())
        {
            Operation operation = (Operation) operationsIterator.next();
            Input input = operation.getInput();
            if(input != null)
            {
                Message inMessage = input.getMessage();

                if(inMessage != null &&
                   definition.getPrefix(
                       inMessage.getQName().getNamespaceURI()) == null)
                {
                    definition.addNamespace(
                        "gtwsdl"
                        + String.valueOf(
                            nameSpaceCounter++),
                        inMessage.getQName().getNamespaceURI());
                }
            }

            Output output = operation.getOutput();

            if(output != null)
            {
                Message outMessage = output.getMessage();
                if(outMessage != null &&
                   definition.getPrefix(
                       outMessage.getQName().getNamespaceURI()) == null)
                {
                    definition.addNamespace(
                        "gtwsdl"
                        + String.valueOf(
                            nameSpaceCounter++),
                        outMessage.getQName().getNamespaceURI());
                }
            }

            Map faults = operation.getFaults();

            if(faults != null)
            {
                Iterator faultIterator = faults.values().iterator();
                while(faultIterator.hasNext())
                {
                    Message faultMessage =
                        ((Fault) faultIterator.next()).getMessage();
                    if(definition.getPrefix(
                        faultMessage.getQName().getNamespaceURI()) == null)
                    {
                        definition.addNamespace(
                            "gtwsdl"
                            + String.valueOf(
                                nameSpaceCounter++),
                            faultMessage.getQName().getNamespaceURI());
                    }
                }
            }
        }
    }

    //Todo: move to utility class?

    protected static String getRelativePath(
        String srcPathURI, String destPathURI)
    {
        String destPath = destPathURI.substring(5);
        String srcPath = srcPathURI.substring(5);
        return RelativePathUtil.getRelativeFileName(new File(destPath),
                                                    new File(srcPath));
    }

}
