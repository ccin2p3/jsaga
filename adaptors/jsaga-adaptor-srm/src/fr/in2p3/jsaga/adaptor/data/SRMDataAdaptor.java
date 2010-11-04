package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
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

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return m_adaptor.getDefaults(attributes);
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return m_adaptor.getSupportedSecurityCredentialClasses();
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_adaptor.setSecurityCredential(credential);
    }

    public int getDefaultPort() {
        return m_adaptor.getDefaultPort();
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // connect
        m_adaptor.connect(userInfo, host, port, null, attributes);

        // check version
        try {
            m_adaptor.ping();
            // v 2.2 => done
        } catch(BadParameterException e) {
            // v 1.1 => reconnect
            m_adaptor = new SRM11DataAdaptor();
            m_adaptor.connect(userInfo, host, port, null, attributes);
        }
    }

    public void disconnect() throws NoSuccessException {
        m_adaptor.disconnect();
    }
}
