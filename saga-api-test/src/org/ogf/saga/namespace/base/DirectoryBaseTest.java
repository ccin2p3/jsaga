package org.ogf.saga.namespace.base;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.abstracts.AbstractDirectory;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   AbstractNSDirectoryTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class DirectoryBaseTest extends AbstractDirectory {

    public DirectoryBaseTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_changeDir() throws Exception {
        m_subDir.changeDir(URLFactory.createURL(".."));
        assertEquals(
                DEFAULT_DIRNAME,
                m_subDir.getName().getPath()+"/");
    }

    @Test
    public void test_list() throws Exception {
        List list = m_subDir.list(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    @Test
    public void test_list_empty() throws Exception {
        m_subDir.remove(DEFAULT_FILENAME);
        List list = m_subDir.list();
        assertEquals(
                0,
                list.size());
    }
    
    @Test
    public void test_list_file() throws Exception {
        List list = m_subDir.list(DEFAULT_FILENAME, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    @Test
    public void test_list_directories() throws Exception {
        List list = m_dir.list("*/", Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(DEFAULT_SUBDIRNAME,
                list.get(0).toString());
        list = m_subDir.list("*/", Flags.NONE.getValue());
        assertEquals(
                0,
                list.size());
    }

    @Test
    public void test_find() throws Exception {
        List list = m_subDir.find(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
        list = m_dir.find(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                0,
                list.size());
    }

    @Test
    public void test_find_recurse() throws Exception {
        List list = m_dir.find(DEFAULT_FILEPATTERN, Flags.RECURSIVE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_SUBDIRNAME +DEFAULT_FILENAME,
                list.get(0).toString());
    }

    @Test
    public void test_getNumEntries() throws Exception {
    	assertEquals(
                1,
                m_subDir.getNumEntries());
    }

    @Test
    public void test_getEntry() throws Exception {
    	assertEquals(
                DEFAULT_FILENAME,
                m_subDir.getEntry(0).toString());
    }

    /////////////////////////////////// overloaded methods ///////////////////////////////////

    @Test
    public void test_isDir() throws Exception {
    	assertTrue(m_subDir.isDir());
    }

    @Test
    public void test_isEntry() throws Exception {
    	assertFalse(m_subDir.isEntry());
    }
}
