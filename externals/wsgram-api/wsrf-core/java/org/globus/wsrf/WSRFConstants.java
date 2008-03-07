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
 * Generic constants defined by WSRF family of specifications.
 */
public class WSRFConstants
{
    /**
     * Prefix used when constructing QNames for the WS-ResourceLifetime
     * namespace
     */
    public static final String LIFETIME_PREFIX = "wsrl";

    /**
     * WS-ResourceLifetime namespace
     */
    public static final String LIFETIME_NS =
        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceLifetime-1.2-draft-01.xsd";

    /**
     * Termination time resource property QName
     */
    public static final QName TERMINATION_TIME =
        new QName(LIFETIME_NS, "TerminationTime");

    /**
     * Current time resource property QName
     */
    public static final QName CURRENT_TIME =
        new QName(LIFETIME_NS, "CurrentTime");

    /**
     * Prefix used when constructing QNames for the WS-ResourceProperties
     * namespace
     */
    public static final String PROPERTIES_PREFIX = "wsrp";

    /**
     * WS-ResourceProperties namespace
     */
    public static final String PROPERTIES_NS =
        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ResourceProperties-1.2-draft-01.xsd";

    /**
     * Prefix used when constructing QNames for the WS-ServiceGroup
     * namespace
     */
    public static final String SERVICEGROUP_PREFIX = "wssg";

    /**
     * WS-ServiceGroup namespace
     */
    public static final String SERVICEGROUP_NS =
        "http://docs.oasis-open.org/wsrf/2004/06/wsrf-WS-ServiceGroup-1.2-draft-01.xsd";

    /**
     * Entry resource property QName
     */
    public static final QName ENTRY =
        new QName(SERVICEGROUP_NS, "Entry");

    /**
     * MembershipContentRule resource property QName
     */
    public static final QName MEMBERSHIP_CONTENT_RULE =
        new QName(SERVICEGROUP_NS, "MembershipContentRule");

    /**
     * MemberEPR resource property QName
     */
    public static final QName MEMBER_EPR =
        new QName(SERVICEGROUP_NS, "MemberEPR");

    /**
     * ServiceGroupEPR resource property QName
     */
    public static final QName SERVICEGROUP_EPR =
        new QName(SERVICEGROUP_NS, "ServiceGroupEPR");

    /**
     * Content resource property QName
     */
    public static final QName CONTENT =
        new QName(SERVICEGROUP_NS, "Content");

    /**
     * Termination topic QName
     */
    public static final QName TERMINATION_TOPIC =
        new QName(LIFETIME_NS, "ResourceTermination");

    /**
     * Identifies the XPath 1.0 language.
     */
    public static final String XPATH_1_DIALECT =
        "http://www.w3.org/TR/1999/REC-xpath-19991116";

}
