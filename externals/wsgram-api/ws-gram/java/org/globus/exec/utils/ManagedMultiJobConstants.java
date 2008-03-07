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

import org.globus.exec.generated.ManagedMultiJobResourcePropertiesType;

/**
 * Constants class for Managed Job.
 */
public class ManagedMultiJobConstants {
    public static final String MULTI_JOB_NS
        = "http://www.globus.org/namespaces/2004/10/gram/job/multi";
    public static final String MULTI_JOB_STATE_NS
        = "http://www.globus.org/namespaces/2004/10/gram/job/multi/state";

    public static final QName RESOURCE_PROPERTY_SET = new QName(
            ManagedMultiJobResourcePropertiesType.
            getTypeDesc().getXmlType().getNamespaceURI(),
            "managedMultiJobResourceProperties");

    //resource properties qnames

    public static final QName RP_SUB_JOB_ENDPOINTS
        = ManagedMultiJobResourcePropertiesType.
            getTypeDesc().getFieldByName("subJobEndpoint").
            getXmlName();

    public static final String RD_SUBSCRIPTION_ENDPOINT
        = "subscriptionEndpoint";

    public static final String RDDestroy_CALLED
        = "destroyCalled";

    public static final String RD_SUB_JOB_COUNT = "subJobCount";
    public static final String RD_STAGE_IN_COUNT = "stageInCount";
    public static final String RD_PENDING_COUNT = "pendingCount";
    public static final String RD_ACTIVE_COUNT = "activeCount";
    public static final String RD_STAGE_OUT_COUNT = "stageOutCount";
    public static final String RD_CLEAN_UP_COUNT = "cleanUpCount";
    public static final String RD_SUSPENDED_COUNT = "suspendedCount";
    public static final String RD_DONE_COUNT = "doneCount";
    public static final String RD_FAILED_COUNT = "failedCount";
    public static final String RD_HOLDING_COUNT = "holdingCount";
}
