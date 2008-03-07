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
package org.globus.delegation.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.security.auth.Subject;

import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.message.token.BinarySecurity;
import org.apache.ws.patched.security.message.token.PKIPathSecurity;
import org.apache.ws.patched.security.message.token.X509Security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.axis.Constants;
import org.apache.axis.MessageContext;

import org.globus.delegation.DelegationConstants;
import org.globus.delegation.DelegationException;
import org.globus.delegation.DelegationListener;
import org.globus.delegation.DelegationUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.security.gridmap.GridMap;
import org.globus.util.I18n;
import org.globus.util.Util;
import org.globus.wsrf.InvalidResourceKeyException;
import org.globus.wsrf.NoSuchResourceException;
import org.globus.wsrf.PersistenceCallback;
import org.globus.wsrf.RemoveCallback;
import org.globus.wsrf.Resource;
import org.globus.wsrf.ResourceException;
import org.globus.wsrf.ResourceIdentifier;
import org.globus.wsrf.ResourceKey;
import org.globus.wsrf.ResourceLifetime;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.impl.security.authentication.ContextCrypto;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityConfig;
import org.globus.wsrf.impl.security.descriptor.ResourceSecurityDescriptor;
import org.globus.wsrf.security.SecureResource;
import org.globus.wsrf.security.SecurityManager;
import org.globus.wsrf.utils.FilePersistenceHelper;

