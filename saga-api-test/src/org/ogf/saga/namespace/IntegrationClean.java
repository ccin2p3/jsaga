package org.ogf.saga.namespace;

import org.ogf.saga.namespace.abstracts.AbstractNSCopyTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   IntegrationClean
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   18 oct. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class IntegrationClean extends AbstractNSCopyTest {
    public IntegrationClean(String protocol, String targetProtocol) throws Exception {
        super(protocol, targetProtocol);
    }

    protected void setUp() throws Exception {
        // do not invoke super.setUp()
        m_toBeRemoved = true;
        m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.NONE.getValue());
        if (m_dirUrl2 != null) {
            m_dir2 = NSFactory.createNSDirectory(m_session, m_dirUrl2, Flags.NONE.getValue());
        }
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
}
