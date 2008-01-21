package org.ogf.saga.file;

import org.ogf.saga.URL;
import org.ogf.saga.namespace.*;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryTest;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class DirectoryTest extends AbstractNSDirectoryTest {
    public DirectoryTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_openDir() throws Exception {
        NSDirectory dir = m_dir.openDir(new URL(".."), Flags.NONE.getValue());
        assertTrue(
                dir instanceof Directory);
        assertEquals(
                DEFAULT_ROOTNAME,
                dir.getName().getPath()+"/");
    }

    public void test_openEntry() throws Exception {
        NSEntry entry = m_dir.open(new URL(DEFAULT_FILENAME), Flags.NONE.getValue());
        assertTrue(
                entry instanceof File);
        assertEquals(
                DEFAULT_FILENAME,
                entry.getName().getPath());
    }
}
