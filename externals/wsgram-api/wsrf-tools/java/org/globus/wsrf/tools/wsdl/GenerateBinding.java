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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.wsdl.Binding;
import javax.wsdl.BindingFault;
import javax.wsdl.BindingInput;
import javax.wsdl.BindingOperation;
import javax.wsdl.BindingOutput;
import javax.wsdl.Definition;
import javax.wsdl.Fault;
import javax.wsdl.Import;
import javax.wsdl.Input;
import javax.wsdl.Message;
import javax.wsdl.Operation;
import javax.wsdl.Port;
import javax.wsdl.PortType;
import javax.wsdl.Service;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.wsdl.xml.WSDLWriter;
import javax.xml.namespace.QName;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.util.AddressingUtils;

import org.globus.wsrf.tools.CommandLineTool;
import com.ibm.wsdl.extensions.PopulatedExtensionRegistry;
import com.ibm.wsdl.extensions.soap.SOAPAddressImpl;
import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import com.ibm.wsdl.extensions.soap.SOAPBodyImpl;
import com.ibm.wsdl.extensions.soap.SOAPFaultImpl;
import com.ibm.wsdl.extensions.soap.SOAPOperationImpl;

public class GenerateBinding
{
    private static String ADDRESS = "localhost:8080/wsrf/services/";
    private static String PROTOCOL = "http";
    public static final String SOAP_NS =
            "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String HTTP_NS = "http://schemas.xmlsoap.org/soap/http";
    private static Log logger = LogFactory.getLog(
            GenerateBinding.class.getName());

