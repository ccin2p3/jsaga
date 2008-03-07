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
package org.globus.wsrf.impl.security.authentication;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;

import org.apache.ws.patched.security.WSSecurityException;
import org.apache.ws.patched.security.components.crypto.CredentialException;
import org.apache.ws.patched.security.components.crypto.Crypto;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DEREncodableVector;
import org.bouncycastle.asn1.DERObject;
import org.bouncycastle.asn1.DERSequence;

import org.globus.gsi.CertUtil;
import org.globus.gsi.bc.BouncyCastleUtil;
import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;

import org.globus.util.I18n;

public class ContextCrypto implements Crypto {

    private static I18n i18n =
        I18n.getI18n("org.globus.wsrf.impl.security.error",
                     ContextCrypto.class.getClassLoader());

    private static Log log = LogFactory.getLog(ContextCrypto.class);
    private static final String ALIAS = "ContextCrypto";
    private static final String[] ALIASES = new String[] {ALIAS};

    private Properties properties;
    private CertificateFactory certFact = null;
    private static ContextCrypto crypto = new ContextCrypto();

    public static ContextCrypto getInstance() {
        return crypto;
    }

    public ContextCrypto() {
        this(null);
    }
    public ContextCrypto(Properties properties) {
        if (properties == null) {
            this.properties = System.getProperties();
        } else {
            this.properties = properties;
        }
    }

    /**
     * Gets the private key from the current thread context</code>.
     * <p/>
     *
     * @param alias    ignored
     * @param password ignored
     * @throws Exception
     * @return          The private key
     */
    public PrivateKey getPrivateKey(String alias, String password)
        throws Exception {
        GlobusGSSCredentialImpl cred =
            (GlobusGSSCredentialImpl) ContextCredential.getCurrent();
        if (cred == null) {
            return null;
        }
        return cred.getPrivateKey();
    }

    public X509Certificate loadCertificate(InputStream in)
        throws WSSecurityException {
        X509Certificate cert = null;
        try {
            cert = (X509Certificate)
                getCertificateFactory().generateCertificate(in);
        } catch (Exception e) {
            throw new WSSecurityException(
                  WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                  "parseError");
        }
        return cert;
    }


    public X509Certificate[] getX509Certificates(byte[] data, boolean reverse)
        throws WSSecurityException {

        if (data == null) {
            return null;
        }

        DERObject obj = null;
        try {
            obj = BouncyCastleUtil.toDERObject(data);
        } catch (IOException e) {
            log.error("",e);
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                "parseError");
        }
        ASN1Sequence seq = ASN1Sequence.getInstance(obj);
        int size = seq.size();
        ByteArrayInputStream in;
        X509Certificate[] certs = new X509Certificate[size];

