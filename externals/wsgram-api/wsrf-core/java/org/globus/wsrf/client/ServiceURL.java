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
package org.globus.wsrf.client;

import java.net.URL;
import java.net.MalformedURLException;
import java.net.InetAddress;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.Constants;
import org.globus.wsrf.utils.Resources;
import org.globus.wsrf.container.ServiceHost;
import org.globus.util.I18n;

public class ServiceURL {

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    public static final String DEFAULT_SERVICE_CONTEXT =
        Constants.DEFAULT_WSRF_LOCATION;

    private String contactString;
    private String serviceName;
    private URL url;

    private static final String URL_SEPARATOR = "://";
    
    private static Log logger = LogFactory.getLog(ServiceURL.class.getName());

    //ContainerConfig.getWSRFLocation(); would be more precise
    //but requires to read server configuration.

    /**
     * Construct a ServiceURL object from the specified contact string.
     * Grammar for a non-null contact String:<p>
     * <b>[protocol://]host[:[port]][/service context/service path]</b>
     * <p>
     * If contactString is null then defaults will be used for protocol,
     * host and port.
     * <p>
     * Note: if the service path is not specified in the contact string
     * then the service name MUST be specified using the appropriate setter.
     * <p>
     * @param contactString The contact string.
     */
    public ServiceURL(String contactString) {
        if (contactString == null) {
            contactString = getDefaultHost();
        } else if (contactString.endsWith("/")) {
            contactString =
                contactString.substring(0, contactString.length() - 1);
        }
        
        this.contactString = contactString;
        
    } //end constructor

    public ServiceURL(String contactString, String serviceName) {
        this(contactString);
        setServiceName(serviceName);
    } //end constructor

    /**
     * Utility method to get the full URL from a simplified contact string.
     *
     * @param contactString the simplified or complete contact string to infer
     *                      the service URL from. See documentation of the
     *                      constructor for the syntax of the contact string.
     * @param serviceName the name of the service without the context
     * @exception MalformedURLException if contact string is malformed
     *            as a URL even after prepending default protocol if missing.
     */
    public static URL getURL(String contactString, String serviceName)
        throws MalformedURLException {
        URL url = null;
        
        ServiceURL serviceURL = new ServiceURL(contactString, serviceName);
        url = serviceURL.getURL();
        if (logger.isDebugEnabled()) {
            logger.debug("Resolved contact string to URL:" + 
                         url.toExternalForm());
        }
        return url;
    }
    
    public String getFullServicePath() {
        return "/" + DEFAULT_SERVICE_CONTEXT + this.serviceName;
    }

    /**
     * Specify the base name of the service, without the service context.
     * The service name is used to build the service path when it is not
     * found in the URL contructed from the contact string.
     *
     * @param serviceName the name of the service
     *                    (ex: core/admin/ContainerService)
     */
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return this.serviceName;
    }
    
    /**
     * @exception MalformedURLException if contact string is malformed
     *            as a URL even after prepending default protocol if missing.
     */
    public URL getURL() throws MalformedURLException {
        
        if (this.url == null) {
            
            String contactURLString = contactString;
            if (contactString.indexOf(URL_SEPARATOR) == -1) {
                //no protocol
                contactURLString = getDefaultProtocol() +
                    URL_SEPARATOR + contactString;
            }

            //now can construct URL object for parsing
            this.url = new URL(contactURLString); //throws MalformedURLException
            String protocol = this.url.getProtocol();
            String host = this.url.getHost();
            int port = this.url.getPort();
            String path = this.url.getPath();
            
            if (host.equals("")) {
                host = getDefaultHost();
            }
            if (port == -1) {
                port = getDefaultPort();
            }
            if (path.equals("")) {
                if (this.serviceName != null && !this.serviceName.equals("")) {
                    path = getFullServicePath();
                } else {
                    throw new RuntimeException(i18n.getMessage("noServiceName"));
                }
            }
            // too bad no public granular setters
            // reconstruct url
            
            this.url = new URL(protocol, host, port, path);
        }
        
        return this.url;
    }
    
    public static String getDefaultProtocol() {
        return ServiceHost.getDefaultProtocol();
    }
    
    public static String getDefaultHost() {
        String defaultHost;
        try {
            defaultHost = ServiceHost.getDefaultHost();
        } catch (Exception e) {
            logger.error("", e);
            try {
                InetAddress ipAddress = InetAddress.getLocalHost();
                defaultHost = ipAddress.getHostName();
                //should not fail
            } catch (Exception iNetEx) {
                logger.error("", iNetEx);
                throw new RuntimeException(iNetEx.getMessage());
            }
        }
        return defaultHost;
    }

    public static int getDefaultPort() {
        return ServiceHost.getDefaultPort();
    }

} //end class
