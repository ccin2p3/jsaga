package fr.in2p3.jsaga.adaptor.security.impl;

import org.ietf.jgss.GSSCredential;
import org.ogf.saga.error.NoSuccess;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   InMemoryProxySecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class InMemoryProxySecurityAdaptor extends GSSCredentialSecurityAdaptor {
    public InMemoryProxySecurityAdaptor(String base64) throws NoSuccess {
        super(toGSSCredential(base64));
    }

    public static String toBase64(GSSCredential proxy) throws NoSuccess {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bytes);
            out.writeObject(proxy);
            out.close();
            byte[] buffer = bytes.toByteArray();
            return new sun.misc.BASE64Encoder().encodeBuffer(buffer);
        } catch (IOException e) {
            throw new NoSuccess(e);
        }
    }

    public static GSSCredential toGSSCredential(String base64) throws NoSuccess {
        try {
            byte[] buffer = new sun.misc.BASE64Decoder().decodeBuffer(base64);
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
            GSSCredential cred = (GSSCredential) in.readObject();
            in.close();
            return cred;
        } catch (Exception e) {
            throw new NoSuccess(e);
        }
    }
}
