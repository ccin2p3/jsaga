package fr.in2p3.jsaga.adaptor;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   ClientAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   21 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public interface ClientAdaptor extends Adaptor {
    static final int NO_PORT = -1;

    /**
     * @return the array of supported SecurityCredential classes.
     */
    public Class[] getSupportedSecurityCredentialClasses();

    /**
     * Set the security credential.
     * @param credential the security credential.
     */
    public void setSecurityCredential(SecurityCredential credential);

    /**
     * @return the default server port.
     */
    public int getDefaultPort();

    /**
     * Connect to the server and initialize the connection with the provided attributes.
     * @param userInfo the user login
     * @param host the server
     * @param port the port
     * @param basePath the base path
     * @param attributes the provided attributes
     */
    public void connect(String userInfo, String host, int port, String basePath, Map attributes)
        throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException;

    /**
     * Disconnect from the server.
     */
    public void disconnect()
        throws NoSuccessException;
}
