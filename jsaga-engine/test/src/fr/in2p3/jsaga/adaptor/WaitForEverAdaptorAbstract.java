package fr.in2p3.jsaga.adaptor;

import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.data.BaseURL;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.util.Map;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   WaitForEverAdaptorAbstract
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   28 mai 2009
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public abstract class WaitForEverAdaptorAbstract implements ClientAdaptor {
    private static boolean s_isHanging = false;

    public Usage getUsage() {
        return null;
    }
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {
        return null;
    }
    public BaseURL getBaseURL() throws IncorrectURLException {
        return null;
    }
    public Class[] getSupportedSecurityAdaptorClasses() {
        return null;
    }
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {
        // do nothing
    }

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        mayHang(attributes);
    }
    public void disconnect() throws NoSuccessException {
        // do nothing
    }

    public static void hang() {
        try {
            // set isHanging
            s_isHanging = true;

            // hang
            for (;;) {
                Thread.currentThread().sleep(100);
            }
        } catch (InterruptedException e) {/*ignore*/}
    }
    public static boolean isHanging() {
        // save result
        boolean ret = s_isHanging;

        // reset
        s_isHanging = false;

        // return
        return ret;
    }
    protected static void mayHang(Map attributes) {
        if (attributes.containsKey("hangatconnect")) {
            hang();
        }
    }
    protected static void mayHang(String additionalArgs) {
        if ("hangatconnect".equals(additionalArgs)) {
            hang();
        }
    }
}
