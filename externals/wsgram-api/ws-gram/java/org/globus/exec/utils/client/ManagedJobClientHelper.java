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
package org.globus.exec.utils.client;

import javax.xml.namespace.QName;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.exec.generated.ManagedExecutableJobResourcePropertiesType;
import org.globus.exec.generated.ManagedJobPortType;
import org.globus.exec.generated.service.ManagedJobServiceAddressingLocator;
import org.globus.exec.utils.service.ManagedJobHelper;

import org.globus.wsrf.utils.AddressingUtils;

/**
 * Helper class for client code.
 */
public class ManagedJobClientHelper {

    public static ManagedJobPortType getPort(EndpointReferenceType endpoint)
        throws Exception
    {
        return new ManagedJobServiceAddressingLocator().
            getManagedJobPortTypePort(endpoint);
    }

    /**
     * Returns a resource-qualified endpoint reference to a managed job
     * service.
     * @param serviceAddress String The URL to the ManagedJobService
     * @param resourceID String The job ID, used as a key for the resource
     * @throws Exception
     * @return EndpointReferenceType The EndpointReference to the service
     */
    public static EndpointReferenceType getEndpoint(
        String serviceAddress,
        String resourceID)
        throws Exception
    {
        EndpointReferenceType endpoint =
            AddressingUtils.createEndpointReference(
            serviceAddress, ManagedJobHelper.getResourceKey(resourceID));
        return endpoint;
    }

    public static EndpointReferenceType getEndpoint(String handle)
        throws Exception
    {
        return ManagedJobHelper.getEndpoint(handle);
    }

    public static String getHandle(
        EndpointReferenceType endpoint)
    {
        return ManagedJobHelper.getHandle(endpoint);
    }


}
