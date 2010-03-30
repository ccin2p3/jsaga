package fr.in2p3.jsaga.adaptor.ssh.security;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.PrintStream;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SSHSecurityCredential
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   15 avril 2008
* ***************************************************/

public class SSHSecurityCredential implements SecurityCredential {
	
	private byte[] privateKey;
	private byte[] publicKey;
	private String password;
	private String userId;
	
    public SSHSecurityCredential(byte[] privateKey, byte[] publicKey, String password, String userId) throws NoSuccessException {
    	this.privateKey = privateKey;
    	this.publicKey = publicKey;
    	this.password = password;
    	this.userId = userId;
    }

    public void dump(PrintStream out) throws Exception {
    	   
    	// get Certificate Type
    	String type = "Unknown";
    	int len= privateKey.length;
        int i=0;
		while (i < len) {
			if (privateKey[i] == 'B' && privateKey[i + 1] == 'E'
					&& privateKey[i + 2] == 'G' && privateKey[i + 3] == 'I') {
				i += 6;
				if (privateKey[i] == 'D' && privateKey[i + 1] == 'S'
						&& privateKey[i + 2] == 'A') {
					type = "DSA";
				} else if (privateKey[i] == 'R' && privateKey[i + 1] == 'S'
						&& privateKey[i + 2] == 'A') {
					type = "RSA";
				} else {

				}
				break;
			}
			i++;
		}
      	System.out.println("User: "+getUserID());
      	System.out.println("Key type: "+type);
    }
    
    public String getUserID() throws NoSuccessException {
        return userId;
    }
    
    public String getUserPass() {
        return password;
    }

    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        if (Context.LIFETIME.equals(key)) {
            return ""+INFINITE_LIFETIME;
        } else {
            throw new NotImplementedException("Attribute not supported: "+key);
        }
    }

    public void close() throws Exception {
    }

	public byte[] getPrivateKey() {
		return privateKey;
	}

	public byte[] getPublicKey() {
		return publicKey;
	}

	public String getUserId() {
		return userId;
	}
}
