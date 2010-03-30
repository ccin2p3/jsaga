package fr.in2p3.jsaga.adaptor;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import fr.in2p3.jsaga.adaptor.Adaptor;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ClientAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ClientAdaptor extends Adaptor {
    /**
     * @return list of supported SecurityCredential classes.
     */
    public Class[] getSupportedSecurityCredentialClasses();

    /**
     * Set the security credential.
     * @param credential the security credential.
     */
    public void setSecurityCredential(SecurityCredential credential);

    /**
     * Connect to the server and initialize the connection with the provided attributes.
     * @param userInfo the user login
     * @param host the server
     * @param port the port
     * @param basePath the base path
     * @param attributes the provided attributes
     */
    public void connect(String userInfo, String host, int port, String basePath, Map attributes)
        throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Disconnect from the server.
     */
    public void disconnect()
        throws NoSuccessException;
}
