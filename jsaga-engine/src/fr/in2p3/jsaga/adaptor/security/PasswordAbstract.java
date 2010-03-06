package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.Base;

import java.io.*;
import java.security.Key;
import java.security.KeyStore;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PasswordAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class PasswordAbstract {
    static final File KEYSTORE_FILE = new File(Base.JSAGA_USER, "jce-keystore.dat");

    protected static final String ALGORITHM = "AES";
    private static final String MODE = "ECB";
    private static final String PADDING = "PKCS5Padding";
    protected static final String CIPHER = ALGORITHM+"/"+MODE+"/"+PADDING;

    protected char[] m_storepass;
    protected char[] m_keypass;
    protected KeyStore m_keystore;
    protected Key m_key;

    protected PasswordAbstract() throws Exception {
        m_storepass = System.getProperty("keystore.password", "changeit").toCharArray();
        m_keypass = System.getProperty("key.password", "changeit").toCharArray();
        m_keystore = KeyStore.getInstance("JCEKS");
        if (KEYSTORE_FILE.exists()) {
            // load keystore
            InputStream in = new FileInputStream(KEYSTORE_FILE);
            m_keystore.load(in, m_storepass);
            in.close();
        } else {
            // create an empty keystore
            m_keystore.load(null, null);
        }
    }
}
