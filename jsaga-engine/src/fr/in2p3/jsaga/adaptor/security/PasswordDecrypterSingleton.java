package fr.in2p3.jsaga.adaptor.security;

import org.ogf.saga.error.BadParameter;
import sun.misc.BASE64Decoder;

import javax.crypto.Cipher;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   PasswordDecrypterSingleton
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class PasswordDecrypterSingleton extends PasswordAbstract {
    private static final int EXPIRY_DATE_POSITION = 12;
    private static PasswordDecrypterSingleton _instance;

    public synchronized static PasswordDecrypterSingleton getInstance() throws Exception {
        if (_instance == null) {
            _instance = new PasswordDecrypterSingleton();
        }
        return _instance;
    }
    private PasswordDecrypterSingleton() throws Exception {
        super();

        // load key from keystore
        m_key = m_keystore.getKey(m_keyalias, m_keypass);
        if (m_key == null) {
            throw new BadParameter("Key not found in keystore: "+m_keyalias);
        }
    }

    public int getExpiryDate() {
        return getValue(m_key.getEncoded(), EXPIRY_DATE_POSITION);
    }

    public String decrypt(String cryptedBase64) throws Exception {
        byte[] crypted = new BASE64Decoder().decodeBuffer(cryptedBase64);
        Cipher cipher = Cipher.getInstance(CIPHER);
        cipher.init(Cipher.DECRYPT_MODE, m_key);
        byte[] decrypted = cipher.doFinal(crypted);
        return new String(decrypted);
    }

    private static int getValue(byte[] bytes, int pos) {
        return ((bytes[pos] & 0xFF) << 24)
                | ((bytes[pos+1] & 0xFF) << 16)
                | ((bytes[pos+2] & 0xFF) << 8)
                | (bytes[pos+3] & 0xFF);
    }
}
