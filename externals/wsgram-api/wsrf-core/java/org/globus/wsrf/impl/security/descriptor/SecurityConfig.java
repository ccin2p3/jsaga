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

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import org.globus.wsrf.Constants;

import org.globus.wsrf.config.ConfigException;

import org.globus.wsrf.jndi.JNDIUtils;
import org.globus.wsrf.utils.XmlUtils;



import org.globus.util.I18n;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.w3c.dom.Document;

import javax.security.auth.Subject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.axis.MessageContext;
import org.globus.gsi.CredentialException;
import org.globus.gsi.X509Credential;
import org.globus.gsi.gridmap.GridMap;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
import org.globus.gsi.gssapi.JaasGssUtil;

import org.globus.wsrf.impl.security.authorization.ServiceAuthorizationChain;

/**
 * Helper base class for initialization and storing of security
 * descriptor in JNDI
 */
public abstract class SecurityConfig {

    private static Log logger =
        LogFactory.getLog(SecurityConfig.class.getName());

    private static I18n i18n =
        I18n.getI18n(SecurityDescriptor.RESOURCE,
                     SecurityConfig.class.getClassLoader());

    // Property in config
    public final static String CONT_SEC_DESCRIPTOR = "containerSecDesc";
    // Security properties in JNDI
    public final static String SECURITY_PROP = "securityProp";
    // For security descriptor
    public final static String SECURITY_DESCRIPTOR = "securityDescriptor";
    public final static String SECURITY_INIT_NAME = "securityInitialied";

    String jndiPathName = null;
    String descriptorFile = null;
    SecurityDescriptor desc = null;

    SecurityConfig() {
    }

    protected SecurityConfig(String jndiPathName, String descFileName) {
        this.jndiPathName = jndiPathName;
        this.descriptorFile = descFileName;
    }

    protected SecurityConfig(SecurityDescriptor desc) {
        this.desc = desc;
    }

    protected SecurityConfig(String jndiPathName, SecurityDescriptor desc) {
        this.jndiPathName = jndiPathName;
        this.desc = desc;
    }

    // Abstract methods
    protected abstract void initSecurityDescriptor(Document doc)
        throws ConfigException;

    protected abstract void initCredentials() throws ConfigException;

    protected abstract void loadAuthorization() throws ConfigException;

    // protected methods
    protected static void storeSubject(Subject subject, String jndiPath,
                                       SecurityDescriptor desc)
        throws ConfigException {
        if (desc != null) {
            desc.setSubject(subject);
            storeSecurityDescriptor(desc, jndiPath);
        }
    }

    protected static void storeGridMap(GridMap gridmap, String jndiPath,
                                       SecurityDescriptor desc)
        throws ConfigException {
        if (desc != null) {
            desc.setGridMap(gridmap);
            storeSecurityDescriptor(desc, jndiPath);
        }
    }

    protected static void storeAuthzChain(ServiceAuthorizationChain authzChain,
                                          String jndiPath,
                                          SecurityDescriptor desc)
        throws ConfigException {
        if (desc != null) {
            desc.setAuthzChain(authzChain);
            storeSecurityDescriptor(desc, jndiPath);
        }
    }

    protected static void storeSecurityDescriptor(SecurityDescriptor desc,
                                                  String jndiPath)
        throws ConfigException {
        putObject(SECURITY_DESCRIPTOR, desc, jndiPath);
    }

    protected static Subject retrieveSubject(String jndiPath)
        throws ConfigException {
        SecurityDescriptor desc = retrieveSecurityDescriptor(jndiPath);
        return (desc == null) ? null : desc.getSubject();
    }

    protected static GridMap retrieveGridMap(String jndiPath)
        throws ConfigException {
        SecurityDescriptor desc = retrieveSecurityDescriptor(jndiPath);
        return (desc == null) ? null : desc.getGridMap();
    }

    protected static ServiceAuthorizationChain
        retrieveAuthzChain(String jndiPath)
        throws ConfigException {
        SecurityDescriptor desc = retrieveSecurityDescriptor(jndiPath);
        return (desc == null) ? null : desc.getAuthzChain();
    }

    protected static SecurityDescriptor
        retrieveSecurityDescriptor(String jndiPath) throws ConfigException {
        return (SecurityDescriptor)getObject(SECURITY_DESCRIPTOR, jndiPath);
    }

    // Create object representation of properties in file and store in JNDI
    protected void initialize() throws ConfigException {
        loadSecurityDescriptor();
        initSecurityDescriptor();
        storeSecurityDescriptor();
        setInitialized(true);
    }

