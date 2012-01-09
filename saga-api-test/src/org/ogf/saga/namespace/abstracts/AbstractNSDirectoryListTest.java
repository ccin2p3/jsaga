package org.ogf.saga.namespace.abstracts;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.error.AlreadyExistsException;
import org.ogf.saga.error.NotImplementedException;
import org.ogf.saga.file.Directory;
import org.ogf.saga.file.File;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.NSFactory;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
        m_subDir.changeDir(URLFactory.createURL(".."));
        assertEquals(
                DEFAULT_DIRNAME,
                m_subDir.getName().getPath()+"/");
    }

    public void test_list() throws Exception {
        List list = m_subDir.list(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_list_empty() throws Exception {
        try {
            m_subDir.remove(DEFAULT_FILENAME);
            List list = m_subDir.list();
            assertEquals(
                    0,
                    list.size());
        } catch(NotImplementedException e) {
            // ignore this test for read-only protocols
        }
    }
    
    public void test_list_file() throws Exception {
        List list = m_subDir.list(DEFAULT_FILENAME, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_list_directories() throws Exception {
        List list = m_dir.list("*/", Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(DEFAULT_SUBDIRNAME,
                list.get(0).toString());
    }

    public void test_list_no_directory() throws Exception {
        List list = m_subDir.list("*/", Flags.NONE.getValue());
        assertEquals(
                0,
                list.size());
    }

    public void test_find() throws Exception {
        List list = m_subDir.find(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_find_norecurse() throws Exception {
        List list = m_dir.find(DEFAULT_FILEPATTERN, Flags.NONE.getValue());
        assertEquals(
                0,
                list.size());
    }

    public void test_find_recurse() throws Exception {
        List list = m_dir.find(DEFAULT_FILEPATTERN, Flags.RECURSIVE.getValue());
        assertEquals(
                1,
                list.size());
        assertEquals(
                DEFAULT_SUBDIRNAME +DEFAULT_FILENAME,
                list.get(0).toString());
    }

    public void test_getNumEntries() throws Exception {
        assertEquals(
                1,
                m_subDir.getNumEntries());
    }

    public void test_getEntry() throws Exception {
        assertEquals(
                DEFAULT_FILENAME,
                m_subDir.getEntry(0).toString());
    }

    public void test_getSizeUrl() throws Exception {
        if (m_file instanceof File) {
            assertEquals(
                    DEFAULT_CONTENT.length(),
                    ((Directory)m_subDir).getSize(m_fileUrl));
        } else {
            fail("Not an instance of class: File");
        }
    }

    public void test_getSizeRecursive() throws Exception {
    	String new_content = "new_content";
        if (m_file instanceof File) {
        	// Write another file
            URL file2Url = createURL(m_subDirUrl, "File2.txt");
            NSEntry file = m_subDir.open(file2Url, FLAGS_FILE);
            Buffer buffer = BufferFactory.createBuffer(new_content.getBytes());
            ((File)file).write(buffer);
            file.close();
            checkWrited(file2Url, new_content);
            // check size of root directory: should be size of both files in subDir
            // use SAGA compliant getSize(URL) instead of JSAGA getSize()
            assertEquals(
                    DEFAULT_CONTENT.length() + new_content.length(),
                    ((Directory)m_dir).getSize(m_dirUrl));
            // delete created file
            file.remove();
        } else {
            fail("Not an instance of class: File");
        }
        
    }

    /////////////////////////////////// overloaded methods ///////////////////////////////////

    public void test_isDir() throws Exception {
        assertTrue(m_subDir.isDir());
    }

    public void test_isEntry() throws Exception {
        assertFalse(m_subDir.isEntry());
    }
}
