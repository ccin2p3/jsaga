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

import org.globus.common.CoGProperties;
import org.globus.wsrf.config.ContainerConfig;
import org.globus.axis.util.Util;

import org.apache.axis.MessageContext;
import org.apache.axis.AxisEngine;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.MalformedURLException;

/**
 * Host lookup used to support multi-homed host configurations
 */
public class ServiceHost {

    public static final String PROXY_PORT_PROPERTY = 
        "org.globus.wsrf.proxy.port";

    private static final Log logger =
        LogFactory.getLog(ServiceHost.class.getName());

    private static boolean defaultsSet = false;
    private static int defaultPort = 8443;
    private static String defaultProtocol = "https";
    private static String defaultHost;

    static {
        Util.registerTransport();
    }

    protected static synchronized void setDefaults(String protocol, 
                                                   String host,
                                                   int port) {
        if (!defaultsSet) {
            logger.debug("Setting container defaults to: " +
                         protocol + "://" + host + ":" + port);
            defaultProtocol = protocol;
            defaultHost = host;
            defaultPort = port;
            defaultsSet = true;
        }
    }

    private static URL getEndpoint(MessageContext messageContext) 
        throws MalformedURLException {
        String address =
            (String) messageContext.getProperty(MessageContext.TRANS_URL);
        return (address == null) ? null : new URL(address);
    }

    /**
     * Returns the default container base URL in the following form: 
     * <code>scheme://host:port/context/</code>. For example: 
     * <code>https://localhost:8443/wsrf/services/</code>.
     * <br>
     * It is recommended to use {@link #getBaseURL() getBaseURL()} function 
     * for better chance of getting the right base URL.
     * 
     * @return base URL of the container.
     */
    public static URL getDefaultBaseURL() throws IOException {
        return getBaseURL(null);
    }
   
    /**
     * Returns container base URL in the following form: 
     * <code>scheme://host:port/context/</code>. For example: 
     * <code>https://localhost:8443/wsrf/services/</code>.
     * The container base URL is determined from the transport URL property
     * in <code>MessageContext</code> associated with the current thread.
     * If there is no <code>MessageContext</code> associated with the thread
     * default values will be used for the base URL.
     * 
     * @return base URL of the container.
     */
    public static URL getBaseURL() 
        throws IOException {
        return getBaseURL(MessageContext.getCurrentContext());
    }

    /**
     * Returns container base URL in the following form: 
     * <code>scheme://host:port/context/</code>. For example: 
     * <code>https://localhost:8443/wsrf/services/</code>.
     * The container base URL is determined from the transport URL property
     * in the specified <code>MessageContext</code> object. 
     * If the <code>MessageContext</code> parameter is not specified the 
     * default values will be used for the base URL.
     *
     * @param context <code>MessageContext</code> object from which to 
     *                determine the container base URL. Can be null.
     * @return base URL of the container.
     */
    public static URL getBaseURL(MessageContext context) 
        throws IOException {
        ContainerConfig config = null;
        String protocol;
        int port;
        String host;
        if (context == null) {
            config = ContainerConfig.getConfig();
            protocol = defaultProtocol;
            port = defaultPort;
            host = getDefaultHost();
        } else {
            config = ContainerConfig.getConfig(context.getAxisEngine());
            URL address = getEndpoint(context);
            if (address == null) {
                protocol = defaultProtocol;
                port = defaultPort;
            } else {
                protocol = address.getProtocol();
                port = address.getPort();
            }
            host = ServiceHost.getHost(config);
        }
        
        String proxyPort = System.getProperty(PROXY_PORT_PROPERTY);
        if (proxyPort != null) {
            port = Integer.parseInt(proxyPort);
        }

        return new URL(protocol, host, port, 
                       "/" + config.getWSRFLocation());
    }

    /**
     * Returns the default port of the container.
     * <br>
     * It is recommended to use {@link #getPort() getPort()} function 
     * for better chance of getting the right port number of the container.
     * 
     * @return the default port of the container.
     */
    public static int getDefaultPort() {
        return getPort(null);
    }

    /**
     * Returns the port number of the container.
     * The container port is determined from the transport URL property
     * in <code>MessageContext</code> associated with the current thread.
     * If there is no <code>MessageContext</code> associated with the thread
     * the default port number will be returned.
     * 
     * @return the port number of the container.
     */
    public static int getPort() {
        return getPort(MessageContext.getCurrentContext());
    }

    /**
     * Returns the port number of the container.
     * The container port is determined from the transport URL property
     * in the specified <code>MessageContext</code> object. 
     * If the <code>MessageContext</code> parameter is not specified the 
     * default port number will be returned.
     *
     * @param context <code>MessageContext</code> object from which to 
     *                determine the container port number. Can be null.
     * @return the port number of the container.
     */
    public static int getPort(MessageContext context) {
        if (context == null) {
            return defaultPort;
        }
        URL url = null;
        try {
            url = getEndpoint(context);
        } catch (IOException e) {
            logger.debug("", e);
            return defaultPort;
        }
        if (url == null) {
            return defaultPort;
        }
        int port = url.getPort();
        if (port == -1) {
            String protocol = url.getProtocol();
            if (protocol.equalsIgnoreCase("http")) {
                port = 80;
            } else if (protocol.equalsIgnoreCase("https")) {
                port = 443;
            } else {
                // unknown protocol
                port = defaultPort;
            }
        }
        return port;
    }
    
