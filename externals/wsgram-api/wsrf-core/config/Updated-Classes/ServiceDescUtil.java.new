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
package org.globus.axis.description;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.io.File;

import javax.wsdl.Binding;
import javax.wsdl.Operation;
import javax.wsdl.OperationType;
import javax.wsdl.PortType;
import javax.wsdl.Fault;
import javax.wsdl.Part;
import javax.wsdl.Message;
import javax.wsdl.WSDLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
import org.apache.axis.description.JavaServiceDesc;
import org.apache.axis.description.OperationDesc;
import org.apache.axis.description.ParameterDesc;
import org.apache.axis.description.ServiceDesc;
import org.apache.axis.description.FaultDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.constants.Style;
import org.apache.axis.constants.Use;
import org.apache.axis.constants.Scope;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.providers.java.JavaProvider;
import org.apache.axis.utils.cache.ClassCache;
import org.apache.axis.utils.cache.JavaClass;
import org.apache.axis.wsdl.symbolTable.BindingEntry;
import org.apache.axis.wsdl.symbolTable.Parameter;
import org.apache.axis.wsdl.symbolTable.Parameters;
import org.apache.axis.wsdl.symbolTable.SymbolTable;
import org.apache.axis.wsdl.symbolTable.TypeEntry;
import org.apache.axis.wsdl.toJava.JavaGeneratorFactory;
import org.apache.axis.message.addressing.handler.AddressingHandler;
import org.apache.axis.message.addressing.util.AddressingUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.globus.axis.providers.RPCProvider;
import org.globus.util.I18n;
import org.globus.wsrf.Constants;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.container.ServiceManager;

import javax.xml.namespace.QName;

/**
 * This class updates the Java service description with information retreived
 * from the wsdl file. It also updates the soap address location url in the
 * wsdl file.
 */
public class ServiceDescUtil {

    public static final String PROVIDER_MAPPING =
        "providerMapping";

    public static final String PROVIDERS_OPTION =
        "providers";

    private static final String INITIALIZED =
        "org.globus.wsrf.axis.servicedesc.initialized";

    public static final String ALLOWED_METHODS_CLASS =
        "allowedMethodsClass";

    public static final String ACTION_MAP =
        "addressing.action.map";

