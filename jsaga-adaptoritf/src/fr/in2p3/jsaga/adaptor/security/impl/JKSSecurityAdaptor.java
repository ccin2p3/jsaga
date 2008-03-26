package fr.in2p3.jsaga.adaptor.security.impl;

import java.security.KeyStore;
import java.security.cert.X509Certificate;

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

	private X509Certificate[] caCertificates;

    public JKSSecurityAdaptor(KeyStore keyStore, String keyStorePass, String userAlias, String userPass, X509Certificate[] caCertificates) throws Exception {
        super(keyStore, keyStorePass, userAlias, userPass);
        this.caCertificates = caCertificates;
    }

    public X509Certificate[] getCaCertificates() {
		return caCertificates;
	}
}
