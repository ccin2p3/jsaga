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
package org.globus.wsrf.utils;

import java.net.URL;

import javax.xml.soap.SOAPElement;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.message.addressing.Address;
import org.apache.axis.message.addressing.EndpointReferenceType;
import org.apache.axis.message.addressing.ReferencePropertiesType;

import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceContext;
import org.globus.wsrf.container.ServiceHost;
import org.globus.util.I18n;

/**
 * Utility class for construction WS-Addressing endpoint references and for
 * discovering the base address of a container
 */
public class AddressingUtils {

    static Log logger =
        LogFactory.getLog(AddressingUtils.class.getName());

    private static I18n i18n = I18n.getI18n(Resources.class.getName());

    /**
     * Create a endpoint reference from the given parameters
     *
     * @param address URL for the service
     * @param key Resource identifier. May be null
     * @return The endpoint reference
     * @throws Exception
     */
    public static EndpointReferenceType
        createEndpointReference(String address, ResourceKey key)
        throws Exception {
        EndpointReferenceType reference = new EndpointReferenceType();
        if (key != null) {
            ReferencePropertiesType referenceProperties =
                new ReferencePropertiesType();

            SOAPElement elem = key.toSOAPElement();

            AnyHelper.setAny(referenceProperties, elem);

            reference.setProperties(referenceProperties);
        }

        reference.setAddress(new Address(address));

        return reference;
    }

    /**
     * Create a endpoint reference using the current service's address and the
     * given resource identifier
     *
     * @param key Resource identifier. May be null
     * @return The endpoint reference
     * @throws Exception
     */
    public static EndpointReferenceType
        createEndpointReference(ResourceKey key)
        throws Exception {
        return createEndpointReference(ResourceContext.getResourceContext(),
                                       key);
    }

    /**
     * Create a endpoint reference using the current service's address, as
     * specified by the resource context, and the given resource identifier
     *
     * @param context The resource context used for obtaining a URL for the
     *                current service
     * @param key     The resource identifier
     * @return The constructed endpoint reference
     * @throws Exception
     */
    public static EndpointReferenceType
        createEndpointReference(ResourceContext context, ResourceKey key)
        throws Exception {
        if (context == null) {
            throw new IllegalArgumentException(i18n.getMessage(
                "nullArgument", "context"));
        }
        URL baseURL = ServiceHost.getBaseURL();
        String serviceURI = baseURL.toString() + context.getService();
        return createEndpointReference(serviceURI, key);
    }

}