        for (int i = 0; i < size; i++) {
            obj = seq.getObjectAt(i).getDERObject();
            try {
                data = BouncyCastleUtil.toByteArray(obj);
            } catch (IOException e) {
                log.error("",e);
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                    "parseError");
            }
            in = new ByteArrayInputStream(data);
            try
            {
                certs[(!reverse) ? (size - 1 - i) : i] =
                    CertUtil.loadCertificate(in);
            }
            catch(GeneralSecurityException e)
            {
                log.error("",e);
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                    "parseError");
            }
        }

        return certs;
    }

    public String getAliasForX509Cert(String issuer)
        throws WSSecurityException {
        return ALIAS;
    }

    public String getAliasForX509Cert(String issuer, BigInteger serialNumber)
        throws WSSecurityException {
        return ALIAS;
    }

    public String getAliasForX509Cert(byte[] skiBytes)
        throws WSSecurityException {
        return ALIAS;
    }

    public String getAliasForX509Cert(Certificate cert)
        throws WSSecurityException {
        return ALIAS;
    }

    public String getDefaultX509Alias() {
        return ALIAS;
    }

    /**
     * Gets the list of certificates for a given alias.
     * <p/>
     *
     * @param alias Lookup certificate chain for this alias
     * @return Array of X509 certificates for this alias name, or
     *         null if this alias does not exist in the keystore
     */
    public X509Certificate[] getCertificates(String alias)
        throws WSSecurityException {
        GlobusGSSCredentialImpl cred =
            (GlobusGSSCredentialImpl) ContextCredential.getCurrent();
        if (cred == null) {
               return null;
        }
        return cred.getCertificateChain();
    }

    public void setKeyStore(KeyStore ks) {
    }

    public void load(InputStream input) throws CredentialException {
    }

    public KeyStore getKeyStore() {
        return null;
    }

    public String[] getAliasesForDN(String subjectDN)
        throws WSSecurityException {
        return ALIASES;
    }

    public byte[] getCertificateData(boolean reverse, X509Certificate[] certs)
        throws WSSecurityException {
        byte[] data = null;

        if (certs == null) {
            throw new IllegalArgumentException(i18n.getMessage("certsNull"));
        }

        DEREncodableVector vec = new DEREncodableVector();

        try {
            // We're returning ours in the reverse order compared to the
            // standard so we need to reverse here
            if (!reverse) {
                for (int i = certs.length - 1; i >= 0; i--) {
                    vec.add(BouncyCastleUtil.toDERObject(
                        certs[i].getEncoded()));
                }
            } else {
                for (int i = 0; i < certs.length; i++) {
                    vec.add(BouncyCastleUtil.toDERObject(
                        certs[i].getEncoded()));
                }
            }
        } catch (CertificateEncodingException e1) {
            log.error("", e1);
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "encodeError");
        } catch (IOException e2) {
            log.error("", e2);
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError");
        }

        DERSequence seq = new DERSequence(vec);

        try {
            data = BouncyCastleUtil.toByteArray(seq);
        } catch (IOException e) {
            log.error("", e);
            throw new WSSecurityException(
                WSSecurityException.SECURITY_TOKEN_UNAVAILABLE, "parseError");
        }
        return data;
    }

    static String SKI_OID = "2.5.29.14";

    /**
     * Reads the SubjectKeyIdentifier information from the certificate.
     * <p/>
     *
     * @param cert       The certificate to read SKI
     * @return The byte array conating the binary SKI data
     */
    public byte[] getSKIBytesFromCert(X509Certificate cert)
        throws WSSecurityException {

        byte data[] = null;
        byte abyte0[] = null;
        if (cert.getVersion() < 3) {
            throw new WSSecurityException(
                1, "noSKIHandling",
                new Object[] { "Wrong certificate version (<3)" });
        }

        /*
         * Gets the DER-encoded OCTET string for the extension value (extnValue)
         * identified by the passed-in oid String. The oid string is
         * represented by a set of positive whole numbers separated by periods.
         */
        data = cert.getExtensionValue(SKI_OID);

        if (data == null) {
            throw new WSSecurityException(
                   WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
                   "noSKIHandling",
                   new Object[] { "No extension data" });
        }

        byte[] extensionValue = null;
        try {
            extensionValue = BouncyCastleUtil.getExtensionValue(data);
        } catch (IOException e1) {
            throw new WSSecurityException(
                WSSecurityException.UNSUPPORTED_SECURITY_TOKEN,
                "noSKIHandling",
                new Object[] { "cannot read SKI value as octet data" });
        }

        /**
         * Strip away first two bytes from the DerValue (tag and length)
         */
        abyte0 = new byte[extensionValue.length - 2];

        System.arraycopy(extensionValue, 2, abyte0, 0, abyte0.length);

        /*
          byte abyte0[] = new byte[derEncodedValue.length - 4];
          System.arraycopy(derEncodedValue, 4, abyte0, 0, abyte0.length);
        */
        return abyte0;
    }

    public synchronized CertificateFactory getCertificateFactory()
        throws WSSecurityException {
        if (certFact == null) {
            try {
                String provider = properties.getProperty(
                    "org.apache.ws.security.crypto.merlin.cert.provider");
                if(provider == null || provider.length() == 0) {
                    certFact = CertificateFactory.getInstance("X.509","BC");
                }
                else {
                    certFact = CertificateFactory.getInstance("X.509", provider);
                }
            } catch (CertificateException e) {
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                                              "unsupportedCertType");
            } catch (NoSuchProviderException e) {
                throw new WSSecurityException(
                    WSSecurityException.SECURITY_TOKEN_UNAVAILABLE,
                    "noSecProvider");
            }
        }
        return certFact;
    }

    public boolean validateCertPath(X509Certificate[] certs)
        throws WSSecurityException {
        throw new WSSecurityException(WSSecurityException.FAILURE);
    }

}

