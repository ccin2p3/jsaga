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
package org.globus.wsrf.utils;

import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.AxisEngine;
import org.apache.axis.MessageContext;
import org.apache.axis.AxisFault;

public class ContextUtils {

    public static String getTargetServicePath(MessageContext context) {
        return (String) context.getTargetService();
    }

    public static SOAPService getTargetService(MessageContext context) {
        return context.getService();
    }

    public static Object getServiceProperty(MessageContext context,
                                            String serviceName,
                                            String propName)
        throws AxisFault {
        AxisEngine engine = context.getAxisEngine();
        if (engine != null) {
            SOAPService service = (SOAPService)engine.getService(serviceName);
            return getServiceProperty(service, propName);
        }
        return null;
    }

    public static Object getServiceProperty(MessageContext context,
                                            String propName) {
        SOAPService service = (SOAPService)context.getService();
        return getServiceProperty(service, propName);
    }

    public static Object getServiceProperty(SOAPService service,
                                            String propName) {
        return (service != null) ? service.getOption(propName) : null;
    }

    public static void setServiceProperty(MessageContext context,
                                          String serviceName,
                                          String propName, 
                                          Object value)
        throws AxisFault {
        AxisEngine engine = context.getAxisEngine();
        if (engine != null) {
            SOAPService service = (SOAPService)engine.getService(serviceName);
            if (service != null) {
                if (value == null) {
                    if (service.getOptions() != null) {
                        service.getOptions().remove(propName);
                    }
                } else {
                    service.setOption(propName, value);
                }
            }
        }
    }
}
