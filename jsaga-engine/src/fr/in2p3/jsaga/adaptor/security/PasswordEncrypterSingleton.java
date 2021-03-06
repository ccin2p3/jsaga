package fr.in2p3.jsaga.adaptor.security;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.security.Key;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PasswordEncrypterSingleton
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PasswordEncrypterSingleton extends PasswordAbstract {
    private static final int EXPIRY_DATE_POSITION = 12;
    private static final int KEY_SIZE = 128;

    /**
     * @param keyalias the alias of the secret key
     * @param lifetime the validity duration of the key in seconds
     */
    public PasswordEncrypterSingleton(String keyalias, int lifetime) throws Exception {
        super();

        // generate key
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        Key key = keyGenerator.generateKey();

        // modify it
        byte[] rawKey = key.getEncoded();
        int expiryDate = getExpiryDate(lifetime);
        setBytes(rawKey, EXPIRY_DATE_POSITION, expiryDate);
        m_key = new SecretKeySpec(rawKey, ALGORITHM);

        // store it
        File keystoreFile = PasswordAbstract.KEYSTORE_FILE;
        m_keystore.setKeyEntry(keyalias, m_key, m_keypass, null);
        m_keystore.store(new FileOutputStream(keystoreFile), m_storepass);
    }

    public String encrypt(String uncrypted) throws Exception {
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.ENCRYPT_MODE, m_key);
        byte[] crypted = cipher.doFinal(uncrypted.getBytes());
        return Base64.encodeBase64String(crypted);
    }

    public static int getExpiryDate(int lifetime) {
        return (int) (System.currentTimeMillis()/1000) + lifetime;
    }

    private static void setBytes(byte[] bytes, int pos, int value) {
        bytes[pos] = (byte)((value & 0xff000000)>>>24);
        bytes[pos+1] = (byte)((value & 0x00ff0000)>>>16);
        bytes[pos+2] = (byte)((value & 0x0000ff00)>>>8);
        bytes[pos+3] = (byte)((value & 0x000000ff));
    }
}
