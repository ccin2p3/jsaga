package fr.in2p3.jsaga.adaptor.security;

import org.ietf.jgss.GSSCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxySecurityAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   13 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxySecurityAdaptor implements SecurityAdaptor {
    private GSSCredential m_proxy;

    public MyProxySecurityAdaptor(GSSCredential proxy) {
        m_proxy = proxy;
    }

    public GSSCredential getGSSCredential() {
        return m_proxy;
    }

    public void close() throws Exception {
        //m_proxy.dispose();
    }
}
