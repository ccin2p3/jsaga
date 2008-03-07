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
package org.globus.delegation;

import java.rmi.RemoteException;

import org.apache.ws.patched.security.message.token.BinarySecurity;
import org.apache.ws.patched.security.message.token.PKIPathSecurity;
import org.apache.ws.patched.security.message.token.X509Security;
import org.apache.ws.patched.security.WSSConfig;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;

import org.w3c.dom.Element;

import org.globus.ws.trust.RequestSecurityTokenType;
import org.globus.util.I18n;

/**
 * Utility function for use by delegation factory and service.
 */
public class DelegationServiceUtil {
    static Log logger = LogFactory.getLog(
        DelegationServiceUtil.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.delegation.errors",
                     DelegationServiceUtil.class.getClassLoader());

    /**
     * Retrieve token from requst
     *
     * @param request The token request from which to get the delegated
     *                certificate including any potential chained certificates.
     * @return A binary security token structure
     * @throws RemoteException
     */
    public static BinarySecurity
        getTokenFromRequest(RequestSecurityTokenType request)
        throws RemoteException {

        MessageElement elem[] = request.get_any();

        BinarySecurity token = null;
        try {
            Element tokenElement = elem[0].getAsDOM();
            String valueType = tokenElement.getAttribute("ValueType");
            WSSConfig wssConfig = WSSConfig.getDefaultWSConfig();
            if (valueType == null) {
                token = new BinarySecurity(wssConfig, tokenElement);
            } else {
                if (valueType.equals(PKIPathSecurity.getType(wssConfig))) {
                    token = new PKIPathSecurity(wssConfig, tokenElement);
                } else if (valueType.equals(X509Security.getType(wssConfig))) {
                    token = new X509Security(wssConfig, tokenElement);
                } else {
                    token = new BinarySecurity(wssConfig, tokenElement);
                }
            }
        } catch (Exception exp) {
            DelegationUtil.logger.error(i18n.getMessage("errConstToken"), exp);
            throw new RemoteException(i18n.getMessage("errConstToken"), exp);
        }
        return token;
    }
}
