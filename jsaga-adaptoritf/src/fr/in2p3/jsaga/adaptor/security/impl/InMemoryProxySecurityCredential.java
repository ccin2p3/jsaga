package fr.in2p3.jsaga.adaptor.security.impl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.binary.BinaryStreamDriver;
import org.apache.commons.codec.binary.Base64;
import org.ietf.jgss.GSSCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   InMemoryProxySecurityCredential
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   12 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class InMemoryProxySecurityCredential extends GSSCredentialSecurityCredential {

    public InMemoryProxySecurityCredential(String base64, File certRepository) throws NoSuccessException {
        super(toGSSCredential(base64), certRepository);
    }

    /** override super.getAttribute() */
    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        if (Context.USERPROXY.equals(key)) {
            return toBase64(m_proxy);
        } else {
            return super.getAttribute(key);
        }
    }

    public static String toBase64(GSSCredential proxy) throws NoSuccessException {
        //try {
            XStream xstream = new XStream(new BinaryStreamDriver());
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            xstream.toXML(proxy, bytes);
            byte[] buffer = bytes.toByteArray();
            //System.out.println("Serialized " + res);
            return Base64.encodeBase64String(buffer);
            //ObjectOutputStream out = new ObjectOutputStream(bytes);
            //out.writeObject(proxy);
//            Output output = new Output(bytes);
//            k.writeObject(output, proxy);
//            output.close();
//            byte[] buffer = bytes.toByteArray();
////            new org.apache.commons.codec.binary.Base64();
//            return Base64.encodeBase64String(buffer);
            //return new sun.misc.BASE64Encoder().encodeBuffer(buffer);
       /* } catch (IOException e) {
            throw new NoSuccessException(e);
        }*/
    }

    public static GSSCredential toGSSCredential(String base64) throws NoSuccessException {
        XStream xstream = new XStream(new BinaryStreamDriver());
        byte[] buffer = Base64.decodeBase64(base64);
        ByteArrayInputStream is = new ByteArrayInputStream(buffer);
        return (GSSCredential) xstream.fromXML(is);
//        Kryo k = new Kryo();
//        byte[] buffer = Base64.decodeBase64(base64);
//        Input is = new Input(new ByteArrayInputStream(buffer));
//        GSSCredential cred = k.readObject(is, GSSCredential.class);
//        is.close();
//        return cred;
//        try {
////        	new org.apache.commons.codec.binary.Base64();
//			//            byte[] buffer = new sun.misc.BASE64Decoder().decodeBuffer(base64);
//        	byte[] buffer = Base64.decodeBase64(base64);
//            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(buffer));
//            GSSCredential cred = (GSSCredential) in.readObject();
//            in.close();
//            return cred;
//        } catch (Exception e) {
//            throw new NoSuccessException(e);
//        }
    }
}
