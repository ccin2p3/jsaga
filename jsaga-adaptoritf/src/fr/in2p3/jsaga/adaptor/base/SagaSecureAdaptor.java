package fr.in2p3.jsaga.adaptor.base;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SagaSecureAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface SagaSecureAdaptor extends SagaBaseAdaptor {
    /**
     * @return list of supported SecurityAdaptor classes.
     */
    public Class[] getSupportedSecurityAdaptorClasses();

    /**
     * Set the security adaptor.
     * @param securityAdaptor the security adaptor.
     */
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor);

    /**
     * Connect to the server and initialize the connection with the provided <code>attributes</code>.
     * @param userInfo the user login
     * @param host the server
     * @param port the port
     * @param basePath the base path
     * @param attributes the provided attributes
     */
    public void connect(String userInfo, String host, int port, String basePath, Map attributes)
        throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess;

    /**
     * Disconnect from the server.
     */
    public void disconnect()
        throws NoSuccess;
}
