package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.URL;
import org.ogf.saga.namespace.Flags;

import java.util.List;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryListTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class AbstractNSDirectoryListTest extends AbstractNSDirectoryTest {
    public AbstractNSDirectoryListTest(String protocol) throws Exception {
        super(protocol);
    }

    public void test_changeDir() throws Exception {
        m_dir.changeDir(new URL(".."));
        assertEquals(
                DEFAULT_ROOTNAME,
                m_dir.getName().getPath()+"/");
    }

    public void test_list() throws Exception {
        List list = m_dir.list(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_find() throws Exception {
        List list = m_dir.find(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_find_norecurse() throws Exception {
        List list = m_root.find(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                0,
                list.size());
    }

    public void test_find_recurse() throws Exception {
        List list = m_root.find(DEFAULT_FILEPATTERN, Flags.RECURSIVE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_DIRNAME+DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_getNumEntries() throws Exception {
        assertEquals(
                1,
                m_dir.getNumEntries());
    }

    public void test_getEntry() throws Exception {
        assertEquals(
                DEFAULT_FILENAME,
                m_dir.getEntry(0).toString());
    }

    /////////////////////////////////// overloaded methods ///////////////////////////////////

    public void test_isDir() throws Exception {
        assertTrue(m_dir.isDir());
    }

    public void test_isEntry() throws Exception {
        assertFalse(m_dir.isEntry());
    }
}