    public static void main(String[] args)
    {
        Definition portTypeDefinition = null;
        CommandLine commandLine = null;
        Options options = new Options();
        Option option = null;
        HelpFormatter formatter = new HelpFormatter();
        String portTypeFile = null;
        String fileRoot = null;
        Definition bindingDefinition = null;
        Definition serviceDefinition = null;

        option = new Option(
                "o", "out", true,
                "Specifies the filename prefix for the generated bindings files");
        option.setArgName("PREFIX");
        option.setRequired(true);
        options.addOption(option);
        option = new Option(
                "p", "protocol", true,
                "Specifies the protocol to bind to (default: http)");
        option.setArgName("PROTOCOL");
        options.addOption(option);
        option = new Option(
                "i", "in", true,
                "Specifies the file containing the porttype to process");
        option.setArgName("FILE");
        option.setRequired(true);
        options.addOption(option);
        options.addOption("h", "help", false, "Prints this help message");
        CommandLineParser parser = new GnuParser();

        try
        {
            commandLine = parser.parse(options, args);
        }
        catch(Exception e)
        {
            logger.error("", e);
            // internationalize
            formatter.printHelp(GenerateBinding.class.getName(), options, true);
            CommandLineTool.sysExit(1);
        }

        if(commandLine.hasOption('h'))
        {
            formatter.printHelp(GenerateBinding.class.getName(), options, true);
        }

        if(commandLine.hasOption('p'))
        {
            PROTOCOL = commandLine.getOptionValue('p');
        }

        portTypeFile = commandLine.getOptionValue('i');
        fileRoot = commandLine.getOptionValue('o');

        args = commandLine.getArgs();

        if(args.length != 0)
        {
            // internationalize
            formatter.printHelp(GenerateBinding.class.getName(),
                                options, false);
            CommandLineTool.sysExit(1);
        }

        FileOutputStream bindingDefintionOutput = null;
        FileOutputStream serviceDefinitionOutput = null;
        FileInputStream portTypeInput = null;

        try
        {
            portTypeFile = new File(portTypeFile).getCanonicalPath();
            portTypeInput = new FileInputStream(portTypeFile);
            WSDLFactory factory = WSDLFactory.newInstance();
            WSDLReader reader = factory.newWSDLReader();
            WSDLWriter writer = factory.newWSDLWriter();
            reader.setFeature("javax.wsdl.verbose", false);
            reader.setFeature("javax.wsdl.importDocuments", true);
            portTypeDefinition = reader.readWSDL(null, portTypeFile);
            bindingDefinition = factory.newDefinition();
            serviceDefinition = factory.newDefinition();
            if(portTypeDefinition.getQName() == null)
            {
                System.err.println(
                    "Missing 'name' attribute in 'definitions' element.");
                CommandLineTool.sysExit(1);
            }

            bindingDefinition.setQName(
                new QName(portTypeDefinition.getQName().getLocalPart()));
            serviceDefinition.setQName(
                new QName(portTypeDefinition.getQName().getLocalPart()));

            bindingDefinition.setTargetNamespace(
                    portTypeDefinition.getTargetNamespace() +
                    "/bindings");
            bindingDefinition.setExtensionRegistry(
                    new PopulatedExtensionRegistry());
            bindingDefinition.addNamespace("soap", SOAP_NS);
            bindingDefinition.addNamespace(
                "porttype",
                portTypeDefinition.getTargetNamespace());
            String bindingFile = fileRoot + "_bindings.wsdl";
            bindingDefintionOutput = new FileOutputStream(bindingFile);
            String relativePortTypeFile =
                    RelativePathUtil.getRelativeFileName(new File(portTypeFile),
                                                         new File(bindingFile));

            Import portTypeImport = bindingDefinition.createImport();
            portTypeImport.setLocationURI(relativePortTypeFile);
            portTypeImport.setNamespaceURI(
                    portTypeDefinition.getTargetNamespace());
            bindingDefinition.addImport(portTypeImport);
            serviceDefinition.setTargetNamespace(
                    portTypeDefinition.getTargetNamespace() +
                    "/service");
            serviceDefinition.setExtensionRegistry(
                    new PopulatedExtensionRegistry());
            serviceDefinition.addNamespace("soap", SOAP_NS);
            serviceDefinition.addNamespace(
                    "binding",
                    bindingDefinition.getTargetNamespace());
            String serviceFile = fileRoot + "_service.wsdl";
            serviceDefinitionOutput = new FileOutputStream(serviceFile);
            String relativeBindingFile =
                    RelativePathUtil.getRelativeFileName(new File(bindingFile),
                                                         new File(serviceFile));
            Import bindingImport = serviceDefinition.createImport();
            bindingImport.setLocationURI(relativeBindingFile);
            bindingImport.setNamespaceURI(
                    bindingDefinition.getTargetNamespace());
            serviceDefinition.addImport(bindingImport);
            Service service = serviceDefinition.createService();
            if(serviceDefinition.getQName().getLocalPart().endsWith("Service"))
            {
                service.setQName(serviceDefinition.getQName());
            }
            else
            {
                service.setQName(
                        new QName(serviceDefinition.getQName().getLocalPart()
                                  + "Service"));
            }
            Iterator portTypeIterator =
                    portTypeDefinition.getPortTypes().values().iterator();
            Binding binding;
            PortType portType;
            while(portTypeIterator.hasNext())
            {
                portType = (PortType) portTypeIterator.next();
                binding= processPortType(bindingDefinition,
                                         portType);
                bindingDefinition.addBinding(binding);
                service.addPort(createPort(serviceDefinition,
                                           portType,
                                           binding));
            }
            writer.writeWSDL(bindingDefinition, bindingDefintionOutput);
            serviceDefinition.addService(service);
            writer.writeWSDL(serviceDefinition, serviceDefinitionOutput);
        }
        catch(Exception e)
        {
            e.printStackTrace();
            CommandLineTool.sysExit(1);
        }
        finally
        {
            if(portTypeInput != null)
            {
                try
                {
                    portTypeInput.close();
                }
                catch(Exception io)
                {
                }
            }
        }
    }

