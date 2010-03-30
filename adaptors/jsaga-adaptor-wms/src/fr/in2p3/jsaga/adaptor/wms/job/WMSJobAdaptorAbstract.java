package fr.in2p3.jsaga.adaptor.wms.job;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.impl.GSSCredentialSecurityAdaptor;
import org.ietf.jgss.GSSCredential;

import java.io.File;


/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WMSJobAdaptorAbstract
* Author: Nicolas DEMESY (nicolas.demesy@bt.com)
* Date:   18 fev. 2008
* ***************************************************
/**
 *
 */
public abstract class WMSJobAdaptorAbstract implements ClientAdaptor {
    protected GSSCredential m_credential;
    protected File m_certRepository;
        
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GSSCredentialSecurityAdaptor.class};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        GSSCredentialSecurityAdaptor proxyAdaptor = (GSSCredentialSecurityAdaptor) securityAdaptor;
        m_credential = proxyAdaptor.getGSSCredential();
        m_certRepository = proxyAdaptor.getCertRepository();
    }    
}
