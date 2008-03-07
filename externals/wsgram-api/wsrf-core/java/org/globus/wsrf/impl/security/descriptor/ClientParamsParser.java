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
package org.globus.wsrf.impl.security.descriptor;

import org.globus.wsrf.impl.security.descriptor.util.ElementHandler;
import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

import org.globus.wsrf.impl.security.util.AuthUtil;

import org.globus.wsrf.impl.security.authorization.Authorization;

import org.globus.wsrf.impl.security.authentication.Constants;

import org.globus.axis.gsi.GSIConstants;

import org.globus.util.I18n;

import org.w3c.dom.Node;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/**
 * Handles all elements that are set on the client security descriptor
 * other than elements that specify credentials.
 */
public class ClientParamsParser implements ElementHandler {

    private static I18n i18n = I18n.getI18n(SecurityDescriptor.RESOURCE);

    public static final String AUTHZ_NAME = "authz";
    public static final String ATTRIB = "value";

    public static final QName AUTHZ_QNAME =
        new QName(SecurityDescriptor.NS, AUTHZ_NAME);
    public static final QName SEC_CONV_QNAME =
        new QName(SecurityDescriptor.NS, AuthMethodParser.AUTH_GSI_SEC_CONV);
    public static final QName SEC_MSG_QNAME =
        new QName(SecurityDescriptor.NS, AuthMethodParser.AUTH_GSI_SEC_MSG);
    
    protected ClientParamsParserCallback callback;

    public ClientParamsParser(ClientParamsParserCallback callback) {
        super();
        this.callback = callback;
    }

    public void parse(Element elem) throws ElementParserException {

        String name = elem.getLocalName();
        
        if (name.equalsIgnoreCase(AUTHZ_NAME)) {
            String val = elem.getAttribute(ATTRIB);

            Authorization authz = AuthUtil.getClientAuthorization(val);
            this.callback.setAuthz(authz);

        } else if (name.equalsIgnoreCase(AuthMethodParser.AUTH_GSI_SEC_CONV)) {

            boolean protLevelSet = false;
            for (Node currentChild = elem.getFirstChild(); 
                 currentChild != null;
                currentChild = currentChild.getNextSibling()) {
                
                if (currentChild.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }

                String childName = currentChild.getLocalName();

                if ((!protLevelSet) && 
                    (childName.equalsIgnoreCase("integrity"))) {
                    this.callback.setGSISecureConv(Constants.SIGNATURE);
                    protLevelSet = true;
                } else if ((!protLevelSet) && 
                           (childName.equalsIgnoreCase("privacy"))) {
                    this.callback.setGSISecureConv(Constants.ENCRYPTION);
                    protLevelSet = true;
                } else if (childName.equalsIgnoreCase("anonymous")) {
                    this.callback.setAnonymous();
                } else if (childName.equalsIgnoreCase("delegation")) {
                    String val = ((Element)currentChild).getAttribute(ATTRIB);
                    if (val.equalsIgnoreCase("limited")) {
                        this.callback.setDelegation(
                                      GSIConstants.GSI_MODE_LIMITED_DELEG);
                    } else if (val.equalsIgnoreCase("full")) {
                        this.callback.setDelegation(
                                      GSIConstants.GSI_MODE_FULL_DELEG);
                    }
                } else {
                    throw new SecurityDescriptorException(
                              i18n.getMessage("unsupportedElement", name));
                }
            }
        } else if (name.equalsIgnoreCase(AuthMethodParser.AUTH_GSI_SEC_MSG)) {
            boolean protLevelSet = false;
            for (Node currentChild = elem.getFirstChild(); 
                 currentChild != null;
                 currentChild = currentChild.getNextSibling()) {
                
                if (currentChild.getNodeType() != Node.ELEMENT_NODE) {
                    continue;
                }
                
                String childName = currentChild.getLocalName();
                
                if ((!protLevelSet) && 
                    (childName.equalsIgnoreCase("integrity"))) {
                    this.callback.setGSISecureMsg(Constants.SIGNATURE);
                    protLevelSet = true;
                } else if ((!protLevelSet) && 
                           (childName.equalsIgnoreCase("privacy"))) {
                    this.callback.setGSISecureMsg(Constants.ENCRYPTION);
                    protLevelSet = true;
                } else if (childName.equalsIgnoreCase("peer-credentials")) {
                    this.callback.setPeerCredentials(
                                 ((Element)currentChild).getAttribute(ATTRIB));
                } else {
                    throw new SecurityDescriptorException(
                              i18n.getMessage("unsupportedElement", name));
                }
            }
        }
    }
}
