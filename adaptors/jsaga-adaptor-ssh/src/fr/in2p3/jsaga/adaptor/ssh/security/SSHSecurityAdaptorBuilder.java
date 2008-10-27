package fr.in2p3.jsaga.adaptor.ssh.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptorBuilder;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.*;

import java.io.File;
import java.io.FileInputStream;
import java.lang.Exception;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHSecurityAdaptorBuilder
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
* ***************************************************/

public class SSHSecurityAdaptorBuilder implements SecurityAdaptorBuilder {

	public static final String USER_PUBLICKEY = "UserPublicKey";
	
	public String getType() {
    	return "SSH";
    }
	
    public Class getSecurityAdaptorClass() {
        return SSHSecurityAdaptor.class;
    }

    public Usage getUsage() {
    	return new UAnd(
   			 new Usage[]{
   					 new UFile(Context.USERKEY),
   					 new UOptional(USER_PUBLICKEY),
   					 new U(Context.USERID),
   					 new UOptional(Context.USERPASS)});
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
    	return new Default[]{
       		new Default(Context.USERKEY, new File[]{
                        new File(System.getProperty("user.home")+"/.ssh/id_rsa"),
                        new File(System.getProperty("user.home")+"/.ssh/id_dsa")}), 
            new Default(USER_PUBLICKEY, new File[]{
            		new File(System.getProperty("user.home")+"/.ssh/id_rsa.pub"),
            		new File(System.getProperty("user.home")+"/.ssh/id_dsa.pub")}),
    		new Default(Context.USERID,
    				System.getProperty("user.name"))
       };
    }
    
    public SecurityAdaptor createSecurityAdaptor(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        try {
        	// load private key
        	String privateKeyPath = (String) attributes.get(Context.USERKEY);
			byte[] privateKey = null;
			FileInputStream fisPrivateKey = null;
			try {
				fisPrivateKey = new FileInputStream(privateKeyPath);
				privateKey = new byte[(int) (new File(privateKeyPath).length())];
				int len = 0;
				while (true) {
					int i = fisPrivateKey.read(privateKey, len, privateKey.length - len);
					if (i <= 0)
						break;
					len += i;
				}
				fisPrivateKey.close();
			} catch (Exception e) {
				try {
					if (fisPrivateKey != null)
						fisPrivateKey.close();
				} catch (Exception ee) {
				}
				throw e;
			}

			
			// load public key
			byte[] publicKey = null;
			if (attributes.containsKey(USER_PUBLICKEY)) {
	        	String publicKeyPath = (String) attributes.get(USER_PUBLICKEY);
				FileInputStream fisPublicKey = null;
				try {
					fisPublicKey = new FileInputStream(publicKeyPath);
					publicKey = new byte[(int) (new File(publicKeyPath).length())];
					int len = 0;
					while (true) {
						int i = fisPublicKey.read(publicKey, len, publicKey.length - len);
						if (i <= 0)
							break;
						len += i;
					}
					fisPublicKey.close();
				} catch (Exception e) {
					try {
						if (fisPublicKey != null)
							fisPublicKey.close();
					} catch (Exception ee) {
					}
					throw e;
				}
			}
			
			// get UserPass
			String userPass = null;
			if (attributes.containsKey(Context.USERPASS)) {
				userPass = (String) attributes.get(Context.USERPASS);
			}
						
		    return new SSHSecurityAdaptor(privateKey, publicKey, userPass, (String) attributes.get(Context.USERID));		    
        } catch(Exception e) {
            throw new NoSuccessException(e);
        }
    }
}