    private static Port createPort(
            Definition serviceDefinition,
            PortType portType,
            Binding binding)
    {
        Port port = serviceDefinition.createPort();
        port.setName(portType.getQName().getLocalPart()
                     + "Port");
        port.setBinding(binding);
        SOAPAddress soapAddress = new SOAPAddressImpl();
        soapAddress.setLocationURI(PROTOCOL + "://" + ADDRESS);
        port.addExtensibilityElement(soapAddress);
        return port;
    }

    private static Binding processPortType(
        Definition bindingDefinition, PortType portType)
    {
        Binding binding = bindingDefinition.createBinding();
        binding.setPortType(portType);
        binding.setUndefined(false);
        binding.setQName(
                new QName(bindingDefinition.getTargetNamespace(),
                          portType.getQName().getLocalPart()
                          + "SOAPBinding"));
        SOAPBinding soapBinding = new SOAPBindingImpl();
        soapBinding.setTransportURI(HTTP_NS);
        soapBinding.setStyle("document");
        binding.addExtensibilityElement(soapBinding);
        Iterator operationIterator =
                portType.getOperations().iterator();
        BindingOperation bindingOperation;
        while(operationIterator.hasNext())
        {
            try
            {
                bindingOperation =
                processOperation(bindingDefinition,
                                 portType.getQName(),
                                 (Operation) operationIterator.next());
                binding.addBindingOperation(bindingOperation);
            }
            catch(Exception e)
            {
                System.err.println("Failed to add operation to binding");
                e.printStackTrace();
                CommandLineTool.sysExit(1);
            }
        }
        return binding;
    }

    private static BindingOperation processOperation(
        Definition bindingDefinition, QName portTypeQName,
        Operation operation)
        throws Exception
    {
        BindingOperation bindingOperation =
                bindingDefinition.createBindingOperation();
        bindingOperation.setOperation(operation);
        bindingOperation.setName(operation.getName());
        Input input = operation.getInput();
        if(input == null)
        {
            throw new Exception("Operation is lacking a <input> element");
        }
        Message inputMessage = input.getMessage();
        if(inputMessage == null)
        {
            throw new Exception("Input is lacking a <message> element");
        }
        List inputParts = inputMessage.getOrderedParts(null);
        if (inputParts == null || inputParts.isEmpty())
        {
            throw new Exception("No <part> element for input message: "  +
                                inputMessage.getQName());

        }
        SOAPOperation soapOperation = new SOAPOperationImpl();
        String action = AddressingUtils.getInputAction(portTypeQName,
                                                       operation);
        soapOperation.setSoapActionURI(action);
        bindingOperation.addExtensibilityElement(soapOperation);
        BindingInput bindingInput =
                bindingDefinition.createBindingInput();
        SOAPBody soapBody = new SOAPBodyImpl();
        soapBody.setUse("literal");
        bindingInput.addExtensibilityElement(soapBody);
        bindingOperation.setBindingInput(bindingInput);
        if(operation.getOutput() != null)
        {
            BindingOutput bindingOutput =
                    bindingDefinition.createBindingOutput();
            soapBody = new SOAPBodyImpl();
            soapBody.setUse("literal");
            bindingOutput.addExtensibilityElement(soapBody);
            bindingOperation.setBindingOutput(bindingOutput);
        }
        Iterator faulIterator =
                operation.getFaults().values().iterator();
        BindingFault bindingFault;
        while(faulIterator.hasNext())
        {
            bindingFault = processFault(
                    (Fault) faulIterator.next(),
                    bindingDefinition);
            bindingOperation.addBindingFault(bindingFault);
        }
        return bindingOperation;
    }

    private static BindingFault processFault(
            Fault fault, Definition bindingDefinition)
    {
        SOAPFault soapFault = new SOAPFaultImpl();
        soapFault.setName(fault.getName());
        soapFault.setUse("literal");
        BindingFault bindingFault =
                bindingDefinition.createBindingFault();
        bindingFault.addExtensibilityElement(soapFault);
        bindingFault.setName(fault.getName());
        return bindingFault;
    }
}
