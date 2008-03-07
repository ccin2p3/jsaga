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
package org.globus.wsrf.handlers;

import java.net.URL;
import java.net.MalformedURLException;

import org.globus.wsrf.config.ContainerConfig;
import org.globus.axis.description.ServiceDescUtil;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisEngine;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.MessageContext;

/**
 * Sets the target service from <i>MessageContext.TRANS_URL</i> MessageContext
 * property. Usually will be used when requesting wsdl for a service or when
 * the WS-Addressing headers are missing.
 */
public class URLMapper extends BasicHandler {

    public void invoke(MessageContext msgContext) throws AxisFault {

        if (msgContext.getTargetService() == null &&
            msgContext.getService() == null) {
            setTargetService(msgContext, 
                             (String)msgContext.getProperty(MessageContext.TRANS_URL));
                        
            if (msgContext.getService() != null) {
                ServiceDescUtil.resetOperations(msgContext);
            }
        }
    }
    
    public void generateWSDL(MessageContext msgContext) throws AxisFault {
        invoke(msgContext);
    }
    
    protected void setTargetService(MessageContext msgContext,
                                    String url) 
        throws AxisFault {
        if (url == null) {
            return;
        }
        String path = null;
        try {
            path = (new URL(url)).getFile();
        } catch (MalformedURLException e) {
            throw AxisFault.makeFault(e);
        }

        AxisEngine engine = msgContext.getAxisEngine();
        ContainerConfig config = ContainerConfig.getConfig(engine);
        String base = config.getWSRFLocation();
        int len = base.length() + 1;

        if (path.startsWith("/" + base) && path.length() > len) {
            String service = path.substring(len);
            msgContext.setTargetService(service);
        }
    }
    
}
