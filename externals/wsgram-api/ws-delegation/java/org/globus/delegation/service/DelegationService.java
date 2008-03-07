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
package org.globus.delegation.service;

import java.rmi.RemoteException;

import org.apache.ws.patched.security.message.token.BinarySecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.delegation.DelegationException;
import org.globus.delegation.DelegationServiceUtil;
import org.globus.delegationService.VoidType;

import org.globus.util.I18n;

import org.globus.ws.trust.RequestSecurityTokenType;

import org.globus.wsrf.ResourceContext;

public class DelegationService {

    static Log logger = LogFactory.getLog(DelegationService.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.delegation.errors",
                     DelegationService.class.getClassLoader());

    public VoidType refresh(RequestSecurityTokenType request)
        throws RemoteException {

        logger.debug("Refresh");

        DelegationResource resource =
            (DelegationResource)ResourceContext.getResourceContext()
            .getResource();
        logger.debug("ID " + resource.getID());

        BinarySecurity token =
            DelegationServiceUtil.getTokenFromRequest(request);
        try {
            resource.storeToken(token);
        } catch (DelegationException exp) {
            logger.error(i18n.getMessage("refreshError"), exp);
            throw new RemoteException(i18n.getMessage("refreshError"), exp);
        }

        return new VoidType();
    }
}
