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

import javax.xml.namespace.QName;

import org.globus.exec.generated.ManagedJobResourcePropertiesType;

/**
 * Constants class for Managed Job.
 */
public class ManagedJobConstants {
    public static final String JOB_NS =
        "http://www.globus.org/namespaces/2004/10/gram/job";

    public static final QName RESOURCE_PROPERTY_SET = new QName(
            ManagedJobResourcePropertiesType.
            getTypeDesc().getXmlType().getNamespaceURI(),
            "managedJobResourceProperties");

    //resource properties qnames

    public static final QName RP_SERVICE_LEVEL_AGREEMENT
        = ManagedJobResourcePropertiesType.
            getTypeDesc().getFieldByName("serviceLevelAgreement").getXmlName();

    public static final QName RP_STATE
        = ManagedJobResourcePropertiesType.
            getTypeDesc().getFieldByName("state").getXmlName();

    public static final QName RP_FAULT
        = ManagedJobResourcePropertiesType.
            getTypeDesc().getFieldByName("fault").getXmlName();

    public static final QName RP_LOCAL_USER_ID
        = ManagedJobResourcePropertiesType.
            getTypeDesc().getFieldByName("localUserId").getXmlName();

    public static final QName RP_USER_SUBJECT
        = ManagedJobResourcePropertiesType.
            getTypeDesc().getFieldByName("userSubject").getXmlName();

    public static final QName RP_HOLDING
        = ManagedJobResourcePropertiesType.
            getTypeDesc().getFieldByName("holding").getXmlName();


    //Resource Data Names

    public static final String RD_STARTED
        = "started";

    public static final String RD_NOTIFICATION_CONSUMER_ENDPOINT
        = "notificationConsumerEndpoint";

    public static final String RD_TOPIC_LISTENER
        = "topicListener";

    public static final String RD_HANDLE
        = "handle";

    /**
     * The QName of the key element as serialized in XML.
     */
    public static final QName RESOURCE_KEY_QNAME =
        new QName(ManagedJobConstants.JOB_NS, "ResourceID");

    public static final String SECURITY_CONFIG_FILE_NAME
        = "etc/gram-service/managed-job-security-config.xml";
}
