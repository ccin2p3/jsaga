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

import javax.naming.InitialContext;
import javax.xml.namespace.QName;

import org.apache.ws.patched.security.message.token.PKIPathSecurity;
import org.apache.ws.patched.security.message.token.BinarySecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.MessageElement;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationException;
import org.globus.delegation.DelegationUtil;
import org.globus.delegation.service.DelegationHome;
import org.globus.delegationService.CertType;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceProperties;
import org.globus.wsrf.ResourceProperty;
import org.globus.wsrf.ResourcePropertySet;
import org.globus.wsrf.impl.SimpleResourceProperty;
import org.globus.wsrf.impl.SimpleResourcePropertySet;
import org.globus.wsrf.security.SecurityException;
import org.globus.wsrf.security.SecurityManager;

public class DelegationFactoryResource implements Resource,
                                                  ResourceProperties {

    static Log logger =
        LogFactory.getLog(DelegationFactoryResource.class.getName());

    ResourcePropertySet propSet;
    QName RP_SET = new QName(DelegationConstants.NS, "DelegationFactoryRPSet");

    public DelegationFactoryResource() throws DelegationException {
        setResourceProperties();
    }

    // Resource properties
    public ResourcePropertySet getResourcePropertySet() {
        return this.propSet;
    }

    private void setResourceProperties() throws DelegationException {

        this.propSet = new SimpleResourcePropertySet(RP_SET);
        PKIPathSecurity publicCertToken = DelegationUtil
            .getServiceCertAsToken(DelegationConstants.FACTORY_PATH, true);
        this.propSet = new SimpleResourcePropertySet(RP_SET);
        QName qName = new QName(DelegationConstants.NS, "CertificateChain");
        ResourceProperty prop = new SimpleResourceProperty(qName);
        prop.add(new CertType(new MessageElement[] {
            new MessageElement(publicCertToken.getElement()) }));
        this.propSet.add(prop);
    }

    public EndpointReferenceType
        createServiceResource(BinarySecurity token)
        throws DelegationException {

        DelegationHome serviceHome = getServiceHome();
        SecurityManager manager = SecurityManager.getManager();
        String callerDN = manager.getCaller();
        String localName = null;
        try {
            localName = manager.getLocalUsernames()[0];
        } catch (SecurityException exp) {
            throw new DelegationException(exp);
        }
        logger.debug("Local name " + localName);
        return serviceHome.create(token, callerDN, localName);
    }

    protected DelegationHome getServiceHome() throws DelegationException {
        try {
            InitialContext context = new InitialContext();
            logger.debug(org.globus.wsrf.Constants.JNDI_SERVICES_BASE_NAME +
                         DelegationConstants.SERVICE_PATH +
                         org.globus.wsrf.Constants.HOME_NAME);
            return (DelegationHome)context
                .lookup(org.globus.wsrf.Constants.JNDI_SERVICES_BASE_NAME +
                        DelegationConstants.SERVICE_PATH +
                        org.globus.wsrf.Constants.HOME_NAME);
        } catch (Exception exp) {
            throw new DelegationException(exp);
        }
    }
}
