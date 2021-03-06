package org.ogf.saga.namespace;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.error.DoesNotExistException;
import org.ogf.saga.namespace.abstracts.AbstractDataMovement;

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
public abstract class DataCleanUp extends AbstractDataMovement {
    protected DataCleanUp(String protocol) throws Exception {
        super(protocol, protocol);
    }

    @Override
    @Before
    public void setUp() throws Exception {
        // do not invoke super.setUp()
        m_toBeRemoved = true;
        m_dir = NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.NONE.getValue());
        if (m_dirUrl2 != null) {
            try {
                m_dir2 = NSFactory.createNSDirectory(m_session, m_dirUrl2, Flags.NONE.getValue());
            } catch (DoesNotExistException e) {
                Logger.getLogger(this.getClass()).warn("Directory does not exist: "+m_dirUrl2, e);
                m_dir2 = null;
            }
        }
    }

    @Test
    public void do_nothing() {}
}
