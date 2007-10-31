package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryMakeTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryMakeTest extends AbstractNSDirectoryTest {
    public AbstractNSDirectoryMakeTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_remove() throws Exception {
        m_dir.remove(Flags.RECURSIVE.getValue());
        try {
            NSFactory.createNSDirectory(m_session, m_dirUrl, Flags.NONE.getValue());
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }
}
