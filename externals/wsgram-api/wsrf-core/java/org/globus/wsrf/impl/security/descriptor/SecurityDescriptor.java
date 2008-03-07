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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.ObjectOutputStream;

import org.globus.wsrf.impl.security.descriptor.util.ElementParser;

import org.globus.wsrf.impl.security.util.AuthUtil;
import org.globus.wsrf.impl.security.util.FixedObjectInputStream;
import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

import org.globus.util.I18n;

import javax.xml.namespace.QName;

import javax.security.auth.Subject;

import org.globus.security.gridmap.GridMap;

/**
 * Base class for security descriptor. Stores credential parameters,
 * global paramaters (reject limited proxy, authorization, gridmap,
 * replay filter, replay window, context lifetime). Also, used to
 * store <code>Subject</code>(created off credentials),
 * <code>GridMap<code> and if authorization class is configured, an
 * instance of class that implements <code>ServiceAuthorization<code>
 */
public class SecurityDescriptor extends ElementParser
    implements GlobalParamsParserCallback, CredentialParamsParserCallback, 
               AuthzParamParserCallback, Serializable {

    public static final String RESOURCE =
        "org.globus.wsrf.impl.security.descriptor.errors";
    protected static I18n i18n = I18n.getI18n(RESOURCE);

    public static final String NS = "http://www.globus.org";
    private static final QName QNAME = new QName(NS, "securityConfig");

    // Configured property
    private String rejectLimitedProxy = null;
    private String proxyFile = null;
    private String certFile = null;
    private String keyFile = null;
    private String authz = null;
    private String gridMapFile = null;
    private String replayFilter = null;
    private String replayWindow = null;
    private Integer contextLifetime = null;

    // Property that is set 
    private Subject subject = null;
    private Long lastModified = null;
    private GridMap gridMap = null;
    private ServiceAuthorizationChain authzChain = null;

    public SecurityDescriptor() {
        super(QNAME);

        register(GlobalParamsParser.CONTEXT_LIFETIME_QNAME, 
                 new GlobalParamsParser(this));
        register(GlobalParamsParser.REJECT_LIMITED_PROXY_QNAME, 
                 new GlobalParamsParser(this));
        register(GlobalParamsParser.GRID_MAP_QNAME, 
                 new GlobalParamsParser(this));
        register(GlobalParamsParser.REPLAY_ATTACK_FILTER_QNAME, 
                 new GlobalParamsParser(this));
        register(GlobalParamsParser.REPLAY_ATTACK_WINDOW_QNAME, 
                 new GlobalParamsParser(this));
        register(CredentialParamsParser.PROXY_FILE_QNAME, 
                 new CredentialParamsParser(this));
        register(CredentialParamsParser.CREDENTIAL_QNAME, 
                 new CredentialParamsParser(this));
        register(AuthzParamParser.AUTHZ_QNAME, 
                 new AuthzParamParser(this));
    }

    /**
     * Sets whether limited proxy should be rejected or not
     * 
     * @param value
     *        If true, rejects limited proxy. If not, allows limited
     *        proxy
     */
    public void setRejectLimitedProxy(String value) {
        rejectLimitedProxy = value;
    }
    
    /**
     * Returns if limited proxy can be rejected or not
     *
     * @return string that indicated limited proxy rejection.
     */
    public String getRejectLimitedProxyState() {
        return rejectLimitedProxy;
    }

    /**
     * Sets proxy file to use
     *
     * @param value
     *         Filename of proxy
     */
    public void setProxyFilename(String value) {
        this.proxyFile = value;
    }

    /**
     * Sets the filenames to pick up certificate and key
     *
     * @param certName
     *        Name of certificate file
     * @param keyName
     *        Name of key file
     */
    public void setCertificateFiles(String certName, String keyName) {
        this.certFile = certName;
        this.keyFile = keyName;
    }

    /**
     * Returns the filename of proxy
     */
    public String getProxyFilename() {
        return this.proxyFile;
    }

    /**
     * Returns the certificate filename
     */
    public String getCertFilename() {
        return this.certFile;
    }

    /**
     * Returns the key filename
     */
    public String getKeyFilename() {
        return this.keyFile;
    }
    
    /**
     * Sets the authorization mechanism to use. 
     *
     * @param value
     *        String representing the authz mechanism to use. See 
     * <code>org.globus.wsrf.impl.security.authorization.Authorization</code>
     */
    public void setAuthz(String value) {
        this.authz = value;
    }

    /**
     * Returns the authorization mechanism. See 
     *
     * @return String representing the authz mechanism to use. See 
     * <code>org.globus.wsrf.impl.security.authorization.Authorization</code>
     */
    public String getAuthz() {
        return this.authz;
    }

    /**
     * Sets an authorization chain instance.
     *
     * @param chain
     *        Instance of ServiceAuthorizationChain
     */
    public void setAuthzChain(ServiceAuthorizationChain chain) {
        this.authzChain = chain;
    }
    
    /**
     * Returns an authorization chain instance.
     *
     * @return Instance of ServiceAuthorizationChain
     */
    public ServiceAuthorizationChain getAuthzChain() {
        return this.authzChain;
    }

    /**
     * Sets grid map filename
     * 
     * @param value
     *        Gridmap filename
     */
    public void setGridMapFile(String value) {
        this.gridMapFile = value;
    }

    /**
     * Returns grid map file name
     *
     * @return Grid map file name
     */
    public String getGridMapFile() {
        return this.gridMapFile;
    }

    /**
     * Sets replay attack filter value. If set to true, replay
     * attack prevention is enforced. If not, it is not enforced.
     *
     * @param value
     *        String to indicate if replay attack filter is required.
     */
    public void setReplayAttackFilter(String value) {
        this.replayFilter = value;
    }

    /**
     * Returns the set replay attack filter value
     *
     * @return configured replay attack string. 
     */
    public String getReplayAttackFilter() {
        return this.replayFilter;
    }

    /**
     * Sets replay attack window size
     *
     * @param value
     *        String representation of replay attack prevention window value in
     * minutes
     */
    public void setReplayAttackWindow(String value) {
        this.replayWindow = value;
    }

    /**
     * Returns replay attack prevention window value in minutes
     *
     * @return replay attack [revention window
     */
    public String getReplayAttackWindow() {
        return this.replayWindow;
    }

    /**
     * Sets the lifetime to be set on context when secure conversation
     * is used
     *
     * @param lifetime
     *        lifetime of context
     */
    public void setContextLifetime(Integer lifetime) {
        contextLifetime = lifetime;
    }
    
    /**
     * Returns the lifetime of context created when secure
     * conversation is used.
     *
     * @return lifetime of context
     */
    public Integer getContextLifetime() {
        return contextLifetime;
    }

    /**
     * Sets the subject
     */
    public void setSubject(Subject sub) {
        this.subject = sub;
    }

    /**
     * Sets the time when the credential/proxy file was last modified
     */
    public void setLastModified(Long modified) {
        this.lastModified = modified;
    }

    /**
     * Sets the gridmap object
     */
    public void setGridMap(GridMap map) {
        this.gridMap = map;
    }

    /**
     * Returns the subject
     */
    public Subject getSubject() {
        return this.subject;
    }

    /**
     * Returns the time when the credential/proxy file was last modified
     */
    public Long getLastModified() {
        return this.lastModified;
    }

    /**
     * Returns gridmap object
     */
    public GridMap getGridMap() {
        return this.gridMap;
    }

    /**
     * Returns if the credential object needs to be refreshed based on
     * the timestamp on configured file and last modified value.
     */
    public boolean isRefreshRequired() {
        String credFile = getCertFilename();
        if (credFile == null) {
            credFile = getProxyFilename();
        }
        if (credFile == null) {
            return false;
        }
        File file = new File(credFile);
        Long lastModified = getLastModified();
        
        return ((lastModified == null) || 
                (lastModified.longValue() != file.lastModified()));
    }

    protected void writeObject(ObjectOutputStream oos) throws IOException {
        oos.writeObject(this.rejectLimitedProxy);
        oos.writeObject(this.proxyFile);
        oos.writeObject(this.certFile);
        oos.writeObject(this.keyFile);
        oos.writeObject(this.authz);
        oos.writeObject(this.gridMapFile);
        oos.writeObject(this.replayFilter);
        oos.writeObject(this.replayWindow);
        oos.writeObject(this.contextLifetime);
        AuthUtil.writeSubject(this.subject, oos);
        oos.writeObject(this.lastModified);
        oos.writeObject(this.gridMap);
        oos.writeObject(this.authzChain);
    }

    protected void readObject(FixedObjectInputStream ois) 
        throws IOException, ClassNotFoundException {

        this.rejectLimitedProxy = (String)ois.readObject();
        this.proxyFile = (String)ois.readObject();
        this.certFile= (String)ois.readObject();
        this.keyFile = (String)ois.readObject();
        this.authz = (String)ois.readObject();
        this.gridMapFile = (String)ois.readObject();
        this.replayFilter = (String)ois.readObject();
        this.replayWindow = (String)ois.readObject();
        this.contextLifetime = (Integer)ois.readObject();
        this.subject = AuthUtil.readSubject(ois);
        this.lastModified = (Long)ois.readObject();
        this.gridMap = (GridMap)ois.readObject();
        this.authzChain = (ServiceAuthorizationChain)ois.readObject();
    }
}
