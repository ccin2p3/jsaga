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

import java.io.Serializable;

import javax.security.auth.Subject;
import javax.xml.namespace.QName;

import org.ietf.jgss.GSSCredential;

import java.io.IOException;
import java.io.ObjectOutputStream;

import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.impl.security.util.FixedObjectInputStream;

import org.globus.util.I18n;
import org.globus.wsrf.impl.security.authorization.Authorization;
import org.globus.wsrf.impl.security.descriptor.util.ElementParser;

/**
 * Represents a client's security descriptor.
 */
public class ClientSecurityDescriptor extends ElementParser
    implements CredentialParamsParserCallback, ClientParamsParserCallback,
               Serializable {

    public static final String RESOURCE =
        "org.globus.wsrf.impl.security.descriptor.errors";
    protected static I18n i18n = I18n.getI18n(RESOURCE);

    public static final String NS = "http://www.globus.org";
    private static final QName QNAME = new QName(NS, "securityConfig");

    private Long lastModified = null;
    private String proxyFile = null;
    private String certFile = null;
    private String keyFile = null;
    private String peerCredFile = null;
    private Authorization authz = null;
    private Integer gsiSecConv = null;
    private Integer gsiTransport = null;
    private Integer gsiSecMsg = null;
    private Boolean anonymous = null;
    private String delegation = null;
    private GSSCredential gsiCred = null;
    private Subject peerSubject = null;

    public ClientSecurityDescriptor() {
        super(QNAME);

        register(CredentialParamsParser.PROXY_FILE_QNAME,
                 new CredentialParamsParser(this));
        register(CredentialParamsParser.CREDENTIAL_QNAME,
                 new CredentialParamsParser(this));
        register(ClientParamsParser.AUTHZ_QNAME,
                 new ClientParamsParser(this));
        register(ClientParamsParser.SEC_CONV_QNAME,
                 new ClientParamsParser(this));
        register(ClientParamsParser.SEC_MSG_QNAME,
                 new ClientParamsParser(this));
    }

    /**
     * Sets the file name of the proxy to load credentials from
     */
    public void setProxyFilename(String value) {
        this.proxyFile = value;
    }

    /**
     * Sets the file name certificate and key file to load
     * credentials from.
     * 
     * @param certName
     *        Name of file to load certificate from
     * @param keyName
     *        Name of file to load key from.
     */
    public void setCertificateFiles(String certName, String keyName) {
        this.certFile = certName;
        this.keyFile = keyName;
    }

    /**
     * Returns the file name of the proxy 
     */
    public String getProxyFilename() {
        return this.proxyFile;
    }

    /**
     * Returns the filename from which certificate was loaded
     */
    public String getCertFilename() {
        return this.certFile;
    }

    /**
     * Returns the filename from which key was laoded
     */
    public String getKeyFilename() {
        return this.keyFile;
    }

    /**
     * sets the authorization scheme to be used on the client side.
     */
    public void setAuthz(Authorization value) {
        this.authz = value;
    }

    /**
     * Returns the authorization scheme configured on client side.
     */
    public Authorization getAuthz() {
        return this.authz;
    }

    /**
     * Indicates that GSI Secure Conversation needs to be used on
     * client side and configured the protection that is required.
     *
     * @param val
     *        Either Constants.SIGNATURE or Constants.ENCRYPTION
     */
    public void setGSISecureConv(Integer val) {
        this.gsiSecConv = val;
    }

    /**
     * Returns the protection type configured with GSI Secure
     * Conversation. If null is returned, this method was not configured
     * to be used.
     */
    public Integer getGSISecureConv() {
        return this.gsiSecConv;
    }

    /**
     * Indicates that GSI Secure Transport needs to be used on
     * client side and configured the protection that is required.
     * Atleast integrity protection is always turned on.
     *
     * @param val
     *        Either Constants.SIGNATURE or Constants.ENCRYPTION
     */
    public void setGSITransport(Integer val) {
        this.gsiTransport = val;
    }

    /**
     * Returns the protection type configured with GSI Secure
     * Transport If null is returned, this method was not configured
     * to be used.
     */
    public Integer getGSITransport() {
        return this.gsiTransport;
    }

    /**
     * Indicates that GSI Secure Message needs to be used on
     * client side and configured the protection that is required.
     *
     *
     * @param val
     *        Either Constants.SIGNATURE or Constants.ENCRYPTION
     */
    public void setGSISecureMsg(Integer val) {
        this.gsiSecMsg = val;
    }

    /**
     * Returns the protection type configured with GSI Transport
     * Transport If null is returned, this method was not configured
     * to be used.
     */
    public Integer getGSISecureMsg() {
        return gsiSecMsg;
    }

    /**
     * If set, client is used in anonymous mode. Applicable for GSI
     * Transport and GSI Secure Conversation.
     */
    public void setAnonymous() {
        this.anonymous = Boolean.TRUE;
    }

    /**
     * Returns if client is anonymous
     */
    public Boolean getAnonymous() {
        return this.anonymous;
    }

    /**
     * Sets type of delegation. Can be limited or full. Applicable
     * only for GSI Secure Conversation
     */
    public void setDelegation(String deleg) {
        this.delegation = deleg;
    }

    /**
     * Returns type of delegation
     */
    public String getDelegation() {
        return this.delegation;
    }

    /**
     * Sets file name from which to load credentials to be used for
     * encryption. 
     */
    public void setPeerCredentials(String str) {
        this.peerCredFile = str;
    }

    public String getPeerCredentials() {
        return this.peerCredFile;
    }

    /**
     * Sets the time when the credential/proxy file was last modified
     */
    public void setLastModified(Long modified) {
        this.lastModified = modified;
    }

    /**
     * Returns the time when the credential/proxy file was last modified
     */
    public Long getLastModified() {
        return this.lastModified;
    }

    /**
     * Sets the credential to use on client side for securing message
     */
    public void setGSSCredential(GSSCredential cred) {
        this.gsiCred = cred;
    }

    public GSSCredential getGSSCredential() {
        return this.gsiCred;
    }

    /**
     * Sets the subject to use for encryption
     */
    public void setPeerSubject(Subject subject) {
        this.peerSubject = subject;
    }

    public Subject getPeerSubject() {
        return this.peerSubject;
    }

    protected void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.lastModified);
        oos.writeObject(this.proxyFile);
        oos.writeObject(this.certFile);
        oos.writeObject(this.keyFile);
        oos.writeObject(this.peerCredFile);
        oos.writeObject(this.authz);
        oos.writeObject(this.gsiSecConv);
        oos.writeObject(this.gsiSecMsg);
        oos.writeObject(this.gsiTransport);
        oos.writeObject(this.anonymous);
        oos.writeObject(this.delegation);
        oos.writeObject(this.gsiCred);
        AuthUtil.writeSubject(this.peerSubject, oos);
    }

    protected void readObject(FixedObjectInputStream ois) 
        throws IOException, ClassNotFoundException {
        
        this.lastModified = (Long)ois.readObject();
        this.proxyFile = (String)ois.readObject();
        this.certFile = (String)ois.readObject();
        this.keyFile = (String)ois.readObject();
        this.peerCredFile = (String)ois.readObject();
        this.authz = (Authorization)ois.readObject();
        this.gsiSecConv = (Integer)ois.readObject();
        this.gsiSecMsg = (Integer)ois.readObject();
        this.gsiTransport = (Integer)ois.readObject();
        this.anonymous = (Boolean)ois.readObject();
        this.delegation = (String)ois.readObject();
        this.gsiCred = (GSSCredential)ois.readObject();
        this.peerSubject = AuthUtil.readSubject(ois);
    }
}
