package integration;

import org.ogf.saga.context.ContextInitTest;
import org.ogf.saga.context.Context;
import fr.in2p3.jsaga.adaptor.security.VOMSContext;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxyContextRetrieve
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   26 janv. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxyContextRetrieve extends ContextInitTest {
    public VOMSMyProxyContextRetrieve() throws Exception {
        super("VOMSMyProxy", false);  // has no UserPass
    }

    protected void updateContextAttributes(Context context) throws Exception {
        if (! context.existsAttribute(VOMSContext.DELEGATIONLIFETIME)) {
        	context.setAttribute(VOMSContext.DELEGATIONLIFETIME, "PT12H");
        }
    }
}
