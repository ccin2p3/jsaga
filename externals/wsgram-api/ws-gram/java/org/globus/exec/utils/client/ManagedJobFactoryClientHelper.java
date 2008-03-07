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

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.exec.generated.ManagedJobFactoryResourceProperties;
import org.globus.exec.generated.ManagedJobFactoryPortType;
import org.globus.exec.generated.service.ManagedJobFactoryServiceAddressingLocator;
import org.globus.exec.utils.ManagedJobConstants;
import org.globus.exec.utils.ManagedJobFactoryConstants;
import org.globus.exec.utils.service.ManagedJobHelper;
import org.globus.wsrf.client.ServiceURL;
import org.globus.wsrf.utils.AddressingUtils;

public class ManagedJobFactoryClientHelper {

    public static ServiceURL getServiceURL(String contactString) {
        return new ServiceURL(
            contactString,
            ManagedJobFactoryConstants.SERVICE_PATH_WITHOUT_CONTEXT);
    }

    /**
     * Returns a resource-qualified endpoint reference to a managed job
     * factory service.
     * @param serviceAddress String The URL to the ManagedJobFactoryService
     * @param factoryType String The type of factory, used as a key for the
     *                    resource
     * @throws Exception
     * @return EndpointReferenceType The endpoint reference to the factory
     */
    public static EndpointReferenceType getFactoryEndpoint(
        String serviceAddress,
        String factoryType)
        throws Exception
    {
        EndpointReferenceType endpoint
            = AddressingUtils.createEndpointReference(
                serviceAddress,
                ManagedJobHelper.getResourceKey(factoryType));
        return endpoint;
    }

    /**
     * Returns a resource-qualified endpoint reference to a managed job
     * factory service.
     * @param serviceAddress URL The URL to the ManagedJobFactoryService
     * @param factoryType String The type of factory, used as a key for the
     *                    resource
     * @throws Exception
     * @return EndpointReferenceType The EndpointReference to the factory
     */
    public static EndpointReferenceType getFactoryEndpoint(
        URL serviceURL,
        String factoryType)
        throws Exception
    {
        return getFactoryEndpoint(
            serviceURL.toExternalForm(), factoryType);
    }

    public static ManagedJobFactoryPortType getPort(
        URL serviceURL,
        String factoryType)
        throws Exception
    {
        return getPort(serviceURL.toExternalForm(), factoryType);
    }

    public static ManagedJobFactoryPortType getPort(
        String serviceAddress,
        String factoryType)
        throws Exception
    {
        EndpointReferenceType endpoint =
            getFactoryEndpoint(serviceAddress, factoryType);
        return getPort(endpoint);
    }

    public static ManagedJobFactoryPortType getPort(
        EndpointReferenceType factoryEndpoint)
        throws Exception
    {
        ManagedJobFactoryServiceAddressingLocator factoryLocator =
            new ManagedJobFactoryServiceAddressingLocator();
        return factoryLocator.getManagedJobFactoryPortTypePort(factoryEndpoint);
    }
}
