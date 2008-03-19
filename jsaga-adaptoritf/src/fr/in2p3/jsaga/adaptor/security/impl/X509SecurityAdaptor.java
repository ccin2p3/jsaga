package fr.in2p3.jsaga.adaptor.security.impl;

import java.io.PrintStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ogf.saga.context.Context;
import org.ogf.saga.error.NoSuccess;
import org.ogf.saga.error.NotImplemented;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   X509SecurityAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   17 mars 2008
* ***************************************************
* Description:                                      */

public class X509SecurityAdaptor  implements SecurityAdaptor {

	private PrivateKey privateKey;
	private X509Certificate publicKey;
	private DateFormat df = new SimpleDateFormat("yyyy/MM/dd-HH:mm:ss");
    
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
	public void setPrivateKey(PrivateKey privateKey) {
		this.privateKey = privateKey;
	}
	public X509Certificate getPublicKey() {
		return publicKey;
	}
	public void setPublicKey(X509Certificate publicKey) {
		this.publicKey = publicKey;
	}
	
	public X509SecurityAdaptor(PrivateKey privateKey, X509Certificate publicKey) {
		this.privateKey = privateKey;
		this.publicKey = publicKey;
	}
	
	public void close() throws Exception {		
	}
	
	public void dump(PrintStream out) throws Exception {		
		out.println("  subject  \t\t: "+getUserID());
        out.println("  issuer   \t\t: "+publicKey.getIssuerDN().getName());
        out.println("  not valid before \t: "+df.format(publicKey.getNotBefore()));
        out.println("  not valid after \t: "+df.format(publicKey.getNotAfter()));        
        
	}
	public String getAttribute(String key) throws NotImplemented, NoSuccess {
		if (Context.LIFETIME.equals(key)) {
			long remainingTimeInMs = publicKey.getNotAfter().getTime() - new Date().getTime();
			if(String.valueOf(remainingTimeInMs).length() > 3)
				return String.valueOf(remainingTimeInMs).substring(0, String.valueOf(remainingTimeInMs).length()-3);
			else
				return "0";
        } else if(Context.USERID.equals(key)) {
        	try {
				return getUserID();
			} catch (Exception e) {
				throw new NoSuccess(e);
			}
		} else if(Context.USERKEY.equals(key)) {
        	try {
				return privateKey.toString();
			} catch (Exception e) {
				throw new NoSuccess(e);
			}
		} else if(Context.USERCERT.equals(key)) {
        	try {
				return publicKey.toString();
			} catch (Exception e) {
				throw new NoSuccess(e);
			}
		} else {
            throw new NotImplemented("Attribute not supported: "+key);
        }
	}
	
	public String getUserID() throws Exception {
		return publicKey.getSubjectDN().getName();
	}
}
