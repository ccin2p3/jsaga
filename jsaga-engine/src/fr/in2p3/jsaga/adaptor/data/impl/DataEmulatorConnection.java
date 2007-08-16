package fr.in2p3.jsaga.adaptor.data.impl;

import fr.in2p3.jsaga.adaptor.schema.data.emulator.Server;
import fr.in2p3.jsaga.adaptor.schema.data.emulator.ServerType;
import org.ogf.saga.error.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataEmulatorConnection
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DataEmulatorConnection extends DataEmulatorConnectionAbstract {
    private Server m_serverRoot;

    public DataEmulatorConnection(String protocol, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        super();
        m_serverRoot = m_grid.connect(protocol, host, port);
        if (m_serverRoot == null) {
            throw new Timeout("Failed to connect to host: "+host);
        }
    }

    public void destroy() {
        m_grid.disconnect(m_serverRoot);
    }

    protected ServerType getServerRoot() {
        return m_serverRoot;
    }
}
