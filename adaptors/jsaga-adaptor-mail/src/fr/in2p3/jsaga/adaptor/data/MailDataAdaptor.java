package fr.in2p3.jsaga.adaptor.data;

import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   MailDataAdaptor
 * Author:
 * Date:
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class MailDataAdaptor implements DataAdaptor {
    public String getType() {
        return "mail";
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
        // set security context
    }

    public int getDefaultPort() {
        return NO_PORT;
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        // connect to server
    }

    public void disconnect() throws NoSuccessException {
        // disconnect from server
    }
}
