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
package org.globus.wsrf.container;

import java.util.Hashtable;
import java.util.Iterator;
import java.security.PrivilegedExceptionAction;
import java.security.PrivilegedActionException;

import javax.security.auth.Subject;

import org.apache.axis.server.AxisServer;
import org.apache.axis.MessageContext;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.AxisEngine;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDService;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.description.ServiceDesc;

import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.NoResourceHomeException;
import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityConfig;
import org.globus.axis.description.ServiceDescUtil;
import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.globus.gsi.gssapi.jaas.JaasSubject;

/**
 * This class manages a set of services. It it used to startup, intialize
 * deploy and undeploy services.
 */
public class ServiceManager {
    
    public static final String LOAD_STARTUP_PARAM = 
        "loadOnStartup";

    private static I18n i18n = 
        I18n.getI18n(Resources.class.getName());

    private static Log logger =
        LogFactory.getLog(ServiceManager.class.getName());

    private static Hashtable managers = new Hashtable();

    private AxisServer engine;
    private WSDDDeployment deployment;
    private MessageContext ctx;
    private UsageConfig monitor;

    private static final String INITIALIZED =
        "org.globus.wsrf.container.service.initialized";

    public ServiceManager(AxisServer engine) {
        if (engine == null) {
            throw new IllegalArgumentException();
        }
        this.engine = engine;
        EngineConfiguration config = this.engine.getConfig();
        if (!(config instanceof WSDDEngineConfiguration)) {
            throw new IllegalArgumentException();
        }
        this.deployment =
            ((WSDDEngineConfiguration)config).getDeployment();

        this.monitor = new UsageConfig(this);
    }
    
    public static synchronized ServiceManager 
        getServiceManager(AxisServer engine) {
        ServiceManager manager = (ServiceManager)managers.get(engine);
        if (manager == null) {
            manager = new ServiceManager(engine);
            managers.put(engine, manager);
        }
        return manager;
    }

    public static ServiceManager getCurrentServiceManager() {
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx == null) {
            return null;
        }
        AxisEngine engine = ctx.getAxisEngine();
        return (engine instanceof AxisServer) ?
            getServiceManager((AxisServer)engine) :
            null;
    }

    public UsageConfig getUsageConfig() {
        return this.monitor;
    }

    AxisServer getAxisEngine() {
        return this.engine;
    }

    WSDDService[] getServices() {
        return this.deployment.getServices();
    }

    String getOption(String key) {
        return (this.deployment.getGlobalConfiguration() != null) ?
            this.deployment.getGlobalConfiguration().getParameter(key) :
            null;
    }

    /**
     * Called when a container is starting up.
     */
    public void start(MessageContext msgCtx) throws Exception {
        logger.debug("Starting engine: " + this.engine);

        this.ctx = msgCtx;

        // step 1: initialize the entire JNDI tree
        //         this does not initialize the resource homes
        JNDIUtils.initializeDir(msgCtx);

        // step 2: find and initialize service with
        //         load on start up option
        WSDDService[] services = this.deployment.getServices();

        MessageContext oldCtx = HelperAxisEngine.getCurrentMessageContext();
        HelperAxisEngine.setCurrentMessageContext(msgCtx);
        try {
            for (int i=0;i<services.length;i++) {
                if ("true".equalsIgnoreCase(
                         services[i].getParameter(LOAD_STARTUP_PARAM))) {
                    String serviceName = 
                        services[i].getQName().getLocalPart();
                    msgCtx.setTargetService(serviceName);
                    try {
                        initializeService(msgCtx);
                    } catch (Exception e) {
                        throw new ContainerException(
                           i18n.getMessage("failedInitService", serviceName), 
                           e);
                    }
                }
                
            }
        } finally {
            HelperAxisEngine.setCurrentMessageContext(oldCtx);
        }

        if (this.monitor.hasTargets()) {
            this.monitor.sendStartPacket();
        }
    }

    public MessageContext createMessageContext(String serviceName) 
        throws Exception {
        MessageContext newCtx = new MessageContext(this.engine);
        Iterator iter = this.ctx.getAllPropertyNames();
        while(iter.hasNext()) {
            String propName = (String)iter.next();
            Object propValue = this.ctx.getProperty(propName);
            newCtx.setProperty(propName, propValue);
        }
        newCtx.setTargetService(serviceName);
        return newCtx;
    }

    /**
     * Initializes service description and resource home of the 
     * service currently associated with the givem MessageContext.
     */
    public static void initializeService(MessageContext ctx)
        throws Exception {
        String serviceName = ctx.getTargetService();
        SOAPService service = ctx.getService();

        // check if target service is non null and
        // actual service is set
        if (serviceName == null || service == null) {
            return;
        }

        ServiceDesc serviceDesc =
            service.getInitializedServiceDesc(ctx);

        // check if already initalized
        if (serviceDesc.getProperty(INITIALIZED) != null) {
            return;
        }

        synchronized (serviceDesc) {
            if (serviceDesc.getProperty(INITIALIZED) != null) {
                return;
            }
            logger.debug("Activating service: " + serviceName);
            
            // forces the service security to be initialized
            ServiceSecurityConfig.initialize(ctx);
            
            Subject subject = 
                SecurityManager.getManager().getServiceSubject(serviceName);

            if (subject == null) {
                InitPrivilegedAction.initialize(ctx);
            } else {
                try {
                    JaasSubject.doAs(subject, new InitPrivilegedAction(ctx));
                } catch (PrivilegedActionException e) {
                    throw e.getException();
                }
            }

            logger.debug("Activated service: " + serviceName);
            serviceDesc.setProperty(INITIALIZED, Boolean.TRUE);
        }
    }
    
    /**
     * Called when container is being shut down.
     */
    public void stop() {
        logger.debug("Stopping engine: " + this.engine);
        managers.remove(this.engine);
        if (this.monitor.hasTargets()) {
            this.monitor.sendStopPacket();
        }
    }
    
    private static class InitPrivilegedAction 
        implements PrivilegedExceptionAction {

        private MessageContext ctx = null;

        public InitPrivilegedAction(MessageContext ctx) {
            this.ctx = ctx;
        }

        public static void initialize(MessageContext ctx) 
            throws Exception {
            // forces the service description to be updated
            ServiceDescUtil.initializeService(ctx);
            
            // forces the resource home to be initialized
            ResourceContext rctx = ResourceContext.getResourceContext(ctx);
            try {
                rctx.getResourceHome();
            } catch (NoResourceHomeException e) {
                // that's acceptible
            }
        }

        public Object run() throws Exception {
            initialize(this.ctx);
            return null;
        }
    }

    public static class HelperAxisEngine extends AxisEngine {

        public HelperAxisEngine() {
            super(null);
        }

        public void init() {
        }

        public AxisEngine getClientEngine() {
            return null;
        }

        public void invoke(MessageContext ctx) {
        }

        public static void setCurrentMessageContext(MessageContext mc) {
            AxisEngine.setCurrentMessageContext(mc);
        }
    }

}
