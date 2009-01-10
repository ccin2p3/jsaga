package org.ogf.saga.logicalfile;

import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryListTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
public abstract class LogicalDirectoryListTest extends AbstractNSDirectoryListTest {
    protected LogicalDirectoryListTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_find() throws Exception {
        if (m_subDir instanceof LogicalDirectory) {
            List<URL> list = ((LogicalDirectory) m_subDir).find(DEFAULT_FILEPATTERN, null, Flags.NONE.getValue());
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
            List<URL> list = ((LogicalDirectory) m_dir).find(DEFAULT_FILEPATTERN, null, Flags.NONE.getValue());
            assertEquals(
                    0,
                    list.size());
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }

    public void test_find_recurse() throws Exception {
        if (m_dir instanceof LogicalDirectory) {
            List<URL> list = ((LogicalDirectory) m_dir).find(DEFAULT_FILEPATTERN, null, Flags.RECURSIVE.getValue());
            assertEquals(
                    1,
                    list.size());
            assertEquals(
                    DEFAULT_SUBDIRNAME +DEFAULT_FILENAME,
                    list.get(0).toString());
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }

    public void test_isFile() throws Exception {
        if (m_subDir instanceof LogicalDirectory) {
            assertFalse(((LogicalDirectory) m_subDir).isFile(URLFactory.createURL("..")));
            assertTrue(((LogicalDirectory) m_subDir).isFile(URLFactory.createURL(DEFAULT_FILENAME)));
        } else {
            fail("Not an instance of class: LogicalDirectory");
        }
    }
}
