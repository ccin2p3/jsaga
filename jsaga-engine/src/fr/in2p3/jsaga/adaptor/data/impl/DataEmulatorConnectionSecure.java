package fr.in2p3.jsaga.adaptor.data.impl;

import fr.in2p3.jsaga.adaptor.schema.data.emulator.SecureServer;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.ServerType;
import fr.in2p3.jsaga.adaptor.security.impl.UserPassSecurityCredential;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataEmulatorConnectionSecure
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataEmulatorConnectionSecure extends DataEmulatorConnectionAbstract {
    private SecureServer m_serverRoot;

    public DataEmulatorConnectionSecure(String protocol, String host, int port, UserPassSecurityCredential security) throws AuthenticationFailedException, AuthorizationFailedException, TimeoutException, NoSuccessException {
        super();
        m_serverRoot = m_grid.connect(protocol, host, port, security);
        if (m_serverRoot == null) {
            throw new TimeoutException("Failed to connect to host: "+host);
        }
    }

    public void destroy() {
        m_grid.disconnect(m_serverRoot);
    }

    protected ServerType getServerRoot() {
        return m_serverRoot;
    }
}
