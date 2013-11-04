package fr.in2p3.jsaga.adaptor.ssh3.security;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.*;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.SSHSecurityCredential;

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
* File:   SSHSecurityAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
* ***************************************************/

public class SSHSecurityAdaptor implements SecurityAdaptor {

	public static final String USER_PUBLICKEY = "UserPublicKey";
	public static final String USER_PRIVATEKEY = "UserPrivateKey";
	
	public String getType() {
    	return "SSH";
    }
	
    public Class getSecurityCredentialClass() {
        return SSHSecurityCredential.class;
    }

    public Usage getUsage() {
    	return new UAnd(
   			 new Usage[]{
   					 new UFile(USER_PRIVATEKEY),
   					 new UOptional(USER_PUBLICKEY),
   					 new U(Context.USERID),
   					 new UOptional(Context.USERPASS)});
    }

    public Default[] getDefaults(Map map) throws IncorrectStateException {
    	return new Default[]{
       		new Default(USER_PRIVATEKEY, new File[]{
                        new File(System.getProperty("user.home")+"/.ssh/id_rsa"),
                        new File(System.getProperty("user.home")+"/.ssh/id_dsa")}), 
            new Default(USER_PUBLICKEY, new File[]{
            		new File(System.getProperty("user.home")+"/.ssh/id_rsa.pub"),
            		new File(System.getProperty("user.home")+"/.ssh/id_dsa.pub")}),
    		new Default(Context.USERID,
    				System.getProperty("user.name"))
       };
    }
    
    public SecurityCredential createSecurityCredential(int usage, Map attributes, String contextId) throws IncorrectStateException, NoSuccessException {
        try {
        	// load private key
        	String privateKeyPath = (String) attributes.get(USER_PRIVATEKEY);
			String publicKeyPath = null;
			if (attributes.containsKey(USER_PUBLICKEY)) {
	        	publicKeyPath = (String) attributes.get(USER_PUBLICKEY);
			}
			
			// get UserPass
			String userPass = null;
			if (attributes.containsKey(Context.USERPASS)) {
				userPass = (String) attributes.get(Context.USERPASS);
			}
						
		    return new SSHSecurityCredential(privateKeyPath, publicKeyPath, userPass, (String) attributes.get(Context.USERID));
        } catch(Exception e) {
            throw new NoSuccessException(e);
        }
    }
}
