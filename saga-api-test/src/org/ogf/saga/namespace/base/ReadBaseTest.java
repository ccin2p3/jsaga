package org.ogf.saga.namespace.base;

import org.junit.Test;

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
public abstract class ReadBaseTest extends DataBaseTest {
    public ReadBaseTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_isDirectory() throws Exception {
        assertFalse(m_file.isDir());
    }

    @Test
    public void test_isEntry() throws Exception {
    	assertTrue(m_file.isEntry());
    }
}
