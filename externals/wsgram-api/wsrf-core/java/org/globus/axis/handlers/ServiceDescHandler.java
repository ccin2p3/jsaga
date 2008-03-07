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
package org.globus.axis.handlers;

import org.globus.axis.description.ServiceDescUtil;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;

public class ServiceDescHandler extends BasicHandler {

    public void invoke(MessageContext messageContext) throws AxisFault {
        try {
            ServiceDescUtil.initializeService(messageContext);
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }
    
}

