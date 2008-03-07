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
package org.globus.exec.utils.service;

import javax.xml.namespace.QName;

import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.MessageElement;

import org.globus.exec.generated.ManagedExecutableJobResourcePropertiesType;
import org.globus.exec.utils.ManagedJobConstants;

import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.impl.SimpleResourceKey;
import org.globus.wsrf.utils.AddressingUtils;

/**
 * Constants class for Managed Job.
 */
public class ManagedJobHelper {
    /**
     * This function is public so that consumers can use it to generate the key
     * object to a resource. There is no need for a specific ResourceKey class
     * as the actual key value is just a string.
     */
    public static SimpleResourceKey getResourceKey(String keyValue) {
        return new SimpleResourceKey(
            ManagedJobConstants.RESOURCE_KEY_QNAME, keyValue);
    }

    /**
     * Return string representing the endpoint to the job. The service URL
     * and the resource ID are extracted from the endpoint reference in order
     * to create the handle. Any other data in the EPR such as other reference
     * properties, parameters or policies is lost in the "conversion".
     * An EPR can be created out of the handle by using the getEndpoint(String)
     * function.
     * @param endpoint EndpointReferenceType the endpoint to convert
     * @return String the handle, of format
     *                   handle :== <serviceURL>HANDLE_SEPARATOR<resourceID>
     */
    public static String getHandle(EndpointReferenceType endpoint) {
        String serviceAddress = endpoint.getAddress().toString();
        MessageElement referenceProperty = endpoint.getProperties().get(
            ManagedJobConstants.RESOURCE_KEY_QNAME);
        String resourceID = referenceProperty.getValue(); //key is a string
        return serviceAddress + HANDLE_SEPARATOR + resourceID;
    }

    public static EndpointReferenceType getEndpoint(String handle)
        throws Exception
    {
        int resourceIdStart = handle.indexOf(HANDLE_SEPARATOR) + 1;
        String resourceID = handle.substring(resourceIdStart);
        String serviceAddress =
            handle.substring(0, resourceIdStart - 1);
        ResourceKey key = getResourceKey(resourceID);
        return AddressingUtils.createEndpointReference(serviceAddress, key);
    }

    public static final String HANDLE_SEPARATOR = "?";
}
