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
package org.globus.wsrf.impl.security.authorization;

import org.opensaml.QName;
import org.opensaml.XML;

/**
 * Constants used by <code>SAMLAuthorizationCallout</code>
 */
public class SAMLAuthorizationConstants {

    final public static String ACTION_OPERATION_NS
        = "http://www.gridforum.org/namespaces/2003/06/ogsa-authorization/saml"
        + "/action/operation";
    final public static String ANY_SUBJECT_NAME = "*";
    final public static String ANY_NAME_IDENTIFIER_FORMAT =
        "http://www.gridforum.org/ogsa-authz/saml/2003/06/NameIdentifier/any";
    final public static String X509_FORMAT =
        "urn:oasis:names:tc:SAML:1.1:nameid-format:X509SubjectName";
    final public static String X509_CONFIRMATION_METHOD =
        "urn:oasis:names:tc:SAML:1.0:am:X509-PKI";
    final public static String AUTHZ_DECISION_STMT =
        "AuthorizationDecisionStatement";
    final public static String SIMPLE_AUTHZ_DECISION_STMT =
        "SimpleAuthorizationDecisionStatement";

    public static QName AUTHZ_DECISION =
        new org.opensaml.QName(XML.OPENSAML_NS, AUTHZ_DECISION_STMT);

    public static QName SIMPLE_AUTHZ_DECISION =
        new QName(XML.OGSA_AUTHZ_SAML_NS, SIMPLE_AUTHZ_DECISION_STMT);

    public static final javax.xml.namespace.QName RP_INDETERMINATE =
        new javax.xml.namespace.QName("http://www.gridforum.org/namespaces/2004/03/ogsa-authz/saml", "supportsIndeterminate");

    public static final javax.xml.namespace.QName RP_SIGNATURE =
        new javax.xml.namespace.QName("http://www.gridforum.org/namespaces/2004/03/ogsa-authz/saml", "signatureCapable");
    public final static String SIMPLE_DECISION_PROPERTY =
        "samlAuthzSimpleDecision";
    public final static String REQ_SIGNED_PROPERTY =
        "samlAuthzReqSigned";
    public static final String AUTHZ_SERVICE_PROPERTY = "authzService";
    public static final String AUTHZ_SERVICE_IDENTITY_PROPERTY =
        "authzServiceIdentity";
    public static final String PROTECTION_LEVEL_PROPERTY = "protectionLevel";
    public static final String SECURITY_MECHANISM_PROPERTY =
        "securityMechanism";
    public static final String AUTHZ_SERVICE_CERT_FILE_PROPERTY =
        "authzServiceCertificateFile";
    public static final String AUTHZ_SERVICE_CERT_PROPERTY =
        "authzServiceCertificate";
    public static final String INTEGRITY = "sig";
    public static final String PRIVACY = "enc";
    public static final String MESSAGE = "msg";
    public static final String CONVERSATION = "conv";
    public static final String NONE = "none";
}
