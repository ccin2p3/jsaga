package fr.in2p3.jsaga.adaptor.security;

import org.ogf.saga.error.NoSuccess;

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
        File keystoreFile = getFile();
        if (keystoreFile.exists()) {
            // load keystore
            InputStream in = new FileInputStream(keystoreFile);
            m_keystore.load(in, m_storepass);
            in.close();
        } else {
            // create an empty keystore
            m_keystore.load(null, null);
        }
    }

    private static File s_file;
    protected static File getFile() throws NoSuccess {
        if (s_file == null) {
            // set keystore file
            s_file = new File(new File(new File(System.getProperty("user.home")), ".jsaga"), "jce-keystore.dat");

            // create parent directory if needed
            File dir = s_file.getParentFile();
            if (!dir.exists() && !dir.mkdir()) {
                throw new NoSuccess("Failed to create directory: "+dir);
            }
        }
        return s_file;
    }
}
