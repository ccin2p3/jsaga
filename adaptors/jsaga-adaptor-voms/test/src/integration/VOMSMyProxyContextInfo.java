package integration;

import org.ogf.saga.context.Context;
import org.ogf.saga.context.ContextInfoTest;

import fr.in2p3.jsaga.adaptor.security.GlobusContext;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   VOMSMyProxyContextInfo
* Author: lionel.schwarz@in2P3.fr
* Date:   12 mars 2014
* ***************************************************
* Description:                                      */
/**
 *
 */
public class VOMSMyProxyContextInfo extends ContextInfoTest {
    public VOMSMyProxyContextInfo() throws Exception {
        super("VOMSMyProxy");
    }

    @Override
    protected void updateContextAttributes(Context context) throws Exception {
        // remove DelegationLifeTime, otherwise the adaptor will retrieve the proxy from MyProxy server
        if (context.existsAttribute(GlobusContext.DELEGATIONLIFETIME)) {
            context.removeAttribute(GlobusContext.DELEGATIONLIFETIME);
        }
    }
}
