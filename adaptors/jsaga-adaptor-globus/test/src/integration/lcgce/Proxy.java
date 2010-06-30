package integration.lcgce;

import org.globus.common.CoGProperties;
import org.gridforum.jgss.ExtendedGSSCredential;
import org.gridforum.jgss.ExtendedGSSManager;
import org.ietf.jgss.GSSCredential;
import org.ietf.jgss.GSSException;

import java.io.*;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   Proxy
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * Date:   25 juin 2010
 * ***************************************************
 * Description:                                      */
/**
 *
 */
public class Proxy {
    public static GSSCredential get() throws IOException, GSSException {
        // set parameters
        String CERTREPOSITORY = System.getProperty("user.home")+"/.globus/certificates/";
        String USERPROXY = System.getProperty("user.home")+"/.jsaga/tmp/dteam_cred.txt";
        CoGProperties.getDefault().setCaCertLocations(CERTREPOSITORY);
        File proxyFile = new File(USERPROXY);

        // load proxy
        byte [] proxyBytes = new byte[(int) proxyFile.length()];
        FileInputStream in = new FileInputStream(proxyFile);
        in.read(proxyBytes);
        in.close();
        ExtendedGSSManager manager = (ExtendedGSSManager) ExtendedGSSManager.getInstance();
        return manager.createCredential(
                proxyBytes,
                ExtendedGSSCredential.IMPEXP_OPAQUE,
                GSSCredential.DEFAULT_LIFETIME,
                null, // use default mechanism: GSI
                GSSCredential.INITIATE_AND_ACCEPT);
    }
}
