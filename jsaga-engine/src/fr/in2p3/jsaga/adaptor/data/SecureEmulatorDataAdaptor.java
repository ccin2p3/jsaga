package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.data.impl.DataEmulatorConnectionSecure;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.UserPassSecurityAdaptor;
import org.ogf.saga.error.*;

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
    UserPassSecurityAdaptor m_securityAdaptor;

    public String[] getSupportedContextTypes() {
        return new String[]{"UserPass"};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_securityAdaptor = (UserPassSecurityAdaptor) securityAdaptor;
    }

    public String getScheme() {
        return "stest";
    }

    public String[] getSchemeAliases() {
        return new String[]{"semulated"};
    }

    public int getDefaultPort() {
        return 43;
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        m_server = new DataEmulatorConnectionSecure(this.getScheme(), host, port, m_securityAdaptor);
        if(Base.DEBUG) m_server.commit();
    }

    public void disconnect() throws NoSuccess {
        m_server.commit();
    }
}
