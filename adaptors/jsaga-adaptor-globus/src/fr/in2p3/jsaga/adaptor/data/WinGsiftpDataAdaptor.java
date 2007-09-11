package fr.in2p3.jsaga.adaptor.data;

import org.globus.ftp.DataChannelAuthentication;
import org.globus.ftp.exception.ServerException;
import org.ogf.saga.error.*;

import java.io.IOException;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   WinGsiftpDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 août 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class WinGsiftpDataAdaptor extends GsiftpDataAdaptor {
    public String[] getSupportedContextTypes() {
        return new String[]{"Globus"};
    }

    public String getScheme() {
        return "gsiftp-win";
    }

    public String[] getSchemeAliases() {
        return new String[]{"gridftp-win"};
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        // connect
        super.connect(userInfo, host, port);

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
