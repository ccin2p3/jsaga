package org.ogf.saga.logicalfile;

import org.ogf.saga.namespace.*;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryTest;
import org.ogf.saga.url.URLFactory;

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
public abstract class LogicalDirectoryTest extends AbstractNSDirectoryTest {
    protected LogicalDirectoryTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_openDir() throws Exception {
        NSDirectory dir = m_subDir.openDir(URLFactory.createURL(".."), Flags.NONE.getValue());
        assertTrue(
                dir instanceof LogicalDirectory);
        assertEquals(
                DEFAULT_DIRNAME,
                dir.getName().getPath()+"/");
    }

    public void test_openEntry() throws Exception {
        NSEntry entry = m_subDir.open(URLFactory.createURL(DEFAULT_FILENAME), Flags.NONE.getValue());
        assertTrue(
                entry instanceof LogicalFile);
        assertEquals(
                DEFAULT_FILENAME,
                entry.getName().getPath());
    }
}
