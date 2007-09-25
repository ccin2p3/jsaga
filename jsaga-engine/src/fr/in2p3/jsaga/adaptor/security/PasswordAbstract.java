package fr.in2p3.jsaga.adaptor.security;

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
    protected static final File FILE = new File(new File(System.getProperty("user.home")), ".jce-keystore");
    protected static final String ALGORITHM = "AES";
    private static final String MODE = "ECB";
    private static final String PADDING = "PKCS5Padding";
    protected static final String CIPHER = ALGORITHM+"/"+MODE+"/"+PADDING;

    protected char[] m_storepass;
    protected char[] m_keypass;
    protected KeyStore m_keystore;
    protected String m_keyalias;
    protected Key m_key;

    protected PasswordAbstract() throws Exception {
        m_storepass = System.getProperty("keystore.password", "changeit").toCharArray();
        m_keypass = System.getProperty("key.password", "changeit").toCharArray();
        m_keystore = KeyStore.getInstance("JCEKS");
        try {
            m_keystore.load(new FileInputStream(FILE), m_storepass);
        } catch (FileNotFoundException e) {
            m_keystore.load(null, null); // create an empty JKS keystore
        }
        m_keyalias = System.getProperty("user.name", "default");
    }
}
