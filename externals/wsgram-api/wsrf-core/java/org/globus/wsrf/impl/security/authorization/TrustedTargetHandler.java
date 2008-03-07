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
package org.globus.wsrf.impl.security.authorization;

import org.globus.wsrf.impl.security.authentication.wssec.WSSecurityFault;
import org.globus.wsrf.impl.security.authentication.secureconv.service.SecurityContext;
import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.rpc.soap.SOAPFaultException;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFactory;
import javax.xml.rpc.handler.GenericHandler;
import javax.xml.rpc.handler.MessageContext;

import java.util.HashMap;

import org.globus.wsrf.security.authorization.PDPConstants;

/**
 * This class reads a map of trusted targets associated with the message
 * context of a request and compares it with the authenticated target.
 * This handler must be configured in the client request chain after a
 * WS secure conversation handshake has taken place and a valid security
 * context exists.
 */
public class TrustedTargetHandler extends GenericHandler {

    private static I18n i18n =
        I18n.getI18n(PDPConstants.RESOURCE,
                     TrustedTargetHandler.class.getClassLoader());
    static Log logger =
        LogFactory.getLog(TrustedTargetHandler.class.getName());

    public boolean handleRequest(MessageContext msgContext) {
        SecurityContext secContext =
            (SecurityContext) msgContext.getProperty(Constants.CONTEXT);
        HashMap allowedTargets =
            (HashMap) msgContext.getProperty(PDPConstants.TRUSTED_TARGETS);
        if (allowedTargets == null) {
            return true;
        }

        if (secContext != null) {
            String target = null;
            try {
                target = secContext.getContext().getTargName().toString();
                if (logger.isDebugEnabled()) {
                    logger.debug("Source name: " + secContext.getContext()
                                                   .getSrcName().toString());
                    logger.debug("Target name: " + target);
                }
            } catch (Exception e) {
                throw WSSecurityFault.makeFault(e);
            }

            if (allowedTargets.get(target)==null) {
                Detail detail = null;
                try {
                    SOAPFactory factory = SOAPFactory.newInstance();
                    detail = factory.createDetail();
                    Name detailName =
                        factory.createName("target", null,
                                           "http://www.globus.org/pdp");
                    DetailEntry entry = detail.addDetailEntry(detailName);
                    entry.addTextNode(i18n.getMessage("target", target));
                } catch (SOAPException ee) {
                    // FIXME
                }
                throw new SOAPFaultException(
                    PDPConstants.TARGET_NOT_ALLOWED_ERROR,
                    i18n.getMessage("trustTarget"), null, detail);
            }
        }
        return true;
    }

    public QName[] getHeaders() {
        return null;
    }
}
