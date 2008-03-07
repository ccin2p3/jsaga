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

import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.security.SecurityException;

import org.globus.wsrf.impl.security.descriptor.RunAsConstants;
import org.globus.wsrf.impl.security.descriptor.ServiceSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

import org.globus.util.I18n;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.impl.security.util.AuthUtil;

import javax.security.auth.Subject;

import javax.xml.namespace.QName;

import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceContextException;

/**
 * Handler that sets the credentials to be used for this invocation.
 */
// GT3-specific handler
public class RunAsHandler extends DescriptorHandler {
    private static I18n i18n =
        I18n.getI18n(ServiceSecurityDescriptor.RESOURCE);
    private static Log logger =
        LogFactory.getLog(RunAsHandler.class.getName());

    public void handle(MessageContext msgCtx, 
                       ResourceSecurityDescriptor resDesc,
                       ServiceSecurityDescriptor desc,
                       String servicePath) throws AxisFault {

        int runAsType = -1;
        Subject invocationSubject = null;
        QName opName = null;

        if (! ((resDesc == null) && (desc == null))) {

            try {
                opName = AuthUtil.getOperationName(msgCtx);
            } catch (SecurityException exp) {
                throw AxisFault.makeFault(exp);
            }
        
            // Get run as type from resource security descriptor
            if (resDesc != null) {
                runAsType = resDesc.getRunAsType(opName);
                // Check for default in resource desc
                if (runAsType == -1) {
                    runAsType = resDesc.getDefaultRunAsType();
                }
            }

            // if not set in resource desc and service desc is not null
            if ((runAsType == -1) && (desc != null)) {
                runAsType = desc.getRunAsType(opName);
                // If default is not set in resource desc, 
                // check in service desc
                if (runAsType == -1) {
                    runAsType = desc.getDefaultRunAsType();
                }
            }
        }

        // If not, resort to default 
        if (runAsType == -1) {
            runAsType = RunAsConstants.RESOURCE;
        }
        
        logger.debug("Run as for " + opName + " is " + runAsType);

        SecurityManager manager = SecurityManager.getManager(msgCtx);

        switch (runAsType) {
        case RunAsConstants.CALLER:
            invocationSubject =
                (Subject) msgCtx.getProperty(Constants.PEER_SUBJECT);

            break;

        case RunAsConstants.SYSTEM:
            try {
                invocationSubject = manager.getSystemSubject();
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            break;

        case RunAsConstants.SERVICE:
            try {
                invocationSubject = manager.getServiceSubject(servicePath);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            break;

        case RunAsConstants.RESOURCE:
            try {
                // get resource
                Resource resource = null;
                try {
                    ResourceContext context =
                        ResourceContext.getResourceContext(msgCtx);
                    resource = context.getResource();
                } catch (ResourceContextException exp) {
                    // FIXME quiet catch
                    logger.debug("Resource does not exist ", exp);
                    resource = null;
                } catch (ResourceException exp) {
                    // FIXME quiet catch
                    logger.debug("Resource does not exist ", exp);
                    resource = null;
                }
                invocationSubject = manager.getSubject(servicePath, resource);
            } catch (Exception e) {
                throw AxisFault.makeFault(e);
            }

            break;

        default:
            throw new AxisFault(
                i18n.getMessage("badRunAs", String.valueOf(runAsType))
            );
        }

        // TODO: should we throw an exception here
        // if there is not subject at all?
        if (invocationSubject != null) {
            msgCtx.setProperty(Constants.INVOCATION_SUBJECT,
                               invocationSubject);
        } else {
            logger.debug("No invocation subject");
        }
    }
}
