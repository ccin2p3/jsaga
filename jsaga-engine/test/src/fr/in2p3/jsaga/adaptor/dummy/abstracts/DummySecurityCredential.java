package fr.in2p3.jsaga.adaptor.dummy.abstracts;

import fr.in2p3.jsaga.adaptor.security.SecurityCredential;
import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.PrintStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   DummySecurityCredential
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class DummySecurityCredential implements SecurityCredential {
    public String getUserID() throws Exception {
        return "me";
    }

    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
    	throw new NotImplementedException();
    }

    public void close() throws Exception {
    }

    public void dump(PrintStream out) throws Exception {
    }
}
