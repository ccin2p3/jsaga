package org.ogf.saga.file;

import org.junit.Test;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.namespace.Flags;
import org.ogf.saga.namespace.NSDirectory;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.abstracts.AbstractDirectory;
import org.ogf.saga.namespace.base.DirectoryBaseTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryTest
* Author: lionel.Schwarz@in2p3.fr
* Date:   5 NOV 2013
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class DirTest extends DirectoryBaseTest {
    protected DirTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_open() throws Exception {
        NSDirectory dir = m_subDir.openDir(URLFactory.createURL(".."), Flags.NONE.getValue());
        assertTrue(
                dir instanceof Directory);
        assertEquals(
                DEFAULT_DIRNAME,
                dir.getName().getPath()+"/");
        NSEntry entry = m_subDir.open(URLFactory.createURL(DEFAULT_FILENAME), Flags.NONE.getValue());
        assertTrue(
                entry instanceof File);
        assertEquals(
                DEFAULT_FILENAME,
                entry.getName().getPath());
    }

    // TODO move these tests to DirectoryBaseTest when logicaldir will support this
    
    @Test
    public void test_getSizeUrl() throws Exception {
        if (m_file instanceof File) {
            assertEquals(
                    DEFAULT_CONTENT.length(),
                    ((Directory)m_subDir).getSize(m_fileUrl));
        } else {
            fail("Not an instance of class: File");
        }
    }

    @Test
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
}
