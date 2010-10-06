package integration;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextInitTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   MyProxyContextInit
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   16 sept. 2008
* ***************************************************
* Description:                                      */
/**
 *
 */
public class MyProxyContextInit extends ContextInitTest {
    public MyProxyContextInit() throws Exception {
        super("MyProxy", true);    // has UserPass
    }

    protected void updateContextAttributes(Context context) throws Exception {
        context.setAttribute(Context.LIFETIME, "P7D");
    }
}
