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
package org.globus.exec.utils;

import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.axis.message.addressing.EndpointReferenceType;

import org.globus.exec.generated.ManagedJobFactoryResourceProperties;
import org.globus.exec.generated.ManagedJobFactoryPortType;
import org.globus.exec.generated.service.ManagedJobFactoryServiceAddressingLocator;
import org.globus.wsrf.client.ServiceURL;
import org.globus.wsrf.utils.AddressingUtils;

public class ManagedJobFactoryConstants {

    public static final QName RESOURCE_PROPERTY_SET = new QName(
            ManagedJobFactoryResourceProperties.
            getTypeDesc().getXmlType().getNamespaceURI(),
            "managedJobFactoryResourceProperties");

    /**
     * The QName of the Resource Property "LocalResourceManager".
     */
    public static final QName RP_LOCAL_RESOURCE_MANAGER
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("localResourceManager").getXmlName();

    public static final QName RP_GLOBUS_LOCATION
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("globusLocation").getXmlName();

    public static final QName RP_CPU = ManagedJobFactoryResourceProperties.
        getTypeDesc().getFieldByName("hostCPUType").getXmlName();

    public static final QName RP_MANUFACTURER
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("hostManufacturer").getXmlName();

    public static final QName RP_OS_NAME
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("hostOSName").getXmlName();

    public static final QName RP_OS_VERSION
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("hostOSVersion").getXmlName();

    public static final QName RP_CONDOR_ARCH
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("condorArchitecture").getXmlName();

    public static final QName RP_CONDOR_VERSION
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("condorOS").getXmlName();

    public static final QName RP_SCRATCH_BASE_DIRECTORY
        = ManagedJobFactoryResourceProperties.
          getTypeDesc().getFieldByName("scratchBaseDirectory").getXmlName();

    public static final QName RP_DELEGATION_FACTORY_ENDPOINT
        = ManagedJobFactoryResourceProperties.getTypeDesc()
            .getFieldByName("delegationFactoryEndpoint")
            .getXmlName();

    public static final QName RP_STAGING_DELEGATION_FACTORY_ENDPOINT
        = ManagedJobFactoryResourceProperties.getTypeDesc()
            .getFieldByName("stagingDelegationFactoryEndpoint")
            .getXmlName();

    /**
     * The QName of the key element as serialized in XML.
     */
    public static final QName RESOURCE_KEY_QNAME
        = ManagedJobConstants.RESOURCE_KEY_QNAME;

    public static final String DEFAULT_SERVICE_PATH = "";

    public static final String SERVICE_NAME = "ManagedJobFactoryService";

    /**
     * The service name without the context
     * (which is common to all services)
     */
    public static final String SERVICE_PATH_WITHOUT_CONTEXT =
        DEFAULT_SERVICE_PATH + SERVICE_NAME;

    public static final ServiceURL DEFAULT_SERVICE_URL =
        new ServiceURL(null, SERVICE_PATH_WITHOUT_CONTEXT);

    public interface FACTORY_TYPE {
        public static final String FORK    = "Fork";
        public static final String PBS     = "PBS";
        public static final String LSF     = "LSF";
        public static final String CONDOR  = "Condor";
        public static final String MULTI  =  "Multi";
    }
    public static final String DEFAULT_FACTORY_TYPE = FACTORY_TYPE.FORK;
}
