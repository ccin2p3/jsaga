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

import java.util.Vector;

import javax.xml.rpc.Stub;

import org.ietf.jgss.GSSCredential;

import org.globus.util.I18n;

import org.globus.axis.gsi.GSIConstants;
import org.globus.security.gridmap.GridMap;

import org.globus.wsrf.impl.security.authentication.Constants;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.authorization.HostAuthorization;
import org.globus.wsrf.impl.security.authorization.IdentityAuthorization;
import org.globus.wsrf.impl.security.authorization.SelfAuthorization;
import org.globus.wsrf.impl.security.descriptor.ClientSecurityDescriptor;
import org.globus.wsrf.impl.security.descriptor.GSISecureMsgAuthMethod;
import org.globus.wsrf.impl.security.descriptor.GSITransportAuthMethod;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;

import org.globus.rendezvous.client.ClientSecurityStrategy;

/**
 * Security settings for remote calls to a managed job port type
 */
public class JobClientSecurityStrategy implements ClientSecurityStrategy {

    public JobClientSecurityStrategy() {
    }

    public void setStubPropertiesForGetResourceProperty(Stub stub) {
        setStubPropertiesForDefaultOperation(stub);
    }

    public void setStubPropertiesForSubscribe(Stub stub) {
        setStubPropertiesForDefaultOperation(stub);
    }

    public void setStubPropertiesForRegister(Stub stub) {
        setStubPropertiesForDefaultOperation(stub);
    }

    public ResourceSecurityDescriptor getSecurityDescriptor() {
        //Authorization authz = HostAuthorization.getInstance(); //or Authorization.AUTHZ_NONE?
        String authz = null;
        ResourceSecurityDescriptor securityDescriptor
                = new ResourceSecurityDescriptor();
        if (authorization == null) {
            authz = Authorization.AUTHZ_NONE;
        }
        else if (authorization instanceof HostAuthorization) {
            authz = Authorization.AUTHZ_NONE;
        }
        else if (authorization instanceof SelfAuthorization) {
            authz = Authorization.AUTHZ_SELF;
        }
        else if (authorization instanceof IdentityAuthorization) {
            GridMap gridMap = new GridMap();
            gridMap.map(
                ( (IdentityAuthorization) authorization).getIdentity(),
                "HaCk");
            securityDescriptor.setGridMap(gridMap);

            authz = Authorization.AUTHZ_GRIDMAP;
        }
        else {
            String errorMessage =
                i18n.getMessage(Resources.UNKNOWN_AUTHORIZATION,
                                authorization.getClass().getName());
            throw new RuntimeException(errorMessage);
        }

        securityDescriptor.setAuthz(authz);
        Vector authMethod = new Vector();
        //authMethod.add(GSISecureMsgAuthMethod.BOTH);
        authMethod.add(GSITransportAuthMethod.BOTH);
        try {
            securityDescriptor.setAuthMethods(authMethod);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        return securityDescriptor;
    }


    public void setStubPropertiesForDefaultOperation(Stub stub)
    {
        ClientSecurityDescriptor secDesc = new ClientSecurityDescriptor();

        secDesc.setAuthz(this.getAuthorization());

        secDesc.setGSITransport(this.getMessageProtectionType());

        if (this.proxy != null)
        {
            secDesc.setGSSCredential(this.proxy);
        }

        stub._setProperty(Constants.CLIENT_DESCRIPTOR, secDesc);
    }

    public static final Integer DEFAULT_MSG_PROTECTION =
        Constants.SIGNATURE;

    public static final Authorization DEFAULT_AUTHZ =
        HostAuthorization.getInstance();


    public void setAuthorization(Authorization auth) {
        this.authorization = auth;
    }

    public Authorization getAuthorization() {
        return (authorization == null) ?
               DEFAULT_AUTHZ :
               this.authorization;
    }

    public void setMessageProtectionType(Integer protectionType) {
        this.msgProtectionType = protectionType;
    }

    public Integer getMessageProtectionType() {
        return (this.msgProtectionType == null) ?
               this.DEFAULT_MSG_PROTECTION :
               this.msgProtectionType;
    }

    /**
    * Gets the credentials of this job.
    *
    * @return job credentials. If null none were set.
    *
    */
    public GSSCredential getCredentials() {
        return this.proxy;
    }

    /**
    * Sets credentials of the job
    *
    * @param  newProxy user credentials
    * @throws IllegalArgumentException if credentials are already set
    */
    public void setCredentials(GSSCredential newProxy) {
        if (this.proxy != null) {
            throw new IllegalArgumentException("Credentials already set");
        } else {
            this.proxy = newProxy;
        }
    }


    private Integer msgProtectionType = null;
    private Authorization authorization = null;
    // holds job credentials
    private GSSCredential proxy = null;

    private static I18n i18n = I18n.getI18n(Resources.class.getName());
}
