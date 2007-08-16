package ${package}.data;

import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   MyProtocolDataAdaptor
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class MyProtocolDataAdaptor implements DataAdaptor {
    public String[] getSupportedContextTypes() {
        return null;
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // set security context
    }

    public String getScheme() {
        return "myprotocol";
    }

    public String[] getSchemeAliases() {
        return new String[]{"altprotocol"};
    }

    public void connect(String userInfo, String host, int port) throws AuthenticationFailed, AuthorizationFailed, Timeout, NoSuccess {
        // connect to server
    }

    public void disconnect() throws NoSuccess {
        // disconnect from server
    }
}
