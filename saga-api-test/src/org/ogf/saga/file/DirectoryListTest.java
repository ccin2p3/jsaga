package org.ogf.saga.file;

import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.namespace.NSEntry;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryListTest;
import org.ogf.saga.url.URL;

/* ***************************************************
* *** Centre de Calcul de l'IN2P3 - Lyon (France) ***
* ***             http://cc.in2p3.fr/             ***
* ***************************************************
* File:   DirectoryListTest
* Author: Sylvain Reynaud (sreynaud@in2p3.fr)
* Date:   2 juil. 2007
* ***************************************************
* Description:                                      */
/**
 *
 */
public abstract class DirectoryListTest extends AbstractNSDirectoryListTest {
    protected DirectoryListTest(String protocol) throws Exception {
        super(protocol);
    }
    
    // TODO move these tests to AbstractNSDirectoryListTest when logicaldir will support this
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

}
