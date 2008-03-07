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

import org.globus.exec.generated.ManagedExecutableJobResourcePropertiesType;

/**
 * Constants class for Managed Job.
 */
public class ManagedExecutableJobConstants {
    public static final String EXEC_JOB_NS =
        "http://www.globus.org/namespaces/2004/10/gram/job/exec";
    public static final String EXEC_JOB_STATE_NS =
        "http://www.globus.org/namespaces/2004/10/gram/job/exec/state";

    public static final QName RESOURCE_PROPERTY_SET = new QName(
            ManagedExecutableJobResourcePropertiesType.
            getTypeDesc().getXmlType().getNamespaceURI(),
            "managedExecutableJobResourceProperties");

    //resource properties qnames
    public static final QName RP_STDOUT_URL
        = ManagedExecutableJobResourcePropertiesType.
                    getTypeDesc().getFieldByName("stdoutURL").
                    getXmlName();

    public static final QName RP_STDERR_URL
        = ManagedExecutableJobResourcePropertiesType.
                    getTypeDesc().getFieldByName("stderrURL").
                    getXmlName();

    public static final QName RP_CREDENTIAL_PATH
                    = ManagedExecutableJobResourcePropertiesType.
                    getTypeDesc().getFieldByName("credentialPath").
                    getXmlName();

    public static final QName RP_EXIT_CODE
                    = ManagedExecutableJobResourcePropertiesType.
                    getTypeDesc().getFieldByName("exitCode").
                    getXmlName();

    //Resource Data Names
    public static final String RD_HOLD_STATE = "holdState";

    public static final String RD_INTERNAL_STATE = "internalState";

    public static final String RD_RESTART_INTERNAL_STATE
        = "restartInternalState";

    public static final String RD_SUSPENDED_INTERNAL_STATE
        = "suspendedInternalState";

    public static final String RD_CANCELED = "canceled";

    public static final String RD_USER_CANCEL_REQUESTED = "userCancelRequested";

    public static final String RD_SYSTEM_CANCEL_REQUESTED
        = "systemCancelRequested";

    public static final String RD_TRANSFER_JOB_ENDPOINT = "transferEndpoint";

    public static final String RD_LOCAL_RESOURCE_MANAGER
        = "localResourceManager";

    public static final String RD_LOCAL_JOB_IDS = "localJobId";

    public static final String RD_LOCAL_JOB_STATES = "localJobState";

    public static final String RD_NOTIFIED_JOB_DATA = "notifiedJobData"; 

    public static final String RD_SUBSCRIPTION_ENDPOINT
        = "subscriptionEndpoint";

    //usage data fields
    public static final String RD_CREATION_TIME = "creationTime";
}
