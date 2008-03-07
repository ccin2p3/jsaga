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
package org.globus.wsrf;

import javax.xml.namespace.QName;

/**
 * Implementation specific constants. The NS
 * constants point to the currently supported namespace versions.
 */
public class Constants
{

    public static final String WSDL_NS = 
        "http://schemas.xmlsoap.org/wsdl/";
    public static final String WSDL_SOAP_NS =
        "http://schemas.xmlsoap.org/wsdl/soap/";
    public static final String XSD_NS =
        "http://www.w3.org/2001/XMLSchema";
    public static final String XSI_NS =
        "http://www.w3.org/2001/XMLSchema-instance";

    public static final String CONTAINER_PROPERTY =
        "org.globus.wsrf.container";
    public static final String DEFAULT_WSRF_LOCATION =
        "wsrf/services/";
    public static final QName ANY =
        org.apache.axis.Constants.XSD_ANY;
    public static final String CORE_NS =
        "http://www.globus.org/namespaces/2004/06/core";

   /**
     * JNDI base path
     */
    public static final String JNDI_BASE_NAME =
        "java:comp/env/";

    /**
     * JNDI services base path
     */
    public static final String JNDI_SERVICES_BASE_NAME =
        JNDI_BASE_NAME + "/services/";

    /**
     * JNDI path to entry containing the name of the default subscription
     * manager service
     */
    public static final String SUBSCRIPTION_MANAGER_SERVICE_NAME =
        "SubscriptionManagerService";

    /**
     * JNDI path to entry containing the name of the default notification
     * consumer service
     */
    public static final String NOTIFICATION_CONSUMER_SERVICE_NAME =
        "NotificationConsumerService";

    /**
     * JNDI path appended to JNDI service path when looking up the service's
     * resource home
     */
    public static final String HOME_NAME =
        "/home";

    /**
     * JNDI path to the default work manager
     */
    public static final String DEFAULT_WORK_MANAGER =
        JNDI_BASE_NAME + "/wm/ContainerWorkManager";

    /**
     * JNDI path to the default timer manager
     */
    public static final String DEFAULT_TIMER =
        JNDI_BASE_NAME + "/timer/ContainerTimer";

    /**
     * JNDI path to the default query engine
     */
    public static final String DEFAULT_QUERY_ENGINE =
        JNDI_BASE_NAME + "/query/ContainerQueryEngine";

    /**
     * JNDI path to the default topic expression engine
     */
    public static final String DEFAULT_TOPIC_EXPRESSION_ENGINE =
        JNDI_BASE_NAME + "/topic/ContainerTopicExpressionEngine";

}
