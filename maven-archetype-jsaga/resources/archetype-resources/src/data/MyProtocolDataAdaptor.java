package ${package}.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.util.Map;

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
    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectState {
        return null;
    }

    public String[] getSchemeAliases() {
        return new String[]{"myprotocol", "altprotocol"};
    }

    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;
    }

    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // set security context
    }

    public int getDefaultPort() {
        return 0;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplemented, AuthenticationFailed, AuthorizationFailed, BadParameter, Timeout, NoSuccess {
        // connect to server
    }

    public void disconnect() throws NoSuccess {
        // disconnect from server
    }
}
