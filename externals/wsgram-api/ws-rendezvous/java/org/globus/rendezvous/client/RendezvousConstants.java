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
package org.globus.rendezvous.client;

import javax.xml.namespace.QName;

import org.globus.rendezvous.generated.RendezvousResourceProperties;

/**
 * Constants class for Managed Job.
 */
public class RendezvousConstants {

    public static final String RENDEZVOUS_NS =
        "http://www.globus.org/namespaces/2004/09/rendezvous";

    public static final QName RESOURCE_PROPERTY_SET = new QName(
            RendezvousResourceProperties.
            getTypeDesc().getXmlType().getNamespaceURI(),
            "rendezvousResourceProperties");

    //resource properties qnames

    public static final QName RP_COMPLETED = RendezvousResourceProperties.
                    getTypeDesc().getFieldByName("rendezvousCompleted").
                    getXmlName();

    public static final QName RP_DATA = RendezvousResourceProperties.
                    getTypeDesc().getFieldByName("registrantData").
                    getXmlName();

    public static final QName RP_CAPACITY
                    = RendezvousResourceProperties.
                    getTypeDesc().getFieldByName("capacity").
                    getXmlName();

    /**
     * The QName of the key element as serialized in XML.
     */
    public static final QName RESOURCE_KEY_QNAME =
        new QName(RENDEZVOUS_NS, "ResourceID");

}
