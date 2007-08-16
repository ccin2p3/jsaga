package fr.in2p3.jsaga.adaptor.security;

import org.globus.gsi.GlobusCredential;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   VOMSSecurityAdaptor
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   11 août 2007
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class VOMSSecurityAdaptor implements SecurityAdaptor {
    private GlobusCredential m_proxy;

    public VOMSSecurityAdaptor(GlobusCredential proxy) {
        m_proxy = proxy;
    }

    public GlobusCredential getGlobusCredential() {
        return m_proxy;
    }

    public void close() throws Exception {
    }
}
