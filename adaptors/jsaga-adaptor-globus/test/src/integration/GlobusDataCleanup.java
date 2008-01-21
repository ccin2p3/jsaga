package integration;

import org.ogf.saga.namespace.IntegrationClean;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   GlobusIntegrationClean
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class GlobusDataCleanup extends IntegrationClean {
    public GlobusDataCleanup() throws Exception {
        super("gsiftp", "gsiftp");
    }

    public void test_dummy() {}
}