    // check if initialized is true
    protected static boolean isInitialized(String jndiPathName)
        throws ConfigException {
        Boolean value = (Boolean)getObject(SECURITY_INIT_NAME,
                                           jndiPathName);
        return (value == null) ? false : value.booleanValue();
    }

    // Populate the descriptor object with gridmap, creds, authz and
    // init. Does *not* store it in JNDI
    protected void initSecurityDescriptor() throws ConfigException {
        // desc is never null for container, so default creds shld get loaded
        if (this.desc == null) {
            return;
        }
        initCredentials();
        loadGridMap();
        loadAuthorization();
    }

    // set initialied
    protected void setInitialized(boolean init)
        throws ConfigException {
        putObject(SECURITY_INIT_NAME, new Boolean(init),
                  this.jndiPathName);
    }

    // loads security descriptor from file
    protected void loadSecurityDescriptor() throws ConfigException {
        Document doc = loadSecurityDescriptor(this.descriptorFile);
        initSecurityDescriptor(doc);
    }

    // loads security descriptor from file
    public static Document loadSecurityDescriptor(String file)
        throws ConfigException {
        if (file == null) {
            return null;
        }

        logger.debug("Loading security descriptor: " + file);

        InputStream input = null;

        File f = new File(file);
        try {
            if (f.isAbsolute()) {
                logger.debug("Loading security descriptor from file "
                             + "(absolute)");
                input = new FileInputStream(file);
            } else {
                logger.debug("Loading security descriptor from classpath");
                ClassLoader loader = SecurityConfig.class.getClassLoader();
                input = loader.getResourceAsStream(file);
                if (input == null) {
                    String cfgDir =
                        getConfigDir(i18n.getMessage("secDescLoadFail", file));
                    f = new File(cfgDir, file);
                    logger.debug("Loading security descriptor from file "
                                 + "(relative)");
                    input = new FileInputStream(f);
                }
            }

            return XmlUtils.newDocument(input);
        } catch (ConfigException e) {
            throw e;
        } catch (FileNotFoundException e) {
            String msg = i18n.getMessage("noSecDescriptor", file);
            logger.error(msg);
            throw new ConfigException(msg, e);
        } catch (ParserConfigurationException e) {
            String msg = i18n.getMessage("secDescParseFail", file);
            logger.error(msg);
            throw new ConfigException(msg, e);
        } catch (Exception e) {
            String msg = i18n.getMessage("secDescLoadFail", file);
            logger.error(msg);
            throw new ConfigException(msg, e);
        } finally {
            if (input != null) {
                try { input.close(); } catch (Exception e) {}
            }
        }
    }

    // Loads and stored creds in the descriptor, if specified.
    protected void loadCredentials()
        throws GSSException, CredentialException, ConfigException, IOException {

        if (this.desc == null) {
            return;
        }

        String certFile = this.desc.getCertFilename();

        GSSCredential cred = null;
        if (certFile == null) {
            String proxyFile = this.desc.getProxyFilename();

            if (proxyFile != null) {
                logger.debug(i18n.getMessage("loadingProxy", proxyFile));
                File tempFile =
                    resolvePath(proxyFile, i18n.getMessage("proxyFileLoadFail",
                                                           proxyFile));
                X509Credential gCred =
                    new X509Credential(tempFile.getPath());
                this.desc.setLastModified(new Long(tempFile.lastModified()));
                cred = toGSSCredential(gCred);
                this.desc.setProxyFilename(tempFile.getAbsolutePath());
            }
        } else {
            String keyFile = this.desc.getKeyFilename();

            if (keyFile == null) {
                throw new
                    ConfigException(i18n.getMessage("serviceKeyMissing"));
            }

            logger.debug(i18n.getMessage("loadingCertKey",
                                         new Object [] {certFile, keyFile}));

            File resolveKeyFile =
                resolvePath(keyFile, i18n.getMessage("keyFileLoadFail",
                                                     keyFile));
            File resolveCertFile =
                resolvePath(certFile, i18n.getMessage("certFileLoadFail",
                                                      certFile));
            X509Credential gCred =
                new X509Credential(resolveCertFile.getPath(),
                                     resolveKeyFile.getPath());
            this.desc.setLastModified(new Long(resolveCertFile
                                               .lastModified()));
            cred = toGSSCredential(gCred);
            this.desc.setCertificateFiles(resolveCertFile.getAbsolutePath(),
                                          resolveKeyFile.getAbsolutePath());
        }
        Subject subject = null;
        if (cred != null) {
            subject = JaasGssUtil.createSubject(cred);
        }

        this.desc.setSubject(subject);
    }

