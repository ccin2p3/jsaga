package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.error.DoesNotExist;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NamespaceFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryWriteTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryWriteTest extends AbstractNSEntryTest {
    public AbstractNSEntryWriteTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_remove() throws Exception {
        m_file.remove(Flags.NONE);
        try {
            NamespaceFactory.createNamespaceEntry(m_session, m_fileUri, Flags.NONE);
            fail("Expected DoesNotExist exception");
        } catch(DoesNotExist e) {
        }
    }
}
