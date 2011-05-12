package fr.in2p3.jsaga.adaptor.security.impl;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.File;
import java.io.FileInputStream;
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
	private String publicKeyFilename;
	private String privateKeyFilename;
	private String password;
	private String userId;
	
    public SSHSecurityCredential(String privateKeyFile, String publicKeyFile, String password, String userId) throws NoSuccessException {
    	this.privateKeyFilename = privateKeyFile;
    	this.publicKeyFilename = publicKeyFile;
    	//this.privateKey = privateKey;
    	//this.publicKey = publicKey;
    	this.password = password;
    	this.userId = userId;
		FileInputStream fisPrivateKey = null;
		try {
			fisPrivateKey = new FileInputStream(privateKeyFilename);
			privateKey = new byte[(int) (new File(privateKeyFilename).length())];
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
			throw new NoSuccessException(e);
		}

		// load public key
		if (publicKeyFilename != null) {
			FileInputStream fisPublicKey = null;
			try {
				fisPublicKey = new FileInputStream(publicKeyFilename);
				publicKey = new byte[(int) (new File(publicKeyFilename).length())];
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
				throw new NoSuccessException(e);
			}
		}
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

	public File getPrivateKeyFile() {
		return new File(this.privateKeyFilename);
	}
	
	public File getPublicKeyFile() {
		return (this.publicKeyFilename == null)?null:new File(this.publicKeyFilename);
	}
	
	public String getUserId() {
		return userId;
	}
}
