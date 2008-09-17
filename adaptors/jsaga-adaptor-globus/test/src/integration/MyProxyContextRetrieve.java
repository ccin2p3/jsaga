package integration;

import fr.in2p3.jsaga.adaptor.security.GlobusContext;
import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextInitTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxyContextRetrieve
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxyContextRetrieve extends ContextInitTest {
    public MyProxyContextRetrieve() throws Exception {
        super("EGEE", false);   // has no UserPass
    }

    protected void updateContextAttributes(Context context) throws Exception {
        context.setAttribute(GlobusContext.DELEGATIONLIFETIME, "PT12H");
    }
}
