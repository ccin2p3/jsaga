package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnectionSecure;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SecureEmulatorDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class SecureEmulatorDataAdaptor extends EmulatorDataAdaptor {
    UserPassSecurityCredential m_securityAdaptor;

    public String getType() {
        return "stest";
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{UserPassSecurityCredential.class};
    }

    public void setSecurityCredential(SecurityCredential credential) {
        m_securityAdaptor = (UserPassSecurityCredential) credential;
    }

    public int getDefaultPort() {
        return 43;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        m_server = new DataEmulatorConnectionSecure(this.getType(), host, port, m_securityAdaptor);
        if(Base.DEBUG) m_server.commit();
    }

    public void disconnect() throws NoSuccessException {
        m_server.commit();
    }
}