public class DelegationResource
    implements Resource, ResourceIdentifier, ResourceLifetime,
               SecureResource, RemoveCallback, PersistenceCallback {

    private ResourceSecurityDescriptor desc = null;

    private static I18n i18n =
        I18n.getI18n("org.globus.delegation.errors",
                     DelegationResource.class.getClassLoader());

    static Log logger = LogFactory.getLog(DelegationResource.class.getName());
    private FilePersistenceHelper persistenceHelper;
    private GlobusCredential credential = null;
    private String callerDN = null;
    private String localName = null;
    private String resourceDescPath = null;
    private Calendar terminationTime = null;
    private HashMap listeners = new HashMap();

    String resourceId;

    // Called only at resource creation time.
    public void create(BinarySecurity _token, String _callerDN,
                       String _localName, String _resourceDescPath,
                       String id)
        throws DelegationException {

        logger.debug("Create called");
        if (_token == null) {
            logger.error(i18n.getMessage("tokenNull"));
            throw new DelegationException(i18n.getMessage("tokenNull"));
        }

        this.resourceId = id;
        this.callerDN = _callerDN;
        this.resourceDescPath = _resourceDescPath;
        // Set resource desc. path
        String baseDir = null;
        MessageContext msgCtx = MessageContext.getCurrentContext();
        if (msgCtx != null) {
            baseDir = (String)msgCtx.getProperty(Constants.MC_CONFIGPATH);
        }
        if (baseDir != null) {
            this.resourceDescPath = baseDir + File.separator
                + this.resourceDescPath;
        }
        logger.debug(this.resourceDescPath);
        this.localName = _localName;
        // set token
        setToken(_token);
        // sets expiration time, shld be after setToken
        initialize();
        try {
            store();
        } catch (ResourceException exp) {
            throw new DelegationException(exp);
        }
    }

    private void initialize() throws DelegationException {

        logger.debug("Setting expiration time and security desc");
        // This is to set the termination time
        setExpirationTime();
        // Set security policies
        setResourceDescriptor();
    }

    public Object getID() {
        return this.resourceId;
    }

    // Called on refresh.
    public void storeToken(BinarySecurity token)
        throws DelegationException {
        setToken(token);
        try {
            store();
        } catch (ResourceException exp) {
            throw new DelegationException(exp);
        }
    }

    private void setToken(BinarySecurity token)
        throws DelegationException {
        X509Certificate[] certChain = getCertificateChain(token);
        PrivateKey privateKey = DelegationUtil
            .getServicePrivateKey(DelegationConstants.SERVICE_PATH, true);
        this.credential = new GlobusCredential(privateKey, certChain);
        setExpirationTime();
        notifyListeners();
    }

    public GlobusCredential getCredential() throws DelegationException {
        authorize();
        return this.credential;
    }

    public GlobusCredential getCredential(Subject subject)
        throws DelegationException {
        authorize(subject);
        return this.credential;
    }

    // Register listener. Does not have remote interface.
    public void addRefreshListener(DelegationListener listener)
        throws DelegationException {
        authorize();
        addListener(listener);
        try {
            store();
        } catch (ResourceException exp) {
            throw new DelegationException(exp);
        }
    }

    public void addRefreshListener(DelegationListener listener,
                                   Subject subject)
        throws DelegationException {

        // Ensure credential can be retrieved.
        authorize(subject);
        addListener(listener);
        try {
            store();
        } catch (ResourceException exp) {
            throw new DelegationException(exp);
        }
    }

    private void addListener(DelegationListener listener)
        throws DelegationException {

        if (listener == null) {
            logger.error(i18n.getMessage("listenerNull"));
            throw new DelegationException(i18n.getMessage("listenerNull"));
        }

        String listenerId = DelegationHome.uuidGen.nextUUID();
        listener.setId(listenerId);

        synchronized (listeners) {
            listeners.put(listenerId, listener);
        }

        // notify on registration.
        listener.setCredential(this.credential);
    }

    // Remove listener. Does not have remote interface.
    public void removeRefreshListener(String listenerId) {

        synchronized(listeners) {
            listeners.remove(listenerId);
        }
        try {
            store();
        } catch (ResourceException exp) {
            throw new RuntimeException(exp.getMessage());
        }
    }

    // Resource lifetime interface
    public void setTerminationTime(Calendar _terminationTime) {
        this.terminationTime   = _terminationTime;
        try {
            store();
        } catch (ResourceException exp) {
            throw new RuntimeException(exp.getMessage());
        }
    }

    public Calendar getTerminationTime() {
        return this.terminationTime;
    }

    public Calendar getCurrentTime() {
        return Calendar.getInstance();
    }

    private X509Certificate[] getCertificateChain(BinarySecurity token)
        throws DelegationException {
        X509Certificate[] certificates = null;
        if (token instanceof PKIPathSecurity) {
            try {
                certificates =
                    ((PKIPathSecurity)token).getX509Certificates(
                        false, new ContextCrypto());
            } catch (IOException exp) {
                logger.error(exp);
                throw new DelegationException(exp);
            }
        } else if (token instanceof X509Security) {
            certificates = new X509Certificate[1];
            try {
                certificates[0] =
                    ((X509Security)token).getX509Certificate(
                        new ContextCrypto());
            } catch(WSSecurityException e) {
                logger.error(e);
                throw new DelegationException(e);
            }
        } else {
            String err = i18n.getMessage("unsupportedToken", new Object[]
                { token.getClass().getName() });
            logger.error(err);
            throw new DelegationException(err);
        }
        return certificates;
    }

    // set termination time from certificates
    private void setExpirationTime() {

        X509Certificate[] certs = this.credential.getCertificateChain();
        Date earliestTime = null;
        for (int i=0; i<certs.length; i++) {
            Date time = certs[i].getNotAfter();
            if (earliestTime == null ||
                time.before(earliestTime)) {
                    earliestTime = time;
                }
        }

        if (this.terminationTime == null) {
            this.terminationTime = Calendar.getInstance();
        }
        this.terminationTime.setTime(earliestTime);
        logger.debug("termination time set to: "
                     + this.terminationTime.getTime());
    }

    // Notify listeners
    private void notifyListeners() throws DelegationException {

        synchronized (listeners) {
            Iterator iter = listeners.entrySet().iterator();
            while (iter.hasNext()) {
                DelegationListener refListener
                    = (DelegationListener)(((Map.Entry)iter.next())
                                           .getValue());
                refListener.setCredential(this.credential);
            }
        }
    }

    // Secure resource interface
    public ResourceSecurityDescriptor getSecurityDescriptor() {
        return this.desc;
    }

    private void setResourceDescriptor() throws DelegationException {
        ResourceSecurityConfig securityConfig =
            new ResourceSecurityConfig(this.resourceDescPath);
        try {
            securityConfig.init();
        } catch (ConfigException exp) {
            logger.error(i18n.getMessage("securityDescInitErr"), exp);
            throw new DelegationException(i18n
                                          .getMessage("securityDescInitErr"),
                                          exp);
        }
        this.desc = securityConfig.getSecurityDescriptor();
        GridMap gridMap = new GridMap();
        gridMap.map(this.callerDN, this.localName);
        this.desc.setGridMap(gridMap);
    }

    private void authorize(Subject subject) throws DelegationException {
        Principal principal = SecurityManager.getManager()
            .getCallerPrincipal(subject);
        String callerDN = null;
        if (principal != null) {
            callerDN = principal.getName();
        }
        authorize(callerDN);
    }

    private void authorize() throws DelegationException {

        // returns caller dn (PEER_SUBJECT) associated with the
        // message context. If message context is null, an attempt to
        // get the current message context is done.
        String callerDN = SecurityManager.getManager().getCaller();
        logger.debug("Caller DN from message context is " + callerDN);
        authorize(callerDN);
    }

    private void authorize(String callerDN) throws DelegationException {

        if (callerDN == null) {
            logger.error(i18n.getMessage("unknownCaller"));
            throw new DelegationException(i18n.getMessage("unknownCaller"));
        }

        // assert callerDN is authz to access this resource
        if (this.desc == null) {
            logger.error(i18n.getMessage("securityDescNull"));
            throw new IllegalStateException(i18n
                                            .getMessage("securityDescNull"));
        }

        GridMap gridMap = this.desc.getGridMap();
        if (gridMap == null) {
            logger.error(i18n.getMessage("gridMapNull"));
            throw new IllegalStateException(i18n.getMessage("gridMapNull"));
        }

        String[] username = gridMap.getUserIDs(callerDN);
        if ((username == null) || (username.length < 1)) {
            logger.error(i18n.getMessage("unAuthzCaller", new Object[]
                { callerDN }));
            throw new DelegationException(i18n.getMessage("unAuthzCaller",
                                                          new Object[]
                { callerDN }));
        }
    }

    /**
     * Loads and sets up the resource - credential, expiration time,
     * listeners and security descriptor.
     */
    public void load(ResourceKey key) throws ResourceException,
                                             NoSuchResourceException,
                                             InvalidResourceKeyException {

        this.resourceId = (String)key.getValue();
        File file = getKeyAsFile(this.resourceId);
        if (!file.exists()) {
            throw new NoSuchResourceException();
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            this.callerDN = (String)ois.readObject();
            this.localName = (String)ois.readObject();
            this.resourceDescPath = (String)ois.readObject();
            this.terminationTime = (Calendar)ois.readObject();
            this.listeners = (HashMap)ois.readObject();
            this.credential = new GlobusCredential(ois);
            initialize();
        } catch (Exception e) {
            throw new ResourceException(i18n.getMessage("resourceLoadErr"), e);
        } finally {
            if (fis != null) {
                try { fis.close(); } catch (Exception ee) {}
            }
        }
    }

    /**
     * Persists the credential, callerDN, localName, resourceDescPath,
     * termination time and listeners
     */
    public synchronized void store() throws ResourceException {

        // Store credential, callerDN, localName, resourceDescPath,
        FileOutputStream fos = null;
        File tmpFile = null;
        try {
            tmpFile = File.createTempFile(
                "delegation", ".tmp",
                getPersistenceHelper().getStorageDirectory());

            if (!Util.setFilePermissions(tmpFile.getAbsolutePath(), 700)) {
                logger.warn(i18n.getMessage("checkFilePerms", new Object[]
                    { tmpFile.getAbsolutePath() }));
            }
            fos = new FileOutputStream(tmpFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this.callerDN);
            oos.writeObject(this.localName);
            oos.writeObject(this.resourceDescPath);
            oos.writeObject(this.terminationTime);
            synchronized(listeners) {
                oos.writeObject(this.listeners);
            }
            this.credential.save(oos);
        } catch (Exception e) {
            if (tmpFile != null) {
                tmpFile.delete();
            }
            throw new ResourceException(i18n.getMessage("resourceStoreErr"),
                                        e);
        } finally {
            if (fos != null) {
                try { fos.close();} catch (Exception ee) {}
            }
        }

        // Overwrite old file
        File file = getKeyAsFile(this.resourceId);
        if (file.exists()) {
            file.delete();
        }

        if (!tmpFile.renameTo(file)) {
            tmpFile.delete();
            throw new ResourceException(i18n.getMessage("resourceStoreErr"));
        }
        // Check if rename to changes the file permissions.
        if (!Util.setFilePermissions(file.getAbsolutePath(), 700)) {
            logger.warn(i18n.getMessage("checkFilePerms", new Object[]
                { file.getAbsolutePath() }));
        }
    }

    protected synchronized FilePersistenceHelper getPersistenceHelper() {
        if (this.persistenceHelper == null) {
            try {
                this.persistenceHelper =
                    new FilePersistenceHelper(getClass(), ".ser");
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
        return this.persistenceHelper;
    }

    private File getKeyAsFile(Object key)
        throws InvalidResourceKeyException {
        if (key instanceof String) {
            return getPersistenceHelper().getKeyAsFile(key);
        } else {
            throw new InvalidResourceKeyException();
        }
    }

    private void notifyCredentialDelete() {
        logger.debug("notify removal");
        synchronized (listeners) {
            Iterator iter = listeners.entrySet().iterator();
            while (iter.hasNext()) {
                DelegationListener refListener
                    = (DelegationListener)(((Map.Entry)iter.next())
                                           .getValue());
                refListener.credentialDeleted();
            }
        }
    }

    public void remove() throws ResourceException {
        logger.debug("Remove called");
        notifyCredentialDelete();
        getPersistenceHelper().remove(this.resourceId);
    }
}
