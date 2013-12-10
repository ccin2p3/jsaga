package integration;

import fr.in2p3.jsaga.adaptor.cream.job.DelegationStub;

import org.glite.ce.security.delegation.DelegationServiceStub.Destroy;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   CreamExecutionDestroyDelegation
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   3 fevr. 2009
* ***************************************************
* Description:                                      */
/**
 *
 */
public class CreamExecutionDestroyDelegation extends CreamAbstractTest {

public CreamExecutionDestroyDelegation() throws Exception {
        super();
    }

    public void test_destroy() throws Exception {
        DelegationStub stub = new DelegationStub(m_url.getHost(), m_url.getPort(), DelegationStub.ANY_VO);
        Destroy destroy = new Destroy();
        destroy.setDelegationID(m_delegationId);
        stub.destroy(destroy);
    }
}
