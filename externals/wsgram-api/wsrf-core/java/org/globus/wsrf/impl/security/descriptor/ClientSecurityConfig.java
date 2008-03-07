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
import java.security.cert.X509Certificate;

import javax.security.auth.Subject;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;
import org.w3c.dom.Document;

import org.globus.gsi.CertUtil;
import org.globus.gsi.GlobusCredential;
import org.globus.gsi.GlobusCredentialException;
import org.globus.util.I18n;
import org.globus.wsrf.config.ConfigException;
import org.globus.wsrf.impl.security.authentication.encryption.EncryptionCredentials;
import org.globus.wsrf.impl.security.descriptor.util.ElementParserException;

/**
 * Helper API for dealing with <code>ClientSecurityDescriptor</code>
 */
public class ClientSecurityConfig {

    private static Log logger =
        LogFactory.getLog(ClientSecurityConfig.class.getName());

    private static I18n i18n =
        I18n.getI18n(SecurityDescriptor.RESOURCE,
                     SecurityConfig.class.getClassLoader());

    /**
     * Initialize class.
     *
     * @param file
     *        Client security descriptor filename
     */
    public static ClientSecurityDescriptor initialize(String file)
        throws ConfigException {

        if (file == null)
            return null;

        ClientSecurityDescriptor desc = null;
        Document doc = SecurityConfig.loadSecurityDescriptor(file);
        if (doc != null) {
            try {
                desc = new ClientSecurityDescriptor();
                desc.parse(doc.getDocumentElement());
            } catch (ElementParserException e) {
                throw new ConfigException(e);
            }
        }

        // FIXME: code in SecurityConfig can be reused with some work.
        try {
            loadCredentials(desc);
            loadPeerCredential(desc);
        } catch (Exception exp) {
            throw new ConfigException(exp);
        }
        return desc;
    }

    private static void loadPeerCredential(ClientSecurityDescriptor desc)
        throws Exception {

        if (desc.getPeerCredentials() != null) {
            Subject subject = new Subject();
            X509Certificate serverCert =
                CertUtil.loadCertificate(desc.getPeerCredentials());
            EncryptionCredentials encryptionCreds =
                new EncryptionCredentials(new X509Certificate[]
                { serverCert });
            subject.getPublicCredentials().add(encryptionCreds);
            desc.setPeerSubject(subject);
        }
    }

    private static void loadCredentials(ClientSecurityDescriptor desc)
        throws GSSException, GlobusCredentialException, ConfigException {

        if (desc == null)
            return;

        String certFile = desc.getCertFilename();

        GSSCredential cred = null;
        if (certFile == null) {
            String proxyFile = desc.getProxyFilename();

            if (proxyFile != null) {
                logger.debug("Loading credential:proxy = '" + proxyFile + "'");
                GlobusCredential gCred = new GlobusCredential(proxyFile);
                desc.setLastModified(
                     new Long((new File(proxyFile)).lastModified()));
                cred = SecurityConfig.toGSSCredential(gCred);
            }
        } else {
            String keyFile = desc.getKeyFilename();

            if (keyFile == null) {
                throw new
                    ConfigException(i18n.getMessage("serviceKeyMissing"));
            }

            logger.debug("Loading credential: cert = '" + certFile
                         + "' key = '" +  keyFile + "'");

            GlobusCredential gCred = new GlobusCredential(certFile, keyFile);
            desc.setLastModified(
                 new Long((new File(certFile)).lastModified()));
            cred = SecurityConfig.toGSSCredential(gCred);
        }
        desc.setGSSCredential(cred);
    }

}
