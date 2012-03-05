package ${package}.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.DataAdaptor;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
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
    public String getType() {
        return "myprotocol";
    }

    public Usage getUsage() {
        return null;
    }

    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }

    public Class[] getSupportedSecurityCredentialClasses() {
        return null;
    }

    public void setSecurityCredential(SecurityCredential credential) {
        // set security credential
    }

    public int getDefaultPort() {
        return 0;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, IncorrectURLException, BadParameterException, TimeoutException, NoSuccessException {
        // connect to server
    }

    public void disconnect() throws NoSuccessException {
        // disconnect from server
    }
}
