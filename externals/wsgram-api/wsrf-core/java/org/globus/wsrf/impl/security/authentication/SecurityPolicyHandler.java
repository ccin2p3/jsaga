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
package org.globus.wsrf.impl.security.authentication;

import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityDescriptor;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;

import java.util.List;
import java.util.Vector;

/**
 * Handler that enforces security policy on server side.
 */
// GT3-specific handler
public class SecurityPolicyHandler extends DescriptorHandler {

    private static List handlers = new Vector();

    static {
        handlers.add(new AuthHandler());
        handlers.add(new RunAsHandler());
    }

    public void handle(MessageContext msgCtx, 
                       ResourceSecurityDescriptor resDesc,
                       ServiceSecurityDescriptor desc, 
                       String servicePath) 
        throws AxisFault {
        
        int size = handlers.size();
        DescriptorHandler handler;
        
        for (int i = 0; i < size; i++) {
            handler = (DescriptorHandler) handlers.get(i);
            handler.handle(msgCtx, resDesc, desc, servicePath);
        }
    }

}
