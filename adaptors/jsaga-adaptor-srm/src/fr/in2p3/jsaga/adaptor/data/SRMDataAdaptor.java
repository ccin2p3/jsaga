package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRMDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 sept. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SRMDataAdaptor implements DataAdaptor {
    private SRMDataAdaptorAbstract m_adaptor;

    /** constructor */
    public SRMDataAdaptor() {
        m_adaptor = new SRM22DataAdaptor();
    }

    public String getType() {
        return "srm";
    }

    public Usage getUsage() {
        return m_adaptor.getUsage();
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return m_adaptor.getDefaults(attributes);
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return m_adaptor.getSupportedSecurityAdaptorClasses();
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_adaptor.setSecurityAdaptor(securityAdaptor);
    }

    public BaseURL getBaseURL() throws IncorrectURL {
        return m_adaptor.getBaseURL();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // connect
        m_adaptor.connect(userInfo, host, port, null, attributes);

        // check version
        try {
            m_adaptor.ping();
            // v 2.2 => done
        } catch(BadParameter e) {
            // v 1.1 => reconnect
            m_adaptor = new SRM11DataAdaptor();
            m_adaptor.connect(userInfo, host, port, null, attributes);
        }
    }

    public void disconnect() throws NoSuccess {
        m_adaptor.disconnect();
    }
}