    // Convert to GSS
    public static GSSCredential toGSSCredential(X509Credential cred)
        throws GSSException {
        return new GlobusGSSCredentialImpl(
                   cred, GSSCredential.INITIATE_AND_ACCEPT
        );
    }

    // load gridmap
    protected void loadGridMap() throws ConfigException {

        if (this.desc == null)
            return;

        String gridMapFile = this.desc.getGridMapFile();
        if (gridMapFile != null) {
            logger.debug(i18n.getMessage("loadingGridmap", gridMapFile));

            GridMap gridMap = new GridMap();
            File file = resolvePath(gridMapFile, i18n
                                    .getMessage("gridMapLoadFail",
                                                gridMapFile));
            try {
                gridMap.load(file);
            } catch (IOException e) {
                throw new ConfigException(i18n.getMessage("gridMapLoadFail",
                                                          file.getName()), e);
            }
            this.desc.setGridMapFile(file.getAbsolutePath());
            this.desc.setGridMap(gridMap);
        } else {
            logger.debug("No gridmap file specified.");
        }
    }

    // Return object stored in jndiPath/objectName. Null if path does
    // not exist
    private static Object getObject(String objectName, String jndiPath)
        throws ConfigException {

        if (jndiPath == null) {
            return null;
        }

        Context ctx = getSecurityContext(jndiPath);

        if (ctx == null) {
            return null;
        }

        Object obj = null;
        try {
            obj = ctx.lookup(objectName);
        } catch (NameNotFoundException exp) {
            logger.debug("Name not found " + exp.getMessage());
            return null;
        } catch (NamingException exp) {
            throw new ConfigException(exp);
        }

        return obj;
    }

    // stores value in JNDI
    protected static void putObject(String objectName, Object object,
                                    String jndiPath)
        throws ConfigException {
        try {
            // create context if need be
            Context serviceCtx = createContext(jndiPath);
            serviceCtx.rebind(objectName, object);
        } catch (NamingException exp) {
            throw new ConfigException(exp);
        }
    }

    protected void storeSecurityDescriptor() throws ConfigException {
        storeSecurityDescriptor(this.desc, this.jndiPathName);
    }

    // returns path to security property
    private static String getContextPath(String jndiPathName) {
        return Constants.JNDI_SERVICES_BASE_NAME + jndiPathName + "/"
            + SECURITY_PROP;
    }


    private static Context createContext(String jndi) throws ConfigException {
        try {
            Context initContext = new InitialContext();
            return JNDIUtils.createSubcontexts(
                               initContext,
                               getContextPath(jndi) + "/foo");
        } catch (NamingException exp) {
            throw new ConfigException(exp);
        }
    }

    protected static boolean isRefreshRequired(String servicePath)
        throws ConfigException {
        Context ctx = getSecurityContext(servicePath);
        if (ctx == null) {
            return false;
        }
        SecurityDescriptor desc = null;
        try {
            desc =
                (SecurityDescriptor)ctx.lookup(SECURITY_DESCRIPTOR);
        } catch (NameNotFoundException exp) {
            return false;
        } catch (NamingException exp) {
            throw new ConfigException(exp);
        }

        return (desc == null) ? false : desc.isRefreshRequired();
    }

    private static Context getSecurityContext(String jndiPath) {
        try {
            Context initContext = new InitialContext();
            Context ctx =
                (Context) initContext.lookup(getContextPath(jndiPath));
            return ctx;
        } catch (NamingException exp) {
            // FIXME ? throw errors maybe ?
            return null;
        }
    }

    private static String getConfigDir(String errMsg) throws ConfigException {

        MessageContext ctx = MessageContext.getCurrentContext();
        if (ctx == null) {
            throw new ConfigException(errMsg);
        }
        String cfgDir = (String)ctx.getProperty(org.apache.axis.Constants
                                                .MC_CONFIGPATH);
        return (cfgDir == null) ? "." : cfgDir;
    }

    private static File resolvePath(String pathName, String err)
        throws ConfigException {

        File file = new File(pathName);
        if (!file.isAbsolute()) {
            // Check if it exists wrt to current dir
            if (!file.exists()) {
                logger.debug("File " + pathName + " does not exist wrt current"
                             + "dir");
                String cfg = getConfigDir(err);
                file = new File(cfg, pathName);
            }
        }
        return file;
    }
    }