    /**
     * Returns the default protocol of the container. 
     * For example: <code>http</code>.
     * <br>
     * It is recommended to use {@link #getProtocol() getProtocol()} function
     * for better chance of getting the right protocol of the container.
     * 
     * @return the default protocol of the container.
     */
    public static String getDefaultProtocol() {
        return getProtocol(null);
    }

    /**
     * Returns the protocol of the container. For example: <code>http</code>.
     * The container protocol is determined from the transport URL property
     * in <code>MessageContext</code> associated with the current thread.
     * If there is no <code>MessageContext</code> associated with the thread
     * default protocol will be returned.
     *
     * @return the protocol of the container.
     */
    public static String getProtocol() {
        return getProtocol(MessageContext.getCurrentContext());
    }

    /**
     * Returns the protocol of the container. For example: <code>http</code>.
     * The container protocol is determined from the transport URL property
     * in the specified <code>MessageContext</code> object. 
     * If the <code>MessageContext</code> parameter is not specified the 
     * default protocol will be returned.
     *
     * @param context <code>MessageContext</code> object from which to 
     *                determine the container protocol. Can be null.
     * @return the protocol of the container.
     */
    public static String getProtocol(MessageContext context) {
        if (context == null) {
            return defaultProtocol;
        }
        URL url = null;
        try {
            url = getEndpoint(context);
        } catch (IOException e) {
            logger.debug("", e);
            return defaultProtocol;
        }
        return (url == null) ? defaultProtocol : url.getProtocol();
    }

    /**
     * Returns the default hostname of the container.
     * <br>
     * It is recommended to use {@link #getHost() getHost()} function for 
     * better chance of getting the right hostname.
     * 
     * @return the default hostname of the container.
     */
    public synchronized static String getDefaultHost() 
        throws IOException {
        if (defaultHost == null) {
            // XXX: this might read it from wrong config file
            defaultHost = getHost(ContainerConfig.getConfig());
        }
        return defaultHost;
    }

    /**
     * Returns the hostname of the container. 
     * The container hostname is determined from the 
     * <code>MessageContext</code> associated with the current thread.
     * If there is no <code>MessageContext</code> associated with the thread
     * the default hostname will be returned.
     *
     * @return the hostname of the container.
     */
    public static String getHost() 
        throws IOException {
        return getHost(MessageContext.getCurrentContext());
    }

    /**
     * Returns the hostname of the container. 
     * The container hostname is determined from the specified 
     * <code>MessageContext</code> configuration parameter.
     *
     * @param context <code>MessageContext</code> object from which to 
     *                determine the container hostname. Can be null.
     *                If null the default hostname will be returned.
     * @return the hostname of the container.
     */
    public static String getHost(MessageContext context) 
        throws IOException {
        if (context == null) {
            return getDefaultHost();
        } else {
            return getHost(context.getAxisEngine());
        }
    }

    /**
     * Returns the hostname of the container. 
     * The container hostname is determined from the specified 
     * <code>AxisEngine</code> configuration parameter.
     *
     * @param engine <code>AxisEngine</code> object from which to 
     *               determine the container hostname. Can be null.
     *               If null the default hostname will be returned.
     * @return the hostname of the container.
     */
    public static String getHost(AxisEngine engine)
        throws IOException {
        if (engine == null) {
            return getDefaultHost();
        } else {
            return getHost(ContainerConfig.getConfig(engine));
        }
    }

    /**
     * Returns the hostname of the container. 
     * The container hostname is determined from the specified container
     * configuration parameter.
     *
     * @param config <code>ContainerConfig</code> object from which to 
     *               determine the container hostname. Cannot be null.
     * @return the hostname of the container.
     */
    public static String getHost(ContainerConfig config)
        throws IOException {
        InetAddress localHost = null;
        
        String disableDNSConfig = 
            config.getOption(ContainerConfig.DISABLE_DNS);
        
        boolean disableDNS = 
            (disableDNSConfig == null) ? 
                false
                : disableDNSConfig.equalsIgnoreCase("true");
        
        String logicalHostName = 
            config.getOption(ContainerConfig.LOGICAL_HOST);
        if (logicalHostName == null) {
            logicalHostName = CoGProperties.getDefault().getHostName();
        }
        
        String host = null;
        
        if (!disableDNS) {
            if (logicalHostName != null) {
                localHost = InetAddress.getByName(logicalHostName);
            } else {
                localHost = InetAddress.getLocalHost();
            }
            
            // Determine whether we should publish IP or hostname
            String publishHost =
                config.getOption(ContainerConfig.PUBLISH_HOST_NAME);
            
            boolean publishHostName = (publishHost != null &&
                publishHost.equalsIgnoreCase("true"));
            
            if (publishHostName) {
                host = localHost.getHostName();
            } else {
                host = localHost.getHostAddress();
            }
        } else if (logicalHostName != null) {
            host = logicalHostName;
        }
        
        if (host == null) {
            host = "localhost";
        } else if (host.indexOf(".") == -1) {
            String domainName = config.getOption(ContainerConfig.DOMAIN_NAME);
            
            if (domainName != null) {
                host = host + "." + domainName;
            }
        }
        
        return host;
    }
}
