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
package org.globus.delegation.factory;

import java.rmi.RemoteException;

import org.apache.ws.security.message.token.BinarySecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReference;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.delegation.DelegationException;
import org.globus.delegation.DelegationServiceUtil;

//import org.globus.ws.trust.RequestSecurityTokenResponseCollectionType;
import org.globus.ws.trust.RequestSecurityTokenResponseType;
import org.globus.ws.trust.RequestSecurityTokenType;

import org.globus.wsrf.ResourceContext;

import org.globus.wsrf.utils.XmlUtils;

public class DelegationFactoryService {

    static Log logger =
        LogFactory.getLog(DelegationFactoryService.class.getName());

    // Create a new resource and store the token there.
    public RequestSecurityTokenResponseType
        requestSecurityToken(RequestSecurityTokenType request)
        throws RemoteException {

        logger.debug("Request security token");

        BinarySecurity token =
            DelegationServiceUtil.getTokenFromRequest(request);
        ResourceContext ctx = ResourceContext.getResourceContext();
        DelegationFactoryResource resource =
            (DelegationFactoryResource)ctx.getResource();
        EndpointReferenceType epr = null;
        try {
            epr = resource.createServiceResource(token);
        } catch (DelegationException exp) {
            String str = "Error creating service resource";
            logger.error(str, exp);
            throw new RemoteException(str, exp);
        }

        EndpointReference eprRef = new EndpointReference(epr);

        MessageElement msgElem = null;
        try {
            msgElem = new MessageElement(eprRef.toDOM(XmlUtils.newDocument()));
        } catch (Exception exp) {
            String err = "Error constructing message element";
            logger.error(err, exp);
            throw new RemoteException(err, exp);
        }

        RequestSecurityTokenResponseType response =
            new RequestSecurityTokenResponseType();
        response.set_any(new MessageElement[] { msgElem });
        return response;
    }
/*
    public RequestSecurityTokenResponseCollectionType
        requestSecurityToken2(RequestSecurityTokenType request)
        throws RemoteException {
        logger.error("Not supported");
        throw new RemoteException("Not supported");
    }*/
}
