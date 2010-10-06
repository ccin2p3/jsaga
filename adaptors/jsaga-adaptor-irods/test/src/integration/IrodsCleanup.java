package integration;

import org.ogf.saga.namespace.IntegrationClean;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   SRMCleanup
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   20 avr. 2009
* ***************************************************
* Description:                                      */

/**
 *
 */
public class IrodsCleanup extends IntegrationClean {
    public IrodsCleanup() throws Exception {
        super("irods", "irods");
    }

    public void test_dummy() {}
}
