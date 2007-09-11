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
     * Set the attributes.
     * @param attributes the attributes to set.
     */
    public void setAttributes(Map attributes) throws BadParameter;

    /**
     * @return the protocol scheme supported by this adaptor.
     */
    public String getScheme();

    /**
     * @return the aliases of the protocol scheme supported by this adaptor.
     */
    public String[] getSchemeAliases();

    /**
     * @return the default server port.
     */
    public int getDefaultPort();

    /**
     * Connect to the server.
     * @param userInfo the user login
     * @param host the server
     * @param port the port
     */
    public void connect(String userInfo, String host, int port)
        throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess;


    /**
     * Disconnect from the server.
     */
    public void disconnect()
        throws NoSuccess;
}
