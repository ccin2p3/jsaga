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
 * Generic constants of WSN family of specifications.
 */
public class WSNConstants
{

    /**
     * Prefix used when constructing QNames for the WS-BaseNotification
     * namespace
     */
    public static final String BASEN_PREFIX = "wsnt";

    /**
     * WS-BaseNotification namespace
     */
    public static final String BASEN_NS =
        "http://docs.oasis-open.org/wsn/2004/06/wsn-WS-BaseNotification-1.2-draft-01.xsd";

    /**
     * Simple topic dialect URI
     */
    public static final String SIMPLE_TOPIC_DIALECT =
        "http://docs.oasis-open.org/wsn/2004/06/TopicExpression/Simple";

    /**
     * Concrete topic dialect URI
     */
    public static final String CONCRETE_TOPIC_DIALECT =
        "http://docs.oasis-open.org/wsn/2004/06/TopicExpression/Concrete";

    /**
     * Full topic dialect URI
     */
    public static final String FULL_TOPIC_DIALECT =
        "http://docs.oasis-open.org/wsn/2004/06/TopicExpression/Full";

    /**
     * Creation time resource property QName
     */
    public static final QName CREATION_TIME =
        new QName(BASEN_NS, "CreationTime");

    /**
     * Consumer reference resource property QName
     */
    public static final QName CONSUMER_REFERENCE =
        new QName(BASEN_NS, "ConsumerReference");

    /**
     * Topic expression resource property QName
     */
    public static final QName TOPIC_EXPRESSION =
        new QName(BASEN_NS, "TopicExpression");

    /**
     * Use notify resource property QName
     */
    public static final QName USE_NOTIFY =
        new QName(BASEN_NS, "UseNotify");

    /**
     * Precondition resource property QName
     */
    public static final QName PRECONDITION =
        new QName(BASEN_NS, "Precondition");

    /**
     * Selector resource property QName
     */
    public static final QName SELECTOR =
        new QName(BASEN_NS, "Selector");

    /**
     * Subscription policy resource property QName
     */
    public static final QName SUBSCRIPTION_POLICY =
        new QName(BASEN_NS, "SubscriptionPolicy");

    /**
     * Topic resource property QName
     */
    public static final QName TOPIC =
        new QName(BASEN_NS, "Topic");

    /**
     * Fixed topic set resource property QName
     */
    public static final QName FIXED_TOPIC_SET =
        new QName(BASEN_NS, "FixedTopicSet");

    /**
     * Topic expression dialects resource property QName
     */
    public static final QName TOPIC_EXPRESSION_DIALECTS =
        new QName(BASEN_NS, "TopicExpressionDialects");

}
