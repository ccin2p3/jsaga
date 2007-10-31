package org.ogf.saga.namespace.abstracts;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSEntryReadTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSEntryReadTest extends AbstractNSEntryTest {
    public AbstractNSEntryReadTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_isDirectory() throws Exception {
        assertFalse(m_file.isDir());
    }

    public void test_isEntry() throws Exception {
        assertTrue(m_file.isEntry());
    }
}
