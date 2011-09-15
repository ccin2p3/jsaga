package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.security.GlobusSecurityCredential;
import fr.in2p3.jsaga.adaptor.security.MyProxySecurityCredential;
import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.exception.ServerException;
import org.ogf.saga.error.*;

import java.io.IOException;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpWinDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 aout 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpWinDataAdaptor extends Gsiftp2DataAdaptor {
    /** override super.getType() */
    public String getType() {
        return "gsiftp-win";
    }

    /** override super.getSupportedSecurityAdaptorClasses() because VOMS context type is not supported */
    public Class[] getSupportedSecurityCredentialClasses() {
        return new Class[]{GlobusSecurityCredential.class, MyProxySecurityCredential.class};
    }

    /** override super.connect() because settings data channel authentication is not supported */
    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // connect
        super.connect(userInfo, host, port, basePath, attributes);

        // not yet supported on windows implementation of gsiftp server
        try {
            m_client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
        } catch (IOException e) {
            throw new TimeoutException(e);
        } catch (ServerException e) {
            throw new AuthenticationFailedException(e);
        }
    }
}
