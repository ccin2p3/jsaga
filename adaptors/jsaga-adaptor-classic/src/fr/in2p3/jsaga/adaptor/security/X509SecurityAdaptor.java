package fr.in2p3.jsaga.adaptor.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.impl.X509SecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.IncorrectStateException;
import org.ogf.saga.error.NoSuccessException;

import javax.crypto.BadPaddingException;
import java.io.*;
import java.security.KeyStore;
import java.util.Enumeration;
import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   X509SecurityAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   16 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class X509SecurityAdaptor implements SecurityAdaptor {
    public String getType() {
        return "X509";
    }

    public Class getSecurityCredentialClass() {
        return X509SecurityCredential.class;
    }

    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        // get required attributes
        String userCert = (String) attributes.get(Context.USERCERT);
        String userPass = (String) attributes.get(Context.USERPASS);

        try {
            // load the keystore
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(userCert), userPass.toCharArray());

            // get the unique alias
            String alias = null;
            Enumeration knownKeyAliases = keyStore.aliases();
            while (alias==null && knownKeyAliases.hasMoreElements()) {
                String next = (String) knownKeyAliases.nextElement();
                if (keyStore.isKeyEntry(next)) {
                    alias = next;
                }
            }
            
            return new X509SecurityCredential(keyStore, userPass, alias, userPass);
        } catch(IOException e) {
            if (e.getMessage()!=null && e.getMessage().endsWith("too big.")) {
                throw new IncorrectStateException("Not a PKCS12 file", e);
            } else if (e.getCause()!=null && e.getCause() instanceof BadPaddingException) {
                throw new IncorrectStateException("Bad passphrase", e);
            } else {
                throw new NoSuccessException(e);
            }
        } catch(Exception e) {
            throw new NoSuccessException(e);
        }
    }

    public Usage getUsage() {
        return new UAnd(new Usage[]{
                new UFile(Context.USERCERT),
                new UHidden(Context.USERPASS)
        });
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return new Default[]{
        		new Default(Context.USERCERT, new File[]{
                         new File(System.getProperty("user.home")+"/usercert.p12")})
        };
    }
}
