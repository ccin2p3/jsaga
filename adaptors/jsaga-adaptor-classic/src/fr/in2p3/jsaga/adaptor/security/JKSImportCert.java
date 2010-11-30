package fr.in2p3.jsaga.adaptor.security;

import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Enumeration;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JKSImportCert
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   1 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class JKSImportCert {
    private static final File DEFAULT_KEYSTORE_FILE = new File(System.getProperty("user.home"), ".keystore");
    private static final char[] DEFAULT_KEYSTORE_PASSWORD = "changeit".toCharArray();

    public static void main(String[] args) throws Exception {
        // get required arguments
        if (args.length != 2) {
            System.err.println("usage: JKSImportCert <file.p12> <PKCS12-passphrase>");
            System.exit(1);
        }
        File pkcs12File = new File(args[0]);
        char[] pkcs12Passphrase = args[1].toCharArray();

        // get optional arguments
        File keystoreFile = System.getProperty("javax.net.ssl.keyStore")!=null
                ? new File(System.getProperty("javax.net.ssl.keyStore"))
                : DEFAULT_KEYSTORE_FILE;
        char[] keystorePassword = System.getProperty("javax.net.ssl.keyStorePassword")!=null
                ? System.getProperty("javax.net.ssl.keyStorePassword").toCharArray()
                : DEFAULT_KEYSTORE_PASSWORD;
        if (keystorePassword.length < 6) {
        	throw new KeyStoreException("KeyStore password must be at least 6 characters");
        }
        // Extract alias, certChain and key from PKCS12
        KeyStore pkcs12Keystore = KeyStore.getInstance("PKCS12");
        InputStream inP12 = new FileInputStream(pkcs12File);
        pkcs12Keystore.load(inP12, pkcs12Passphrase);
        inP12.close();
        String alias = getAlias(pkcs12Keystore);
        Certificate certChain[] = pkcs12Keystore.getCertificateChain(alias);
        Key privKey = pkcs12Keystore.getKey(alias, pkcs12Passphrase);

        // Load JKS
        KeyStore ks = KeyStore.getInstance("JKS", "SUN");
        if (keystoreFile.exists()) {
            InputStream in = new FileInputStream(keystoreFile);
            ks.load(in, keystorePassword);
            in.close();
        } else {
            System.err.println("File not found: "+keystoreFile);
            System.err.println("Creating an empty keystore...");
            ks.load(null, null);
        }

        // Add certificate to JKS
        ks.setKeyEntry("CERT", privKey, keystorePassword, certChain);

        // Save JKS
        OutputStream out = new FileOutputStream(keystoreFile);
        ks.store(out, keystorePassword);
        out.close();
    }


    private static String getAlias(KeyStore keystore) throws KeyStoreException {
        String alias;
        Enumeration e = keystore.aliases();
        if (e.hasMoreElements()) {
            alias = (String) e.nextElement();
            if (e.hasMoreElements()) {
                throw new KeyStoreException("Certificate contains more than one alias");
            }
        } else {
            throw new KeyStoreException("Certificate contains no alias");
        }
        return alias;
    }
}
