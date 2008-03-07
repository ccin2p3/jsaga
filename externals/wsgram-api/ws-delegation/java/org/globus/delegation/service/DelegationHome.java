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

import org.apache.ws.patched.security.message.token.BinarySecurity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;
import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationException;
import org.globus.util.I18n;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.container.ServiceHost;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.ResourceSweeper;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.utils.AddressingUtils;

public class DelegationHome extends ResourceHomeImpl {

    static Log logger = LogFactory.getLog(DelegationHome.class.getName());

    private static I18n i18n =
        I18n.getI18n("org.globus.delegation.errors",
                     DelegationHome.class.getClassLoader());

    String resourceDescPath;

    public static UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();

    public Resource find(ResourceKey key) throws ResourceException {
        Resource resource = null;
        try {
            resource = super.find(key);
        } catch (NoSuchResourceException exp) {
            resource = createNewInstance();
            ((DelegationResource)resource).load(key);
        }

        if (ResourceSweeper.isExpired(resource)) {
            // if resource is expired then remove it explicitely
            remove(key);
            // throw exception
            throw new NoSuchResourceException();
        }

        if (resource == null)
            throw new NoSuchResourceException();

        return resource;
    }

    public EndpointReferenceType create(BinarySecurity token,
                                        String callerDN, String localName)
        throws DelegationException {

        DelegationResource resource = null;
        try {
            resource = (DelegationResource)createNewInstance();
        } catch (ResourceException exp) {
            logger.error(i18n.getMessage("createTokenErr"), exp);
            throw new DelegationException(i18n.getMessage("createTokenErr"),
                                          exp);
        }

        resource.create(token, callerDN, localName, this.resourceDescPath,
                        uuidGen.nextUUID());
        ResourceKey key = new SimpleResourceKey(keyTypeName,
                                                resource.getID());
        add(key, resource);
        logger.debug("Added " + key);

        // Create EPR
        EndpointReferenceType epr = null;
        try {
            String addr = ServiceHost.getBaseURL()
                + DelegationConstants.SERVICE_PATH;
            epr = AddressingUtils.createEndpointReference(addr, key);
        } catch (Exception exp) {
            throw new DelegationException(exp);
        }
        logger.debug("EPR for resource: " + epr);
        return epr;
    }

    public void setResourceDescPath(String _resourceDescPath) {
        this.resourceDescPath = _resourceDescPath;
    }

    public String getResourceDescPath() {
        return this.resourceDescPath;
    }
}
