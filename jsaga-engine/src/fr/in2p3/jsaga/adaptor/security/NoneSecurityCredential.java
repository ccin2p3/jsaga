package fr.in2p3.jsaga.adaptor.security;

import org.ogf.saga.error.NoSuccessException;
import org.ogf.saga.error.NotImplementedException;

import java.io.PrintStream;

/* ***************************************************
 * *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
 * ***             http://cc.in2p3.fr/             ***
 * ***************************************************
 * File:   NoneSecurityCredential
 * Author: Sylvain Reynaud (sreynaud@in2p3.fr)
 * ***************************************************
 * Description:                                      */

/**
 *
 */
public class NoneSecurityCredential implements SecurityCredential {
    public String getUserID() throws Exception {
        return "";
    }

    public String getAttribute(String key) throws NotImplementedException, NoSuccessException {
        return null;
    }

    public void close() throws Exception {
        // do nothing
    }

    public void dump(PrintStream out) throws Exception {
        // do nothing
    }
}
