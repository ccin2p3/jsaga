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
package org.globus.rendezvous.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceIdentifier;
import org.globus.wsrf.impl.ResourceHomeImpl;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.impl.ReflectionResource;

import org.globus.rendezvous.client.RendezvousConstants;
import org.globus.rendezvous.generated.RendezvousResourceProperties;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.components.uuid.UUIDGen;
import org.apache.axis.components.uuid.UUIDGenFactory;

public class RendezvousHome extends ResourceHomeImpl {

    static Log logger =
        LogFactory.getLog(RendezvousHome.class.getName());

    public synchronized void initialize() throws Exception {
        //Key deserialization configuration (easier here than in JNDI config)
        this.keyTypeName = RendezvousConstants.RESOURCE_KEY_QNAME;

        super.initialize();
    }

    /**
     * The actual key value is computed automatically by this function
     * as a UUID.
     * <b>Precondition:</b> capacity >= 0
     * @throws Exception
     * @return ResourceIdentifier
     */
    public ResourceIdentifier create(int capacity)
        throws Exception
    {
        if (capacity < 0) {
            throw new RuntimeException("Precondition violation");
        }
        SimpleResourceKey key = getResourceKey(uuidGen.nextUUID());

        ReflectionResource resource;
        synchronized (this) {

            resource = (ReflectionResource)this.createNewInstance();

            ((ReflectionResource)resource).initialize(
                this.createResourcePropertiesBean(capacity),
                RendezvousConstants.RESOURCE_PROPERTY_SET,
                key);
            logger.debug("Created RendezvousResource with key value "
                        + key.getValue());

            this.add(key, resource);
        }

        return resource;
    }

    private static final SimpleResourceKey getResourceKey(String keyValue) {
        return new SimpleResourceKey(
            RendezvousConstants.RESOURCE_KEY_QNAME, keyValue);
    }

    private static RendezvousResourceProperties createResourcePropertiesBean(
        int capacity)
    {
        RendezvousResourceProperties resourceProperties =
            new RendezvousResourceProperties();

        //Set initial values of Resource Properties:
        //resourceProperties.setTerminationTime(initialTerminationTime);
        //resourceProperties.setCurrentTime(currentTime);

        resourceProperties.setCapacity(capacity);
        resourceProperties.setRegistrantData(null);
        resourceProperties.setRendezvousCompleted(false);

        return resourceProperties;
    }

    private static UUIDGen uuidGen = UUIDGenFactory.getUUIDGen();
}

