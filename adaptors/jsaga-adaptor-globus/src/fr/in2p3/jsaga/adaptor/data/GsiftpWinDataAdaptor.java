package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.security.GlobusSecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.MyProxySecurityAdaptor;
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
* Date:   20 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpWinDataAdaptor extends Gsiftp2DataAdaptor {
    /** override super.getSchemeAliases() */
    public String[] getSchemeAliases() {
        return new String[]{"gsiftp-win", "gridftp-win"};
    }

    /** override super.getSupportedSecurityAdaptorClasses() because VOMS context type is not supported */
    public Class[] getSupportedSecurityAdaptorClasses() {
        return new Class[]{GlobusSecurityAdaptor.class, MyProxySecurityAdaptor.class};
    }

    /** override super.connect() because settings data channel authentication is not supported */
    public void connect(String userInfo, String host, int port, Map attributes) throws AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // connect
        super.connect(userInfo, host, port, attributes);

        // not yet supported on windows implementation of gsiftp server
        try {
            m_client.setDataChannelAuthentication(DataChannelAuthentication.NONE);
        } catch (IOException e) {
            throw new Timeout(e);
        } catch (ServerException e) {
            throw new AuthenticationFailed(e);
        }
    }
}
