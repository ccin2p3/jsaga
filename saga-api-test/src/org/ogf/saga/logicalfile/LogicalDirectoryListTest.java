package org.ogf.saga.logicalfile;

import org.ogf.saga.URL;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryListTest;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   LogicalDirectoryListTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public class LogicalDirectoryListTest extends AbstractNSDirectoryListTest {
    public LogicalDirectoryListTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_find() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            List list = ((LogicalDirectory)m_dir).find(DEFAULT_FILEPATTERN, null, Flags.NONE.getValue());
            assertEquals(
                    1,
                    list.size());
            assertEquals(
                    DEFAULT_FILENAME,
                    list.get(0).toString());
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }

    public void test_find_norecurse() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            LogicalDirectory root = (LogicalDirectory) m_dir.openDir(new URL(".."), Flags.NONE.getValue());
            List list = root.find(DEFAULT_FILEPATTERN, null, Flags.NONE.getValue());
            assertEquals(
                    0,
                    list.size());
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }

    public void test_find_recurse() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            LogicalDirectory root = (LogicalDirectory) m_dir.openDir(new URL(".."), Flags.NONE.getValue());
            List list = root.find(DEFAULT_FILEPATTERN, null, Flags.RECURSIVE.getValue());
            assertEquals(
                    1,
                    list.size());
            assertEquals(
                    DEFAULT_DIRNAME+DEFAULT_FILENAME,
                    list.get(0).toString());
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }

    public void test_isFile() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            assertFalse(((LogicalDirectory)m_dir).isFile(new URL(".."), Flags.NONE.getValue()));
            assertTrue(((LogicalDirectory)m_dir).isFile(new URL(DEFAULT_FILENAME), Flags.NONE.getValue()));
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }
}
