package fr.in2p3.jsaga.adaptor.job;

import fr.in2p3.jsaga.Base;
import fr.in2p3.jsaga.adaptor.ClientAdaptor;
import fr.in2p3.jsaga.adaptor.base.defaults.Default;
import fr.in2p3.jsaga.adaptor.base.usage.Usage;
import fr.in2p3.jsaga.adaptor.security.SecurityAdaptor;
import org.ogf.saga.error.*;

import java.io.File;
import java.util.Map;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   EmulatorJobAdaptorAbstract
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   27 oct. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class EmulatorJobAdaptorAbstract implements ClientAdaptor {
    private static final File STATUS_DIR = new File(Base.JSAGA_VAR, "JobEmulator");

    public String getType() {
        return "test";
    }

    public Usage getUsage() {return null;}
    public Default[] getDefaults(Map attributes) throws IncorrectStateException {return null;}
    public Class[] getSupportedSecurityAdaptorClasses() {return null;}
    public void setSecurityAdaptor(SecurityAdaptor securityAdaptor) {}

    public void connect(String userInfo, String host, int port, String basePath, Map attributes) throws NotImplementedException, AuthenticationFailedException, AuthorizationFailedException, BadParameterException, TimeoutException, NoSuccessException {
        if (! STATUS_DIR.exists()) {
            STATUS_DIR.mkdir();
        }
    }

    public void disconnect() throws NoSuccessException {
        if (STATUS_DIR.list().length == 0) {
            STATUS_DIR.delete();
        }
    }

    protected File getJob(String nativeJobId) {
        return new File(STATUS_DIR, nativeJobId);
    }
}
