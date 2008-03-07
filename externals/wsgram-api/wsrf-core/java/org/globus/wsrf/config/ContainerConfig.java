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
package org.globus.wsrf.config;

import java.util.Hashtable;
import java.io.IOException;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.AxisEngine;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.WSDDEngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.client.AxisClient;
import org.apache.axis.configuration.FileProvider;
import org.apache.axis.deployment.wsdd.WSDDDeployment;
import org.apache.axis.deployment.wsdd.WSDDGlobalConfiguration;
import org.apache.axis.server.AxisServer;

import org.globus.util.I18n;
import org.globus.wsrf.Constants;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.utils.Resources;

/**
 * This class makes global container configuration variables available to 
 * the application.
 */
public class ContainerConfig {

    static I18n i18n = I18n.getI18n(Resources.class.getName());

    public static final String WSRF_LOCATION = "wsrfLocation";
    public static final String CONTAINER_THREADS = "containerThreads";
    public static final String CONTAINER_THREADS_MAX = "containerThreadsMax";
    public static final String CONTAINER_THREADS_WATERMARK =
        "containerThreadsHighWaterMark";
    public static final String LOGICAL_HOST = "logicalHost";
    public static final String PUBLISH_HOST_NAME = "publishHostName";
    public static final String DOMAIN_NAME = "domainName";
    public static final String DISABLE_DNS = "disableDNS";

    public static final String WEB_CONTEXT = 
        "webContext";

    /**
     * Config property used to specify external web root location published
     * in wsdl files.
     */
    public static final String EXTERNAL_WEB_ROOT = 
        "webroot.external";

    /**
     * System property used to specify external web root location published
     * in wsdl files.
     */
    public static final String EXTERNAL_WEB_ROOT_PROPERTY = 
        Constants.CONTAINER_PROPERTY + "." + EXTERNAL_WEB_ROOT;
    
    /**
     * Config property used to specify internal web root location.
     */
    public static final String INTERNAL_WEB_ROOT = 
        "webroot.internal";

    /**
     * System property used to specify internal web root location.
     */
    public static final String INTERNAL_WEB_ROOT_PROPERTY = 
        Constants.CONTAINER_PROPERTY + "." + INTERNAL_WEB_ROOT;

    /**
     * Enables WebStart support in standalone container. Must be set to 
     * <code>enable</code> to enable the support.
     * <i>Standalone container only.</i>
     */
    public static final String WEB_START_PROPERTY = 
        Constants.CONTAINER_PROPERTY + ".webstart";

    public static final String CONFIG_PROFILE =
        "config.profile";

    private static AxisEngine axisClientEngine = null;

    // XXX: this is not cleared - maybe use SoftReferences?
    private static Hashtable serverEngines = new Hashtable();

    public static final String DEFAULT_SERVER_CONFIG = 
        "server-config.wsdd";

    private static ContainerConfig config;

    private AxisEngine engine;
    private WSDDGlobalConfiguration globalConfig;
    
    static Log logger = 
        LogFactory.getLog(ContainerConfig.class.getName());

    protected ContainerConfig(AxisEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException();
        }
        EngineConfiguration config = engine.getConfig();
        if (!(config instanceof WSDDEngineConfiguration)) {
            throw new IllegalArgumentException(
              i18n.getMessage("expectedType", WSDDEngineConfiguration.class));
        }
        WSDDDeployment deployment =
            ((WSDDEngineConfiguration)config).getDeployment();
        
        this.globalConfig = deployment.getGlobalConfiguration();
        this.engine = engine;
    }
    
    /**
     * Gets a default <code>ContainerConfig</code> instance initialized with
     * a default AxisServer engine.
     */
    public synchronized static ContainerConfig getConfig() {
        if (config == null) {
            config = new ContainerConfig(getEngine());
        }
        return config;
    }

    public static ContainerConfig getConfig(AxisEngine engine) {
        return new ContainerConfig(engine);
    }

    public String getOption(String name) {
        return getOption(name, null);
    }
    
    /**
     * Gets an option (a global parameter). 
     */
    public String getOption(String name, String defaultValue) {
        if (this.globalConfig == null) {
            return null;
        } else {
            String value = this.globalConfig.getParameter(name);
            return (value != null) ? value : defaultValue;
        }
    }
    
    /**
     * Sets an option (a global parameter). The value is stored in
     * memory only. It will not be persisted. 
     */
    public void setOption(String name, String value) {
        if (this.globalConfig == null) {
            this.globalConfig = new WSDDGlobalConfiguration();
        }
        this.globalConfig.setParameter(name, value);
    }

    public String getWSRFLocation() {
        return getOption(ContainerConfig.WSRF_LOCATION,
                         Constants.DEFAULT_WSRF_LOCATION);
    }

    /**
     * Returns external web root location. The external web root location
     * the published and virtual base location from which schema files, etc.
     * are resolved from. 
     * The wsdl import location attribute value (if it is a file) in the 
     * service wsdl will be prepended with this location.
     */
    public static String getExternalWebRoot(MessageContext ctx) 
        throws IOException {
        String webRoot = System.getProperty(EXTERNAL_WEB_ROOT_PROPERTY);
        if (webRoot == null) {
            ContainerConfig config = new ContainerConfig(ctx.getAxisEngine());
            webRoot = config.getOption(EXTERNAL_WEB_ROOT);
            if (webRoot == null) {
                String webContext = config.getOption(WEB_CONTEXT);
                if (webContext == null) {
                    webContext = "/";
                } else {
                    webContext = "/" + webContext + "/";
                }
                URL webRootUrl = 
                    new URL(ServiceHost.getProtocol(ctx),
                            ServiceHost.getHost(ctx),
                            ServiceHost.getPort(ctx),
                            webContext);
                webRoot = webRootUrl.toExternalForm();
            }
        }
        return webRoot;
    }
    
    /**
     * Returns internal web root location. The internal web root location
     * is a path to a directory on a file system. Schema files, etc. are
     * loaded from this location.
     */
    public String getInternalWebRoot() {
        String webRoot = System.getProperty(INTERNAL_WEB_ROOT_PROPERTY);
        if (webRoot == null) {
            webRoot = getOption(INTERNAL_WEB_ROOT);
            if (webRoot == null) {
                webRoot = getGlobusLocation();
            }
        }
        return webRoot;
    }

    public static String getGlobusLocation() {
        String value = System.getProperty("GLOBUS_LOCATION");
        return (value == null) ? "." : value;
    }
    
    /**
     * Get the default Axis client engine.
     */
    public static synchronized AxisEngine getClientEngine() {
        if (axisClientEngine == null) {
            axisClientEngine = new AxisClient();
        }
        return axisClientEngine;
    }

    /**
     * Get the default Axis server engine.
     */
    public static AxisServer getEngine() {
        return getServerEngine(null);
    }
    
    public synchronized static AxisServer getServerEngine(String config) {
        config = (config == null) ? DEFAULT_SERVER_CONFIG : config;
        AxisServer engine = (AxisServer)serverEngines.get(config);
        if (engine == null) {
            engine = new AxisServer(new FileProvider(config));
            serverEngines.put(config, engine);
        }
        return engine;
    }
    
    /**
     * Gets MessageContext associated with the current thread. If 
     * MessageContext is not associated with the current thread a new
     * one is created, initialized with client AxisEngine.
     */
    public static MessageContext getContext() {
        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx == null) {
            ctx = new MessageContext(getClientEngine());
            ctx.setEncodingStyle("");
            ctx.setProperty(AxisClient.PROP_DOMULTIREFS,
                            Boolean.FALSE);
        }
        return ctx;
    }

}
