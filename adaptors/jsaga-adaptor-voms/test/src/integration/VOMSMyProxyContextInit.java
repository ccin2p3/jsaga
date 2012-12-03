package integration;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextInitTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxyContextInit
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxyContextInit extends ContextInitTest {
    public VOMSMyProxyContextInit() throws Exception {
        super("VOMSMyProxy", true);    // has UserPass
    }

    protected void updateContextAttributes(Context context) throws Exception {
        context.setAttribute(Context.LIFETIME, "P1D");
    }
}
