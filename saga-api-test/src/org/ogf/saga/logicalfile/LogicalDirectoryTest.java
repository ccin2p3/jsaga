package org.ogf.saga.logicalfile;

import org.ogf.saga.URI;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryTest;
import org.ogf.saga.namespace.*;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalDirectoryTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalDirectoryTest extends AbstractNSDirectoryTest {
    public LogicalDirectoryTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_openDir() throws Exception {
        NamespaceDirectory dir = m_dir.openDir(new URI(".."), Flags.NONE);
        assertTrue(
                dir instanceof LogicalDirectory);
        assertEquals(
                DEFAULT_ROOTNAME,
                dir.getName()+"/");
    }

    public void test_openEntry() throws Exception {
        NamespaceEntry entry = m_dir.open(new URI(DEFAULT_FILENAME), Flags.NONE);
        assertTrue(
                entry instanceof LogicalFile);
        assertEquals(
                DEFAULT_FILENAME,
                entry.getName());
    }
}