    static Log logger =
        LogFactory.getLog(ServiceDescUtil.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public static void resetOperations(MessageContext msgContext)
        throws AxisFault {
	// first ensure service is initialized
        try {
            ServiceManager.initializeService(msgContext);
        } catch (AxisFault e) {
            throw e;
        } catch (Exception e) {
        	//TODO
        	if(e.getMessage() == null)
        		System.err.println("Patch ND in org.globus.axis.description.ServiceDescUtil.resetOperations()");
        	else
        		throw AxisFault.makeFault(e);
        }
	// then reset operations
        AddressingHandler.resetContextOperations(msgContext);
    }

    public static void initializeService(MessageContext msgContext)
        throws Exception {
        SOAPService service = msgContext.getService();
        if (service == null) {
            return;
        }
        ServiceDesc serviceDesc =
            service.getInitializedServiceDesc(msgContext);
        synchronized (serviceDesc) {
            if (serviceDesc.getProperty(INITIALIZED) != null) {
                return;
            }

            //TODO: reserialize is never used?
            boolean reserialize = false;
            if (initializeProviders(msgContext, service, serviceDesc)) {
                reserialize = true;
            }
            updateAllowedMethods(msgContext, service, serviceDesc);
            if (fixNamespaces(msgContext, service, serviceDesc)) {
                reserialize = true;
            }

            serviceDesc.setProperty(INITIALIZED, Boolean.TRUE);
        }
    }

    public static boolean initializeProviders(MessageContext msgCtx,
                                              SOAPService service,
                                              ServiceDesc serviceDesc)
        throws Exception {
        boolean operDescModified = false;

        boolean rightHandler =
            (service.getPivotHandler() instanceof RPCProvider);

        String providers = (String)service.getOption(PROVIDERS_OPTION);
        if (providers == null) {
            if (rightHandler) {
                // fall through
            } else {
                return operDescModified;
            }
        } else {
            if (rightHandler) {
                // fall through
            } else {
                throw new Exception(
                i18n.getMessage(
                    "invalidPivotHandler",
                    new Object[] {service.getName(),
                                  RPCProvider.class.getName()}));
            }
        }

        String scopeOpt = (String)service.getOption(JavaProvider.OPTION_SCOPE);
        Scope scope = Scope.getScope(scopeOpt, Scope.DEFAULT);
        if (scope != Scope.APPLICATION && scope != Scope.REQUEST) {
            throw new Exception(
                i18n.getMessage(
                    "invalidScope",
                    new Object[] {service.getName(),
                                  Scope.APPLICATION_STR,
                                  Scope.REQUEST_STR}));
        }

        AxisEngine engine = msgCtx.getAxisEngine();

        if (scope == Scope.APPLICATION) {
            Class serviceClass = ((JavaServiceDesc)serviceDesc).getImplClass();
            Object serviceInstance =
                RPCProvider.getNewServiceInstance(msgCtx, serviceClass);
            engine.getApplicationSession().set(serviceDesc.getName(),
                                               serviceInstance);
        }

        if (providers == null) {
            return operDescModified;
        }

        WSDDGlobalConfiguration globalConfig = null;

        EngineConfiguration config = engine.getConfig();
        if (config instanceof WSDDEngineConfiguration) {
            WSDDDeployment deployment =
                ((WSDDEngineConfiguration)config).getDeployment();
            globalConfig = deployment.getGlobalConfiguration();
        }

        ClassCache cache = engine.getClassCache();
        ClassLoader cl = msgCtx.getClassLoader();

        JavaServiceDesc providerDesc;
        String provider;
        Class providerClass;
        Map mapping = null;
        JavaClass jc;
        StringTokenizer tokens = new StringTokenizer(providers);

        while(tokens.hasMoreTokens()) {
            provider = tokens.nextToken();

            // lookup global paramter for this provider name
            if (globalConfig != null) {
                String tmp  = globalConfig.getParameter(provider);
                if (tmp != null) {
                    provider = tmp.trim();
                }
            }

            // load the class and introspect it
            jc = cache.lookup(provider, cl);
            providerClass = jc.getJavaClass();
            providerDesc = new JavaServiceDesc();
            providerDesc.loadServiceDescByIntrospection(providerClass);

            List operations = providerDesc.getOperations();
            Iterator iter = operations.iterator();
            String operationName = null;
            Object providerObj = null;
            while(iter.hasNext()) {
                OperationDesc operation = (OperationDesc)iter.next();
                operationName = operation.getName();
                if (serviceDesc.getOperationsByName(operationName) == null) {

                    if (logger.isDebugEnabled()) {
                        logger.debug("Added operation '" + operationName +
                                     "' for " + serviceDesc.getName() +
                                     " service");
                    }

                    serviceDesc.addOperationDesc(operation);

                    if (mapping == null) {
                        mapping = new HashMap();
                        // set mapping
                        service.setOption(PROVIDER_MAPPING,
                                          mapping);
                    }

                    if (providerObj == null) {
                        // this ensures that only one instance of the provider
                        // is created even though multiple operations of the
                        // same provider are added
                        if (scope == Scope.APPLICATION) {
                            // for application scope create instance
                            providerObj =
                                RPCProvider.getNewServiceInstance(msgCtx,
                                                                  providerClass);
                            String key = serviceDesc.getName() + "/" +
                                providerObj.hashCode();
                            engine.getApplicationSession().set(key, providerObj);
                        } else {
                            // for request scope just pass class
                            providerObj = providerClass;
                        }
                    }

                    mapping.put(operation, providerObj);
                }
            }
        }

        operDescModified = (mapping != null);

        return operDescModified;
    }

    public static boolean fixNamespaces(MessageContext msgCtx,
                                        SOAPService service,
                                        ServiceDesc serviceDesc)
        throws Exception {
        
        boolean operDescModified = false;

        if (serviceDesc.getWSDLFile() == null) {
            return operDescModified;
        }

        if (!(serviceDesc.getStyle() == Style.DOCUMENT &&
              serviceDesc.getUse() == Use.LITERAL)) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not a doc/lit service: " + 
                             serviceDesc.getName());
            }
            return operDescModified;
        }

        service.generateWSDL(msgCtx);
        Document wsdlDoc = (Document)msgCtx.getProperty("WSDL");
        if (wsdlDoc == null) {
            return operDescModified;
        }
        msgCtx.removeProperty("WSDL");

        AxisEngine engine = msgCtx.getAxisEngine();
        ClassCache cache = engine.getClassCache();
        ClassLoader cl = msgCtx.getClassLoader();

        if (logger.isDebugEnabled()) {
            logger.debug("Fixing namespaces for: " + serviceDesc.getName());
        }
        SymbolTable table =
            new SymbolTable(new JavaGeneratorFactory().getBaseTypeMapping(),
                            true,
                            logger.isDebugEnabled(),
                            !serviceDesc.isWrapped());

        table.setQuiet(!logger.isDebugEnabled());

        File wsdlFile = new File(serviceDesc.getWSDLFile());
        if (!wsdlFile.exists()) {
            String homeDir = (String)msgCtx.getProperty(org.apache.axis.Constants.MC_HOME_DIR);
            if (homeDir == null) {
                homeDir = ".";
            }
            wsdlFile = new File(homeDir, serviceDesc.getWSDLFile());
        }

        //TODO : ND return java.lang.NullPointerException
        table.populate(wsdlFile.getAbsolutePath(), wsdlDoc);

        HashMap symbolMap = table.getHashMap();

        Iterator symbols = symbolMap.values().iterator();

        boolean hasBinding = false;

        while (symbols.hasNext()) {
            Vector symbolVector = (Vector) symbols.next();

            for (int i = 0; i < symbolVector.size(); ++i) {
                if (symbolVector.get(i) instanceof BindingEntry) {
                    hasBinding = true;

                    BindingEntry bindingEntry =
                        (BindingEntry) symbolVector.get(i);

                    Binding binding = bindingEntry.getBinding();

                    PortType portType = binding.getPortType();

                    Iterator operations = portType.getOperations().iterator();

                    // get parameters
                    while (operations.hasNext()) {
                        Operation operation = (Operation) operations.next();

                        String opName = operation.getName();

                        // for the operation defined in wsdl get the service
                        // operation description
                        OperationDesc operationDesc =
                            serviceDesc.getOperationByName(opName);

                        if (operationDesc == null &&
                            Character.isUpperCase(opName.charAt(0))) {
                            opName = Character.toLowerCase(opName.charAt(0)) +
                                opName.substring(1);
                            logger.debug("Trying: " + opName);
                            operationDesc =
                                serviceDesc.getOperationByName(opName);
                        }

                        if (operationDesc == null) {
                            throw new Exception(
                                   i18n.getMessage(
                                         "missingOperation",
                                         new Object[] { operation.getName(),
                                                        serviceDesc.getName() }
                                   )
                            );
                        }

                        // creates WS-Addressing reply Action Operation mapping
                        createWSAActionMapping(serviceDesc,
                                               operationDesc,
                                               portType.getQName(),
                                               operation);

                        Parameters params =
                            bindingEntry.getParameters(operation);

                        if (params.list.size() > 1) {
                            throw new Exception(
                                i18n.getMessage("invalidNumberOfParams"));
                        }

                        for (int j=0;j<params.list.size();j++) {
                            Parameter param = (Parameter)params.list.get(j);

                            if (param.getMode() == Parameter.IN) {
                                // fix the qname
                                // qname of the parameter in the service
                                // description will match the qname
                                // in service wsdl
                                ParameterDesc paramDesc =
                                    operationDesc.getParameter(j);
                                if (paramDesc != null) {
                                    paramDesc.setQName(param.getQName());
                                    operDescModified = true;
                                }
                            }

                        }

                        // fix element names for Faults
                        fixFaults(serviceDesc,
                                  operationDesc,
                                  cache,
                                  cl,
                                  operation,
                                  table);

                        if (params.returnParam != null) {
                            operationDesc.setReturnQName(
                                params.returnParam.getQName());
                        }

                        QName operationName =
                            new QName(portType.getQName().getNamespaceURI(),
                                      opName);
                        operationDesc.setElementQName(operationName);

                        if (operation.getStyle() == OperationType.ONE_WAY) {
                            operationDesc.setMep(OperationType.ONE_WAY);
                        }
                    }
                }
            }
        }

        if (!hasBinding) {
             throw new WSDLException(
                         WSDLException.OTHER_ERROR,
                         i18n.getMessage("noWSDLBinding",
                                         new Object[] {serviceDesc.getName()})
             );
        }

        return operDescModified;
    }

    private static void createWSAActionMapping(ServiceDesc serviceDesc,
                                               OperationDesc operationDesc,
                                               QName portTypeQName,
                                               Operation operation)
        throws Exception {
        String action = AddressingUtils.getOutputAction(portTypeQName,
                                                        operation);
        Map actionMap = (Map)serviceDesc.getProperty(ACTION_MAP);
        if (actionMap == null) {
            actionMap = new HashMap();
            serviceDesc.setProperty(ACTION_MAP, actionMap);
        }
        actionMap.put(operationDesc, action);
    }

    private static void fixFaults(ServiceDesc serviceDesc,
                                  OperationDesc operationDesc,
                                  ClassCache cache,
                                  ClassLoader cl,
                                  Operation operation,
                                  SymbolTable table)
        throws Exception {
        Map faultMap = new HashMap();

        // step 1: get xml types of faults associated with the
        // operation and create a fault map of xmlType->FaultDesc
        List list = operationDesc.getFaults();
        if (list != null) {
            JavaClass jc = null;
            TypeDesc typeDesc = null;
            Iterator iter = list.iterator();
            while (iter.hasNext()) {
                FaultDesc fault = (FaultDesc)iter.next();
                QName xmlType = fault.getXmlType();
                if (xmlType == null) {
                    jc = cache.lookup(fault.getClassName(), cl);
                    typeDesc = TypeDesc.getTypeDescForClass(jc.getJavaClass());
                    if (typeDesc != null) {
                        xmlType = typeDesc.getXmlType();
                        if (xmlType == null) {
                            logger.warn(i18n.getMessage("typeDescNoXmlType",
                                                        jc.getJavaClass()));
                            continue;
                        }
                    } else {
                        // no TypeDesc try basic name lookup
                        faultMap.put(fault.getName(), fault);
                        logger.debug(i18n.getMessage("noFaultTypeDesc",
                                                     jc.getJavaClass()));
                        continue;
                    }
                    // we could also set the fault.setXmlType()
                }
                faultMap.put(xmlType, fault);
            }
        }

        // step 2: update xml element names for FaultDesc
        // classes using the fault map generated in step 1
        Map faults = operation.getFaults();
        Iterator iter = faults.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry entry = (Map.Entry)iter.next();
            Fault fault = (Fault)entry.getValue();
            Message message = fault.getMessage();
            if (message == null) {
                logger.warn(
                 i18n.getMessage("noFaultMessage",
                                 new Object[] { entry.getKey(),
                                                serviceDesc.getName() })
                );
                continue;
            }
            Map parts = message.getParts();
            if (parts == null || parts.size() != 1) {
                logger.warn(
                 i18n.getMessage("invalidFaultPart",
                                 new Object[] { entry.getKey(),
                                                serviceDesc.getName() })
                );
                continue;
            }
            Part part = (Part)parts.values().iterator().next();
            QName elementQName = part.getElementName();
            if (elementQName == null) {
                logger.warn(
                 i18n.getMessage("missingElementAttribute",
                                 new Object[] { entry.getKey(),
                                                serviceDesc.getName() })
                );
                continue;
            }
            TypeEntry elementTypeEntry =
                table.getElement(elementQName);
            if (elementTypeEntry == null) {
                logger.warn(
                 i18n.getMessage("missingElementFault",
                                 new Object[] { entry.getKey(),
                                                serviceDesc.getName() })
                );
                continue;
            }
            TypeEntry typeEntry =
                elementTypeEntry.getRefType();
            if (typeEntry == null) {
                logger.warn(
                 i18n.getMessage("missingTypeFault",
                                 new Object[] { entry.getKey(),
                                                serviceDesc.getName() })
                );
                continue;
            }
            QName typeQName = typeEntry.getQName();
            if (typeQName == null) {
                throw new Exception(i18n.getMessage("noTypeQName"));
            }
            FaultDesc faultDesc = (FaultDesc)faultMap.get(typeQName);
            if (faultDesc == null) {
                // Try matching on the fault message name
                faultDesc = 
                    (FaultDesc)faultMap.get(message.getQName().getLocalPart());
                if (faultDesc == null) {
                    logger.warn(
                                i18n.getMessage("missingFault",
                                      new Object[] { elementQName,
                                                     operationDesc.getName(),
                                                     serviceDesc.getName() })
                                );
                    
                    continue;
                }
            }

            logger.debug("Updated fault qname '" + elementQName +
                         "' for operation '" + operationDesc.getName() +
                         "'");
            faultDesc.setQName(elementQName);
        }
    }

    public static void updateWSDL(MessageContext ctx)
        throws Exception {
        updateWSDLSOAPAddress(ctx);
        updateWSDLImport(ctx);
    }

    public static void updateWSDLImport(MessageContext ctx)
        throws Exception {
        Document doc = (Document)ctx.getProperty("WSDL");
        if (doc == null) {
            return;
        }
        SOAPService service = ctx.getService();
        if (service == null) {
            return;
        }
        ServiceDesc serviceDesc =
            service.getInitializedServiceDesc(ctx);

        NodeList nodes =
            doc.getElementsByTagNameNS(Constants.WSDL_NS, "import");
        
        int len = nodes.getLength();
        if (len > 0) {
            String wsdlURL =
                ContainerConfig.getExternalWebRoot(ctx) +
                serviceDesc.getWSDLFile();
            wsdlURL = wsdlURL.substring(0, wsdlURL.lastIndexOf('/'));
            
            for (int i = 0; i < nodes.getLength(); i++) {
                Element importElement = (Element) nodes.item(i);
                String location = importElement.getAttribute("location");

                if (location == null) {
                    throw new WSDLException(
                           WSDLException.OTHER_ERROR,
                           i18n.getMessage("noWSDLImportLocationError")
                    );
                }

                // fix relative imports
                if (location.indexOf("://") == -1) {
                    String wsdlDir = wsdlURL;
                    int subPath = location.indexOf("../");

                    while (subPath != -1) {
                        location = location.substring(subPath + 3);
                        wsdlDir = wsdlDir.substring(0, 
                                                    wsdlDir.lastIndexOf('/'));
                        subPath = location.indexOf("../");
                    }
                    
                    importElement.setAttribute("location",
                                               wsdlDir + "/" + location);
                }
            }
        }
    }
    
    public static void updateWSDLSOAPAddress(MessageContext ctx)
        throws Exception {
        Document doc = (Document)ctx.getProperty("WSDL");
        if (doc == null) {
            return;
        }
        NodeList nodes =
            doc.getElementsByTagNameNS(Constants.WSDL_SOAP_NS, "address");

        if (nodes.getLength() == 0) {
            throw new WSDLException(WSDLException.OTHER_ERROR,
                                    i18n.getMessage("noSOAPAddressError"));
        }

        AxisEngine engine = ctx.getAxisEngine();
        ContainerConfig config = ContainerConfig.getConfig(engine);

        String servicePath = ctx.getTargetService();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element address = (Element) nodes.item(i);

            URL url = new URL(ServiceHost.getProtocol(ctx),
                              ServiceHost.getHost(ctx),
                              ServiceHost.getPort(ctx),
                              "/" + config.getWSRFLocation() + servicePath);

            address.setAttribute("location", url.toExternalForm());
        }
    }

    public static void updateAllowedMethods(MessageContext msgCtx,
                                            SOAPService service,
                                            ServiceDesc serviceDesc)
        throws Exception {
        List allowedOperations = null;

        // allowed operation set already
        if (serviceDesc.getAllowedMethods() != null) {
            allowedOperations = serviceDesc.getAllowedMethods();
        } else {
            // null if the 'allowedMethods' parameter is set to '*'
            if (service.getOption(JavaProvider.OPTION_ALLOWEDMETHODS) != null) {
                return;
            } else {
                allowedOperations = getAllowedOperations(msgCtx,
                                                         service,
                                                         serviceDesc);
                if (allowedOperations == null) {
                    return;
                }
            }
        }

        List removedOperations = new ArrayList();
        List operations = serviceDesc.getOperations();
        Iterator iter = operations.iterator();
        while(iter.hasNext()) {
            OperationDesc operation = (OperationDesc)iter.next();
            if (!allowedOperations.contains(operation.getName())) {
                removedOperations.add(operation);
                if (logger.isDebugEnabled()) {
                    logger.debug("operation '" + operation.getName() +
                                 "' disallowed for service '" +
                                 service.getName() + "'");
                }
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("operation '" + operation.getName() +
                                 "' allowed for service '" +
                                 service.getName() + "'");
                }
            }
        }

        iter = removedOperations.iterator();
        while(iter.hasNext()) {
            serviceDesc.removeOperationDesc((OperationDesc)iter.next());
        }
    }

    protected static List getAllowedOperations(MessageContext msgCtx,
                                               SOAPService service,
                                               ServiceDesc serviceDesc)
        throws Exception {
        String allowedClass =
            (String)service.getOption(ALLOWED_METHODS_CLASS);
        if (allowedClass == null) {
            return null;
        }

        AxisEngine engine = msgCtx.getAxisEngine();
        ClassCache cache = engine.getClassCache();
        ClassLoader cl = msgCtx.getClassLoader();
        JavaClass jc = cache.lookup(allowedClass, cl);

        List allowedOperations = new ArrayList();
        Method[] methods = getMethods(jc.getJavaClass());
        for (int i = 0; i < methods.length; i++) {
            if (Modifier.isPublic(methods[i].getModifiers())) {
                allowedOperations.add(methods[i].getName());
            }
        }

        return allowedOperations;
    }

    protected static Method[] getMethods(Class clazz) {
        if (clazz.isInterface()){
            // Returns all methods incl inherited
            return clazz.getMethods();
        } else {
            return clazz.getDeclaredMethods();
        }
    }

}
