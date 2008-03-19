package fr.in2p3.jsaga.adaptor.security.impl;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Vector;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   JKSSecurityAdaptor
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   17 mars 2008
* ***************************************************
* Description:                                      */

public class JKSSecurityAdaptor extends X509SecurityAdaptor {

	private Vector<X509Certificate> caCertificates;

	public Vector<X509Certificate> getCaCertificates() {
		return caCertificates;
	}

	public void setCaCertificates(Vector<X509Certificate> caCertificates) {
		this.caCertificates = caCertificates;
	}

	public JKSSecurityAdaptor(PrivateKey privateKey, X509Certificate publicKey,
			Vector<X509Certificate> caCertificates) {
		super(privateKey, publicKey);
		this.caCertificates = caCertificates;
	}
}
