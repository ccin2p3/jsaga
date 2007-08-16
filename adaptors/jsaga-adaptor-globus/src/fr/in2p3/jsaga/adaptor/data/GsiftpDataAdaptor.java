package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import fr.in2p3.jsaga.adaptor.security.GlobusSecurityAdaptor;
import org.ogf.saga.error.*;
import org.ietf.jgss.GSSCredential;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GsiftpDataAdaptor
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GsiftpDataAdaptor implements DataAdaptor {
    private GSSCredential m_credential;

    public String[] getSupportedContextTypes() {
        return new String[]{"Globus", "MyProxy", "VOMS"};
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        m_credential = ((GlobusSecurityAdaptor) securityAdaptor).getGSSCredential();
    }

    public String getScheme() {
        return "gsiftp";
    }

    public String[] getSchemeAliases() {
        return new String[]{"gridftp"};
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        // connect to server
    }

    public void disconnect() throws NoSuccess {
        // disconnect from server
    }
}
