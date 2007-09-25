package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.SagaSecureAdaptor;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   14 juin 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface DataAdaptor extends SagaSecureAdaptor {
    /**
     * @return the aliases of the protocol scheme supported by this adaptor.
     */
    public String[] getSchemeAliases();

    /**
     * @return the default server port.
     */
    public int getDefaultPort();

    /**
     * Connect to the server and initialize the connection with the provided <code>attributes</code>.
     * @param userInfo the user login
     * @param host the server
     * @param port the port
     * @param attributes the provided attributes
     */
    public void connect(String userInfo, String host, int port, Map attributes)
        throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess;


    /**
     * Disconnect from the server.
     */
    public void disconnect()
        throws NoSuccess;
}
