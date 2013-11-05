package org.ogf.saga.file;

import org.junit.Assert;
import org.junit.Test;
import org.ogf.saga.buffer.Buffer;
import org.ogf.saga.buffer.BufferFactory;
import org.ogf.saga.namespace.*;
import org.ogf.saga.namespace.abstracts.AbstractNSDirectoryTest;
import org.ogf.saga.namespace.base.DirBaseTest;
import org.ogf.saga.url.URL;
import org.ogf.saga.url.URLFactory;

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
public abstract class DirTest extends DirBaseTest {
    protected DirTest(String protocol) throws Exception {
        super(protocol);
    }

    @Test
    public void test_open() throws Exception {
        NSDirectory dir = m_subDir.openDir(URLFactory.createURL(".."), Flags.NONE.getValue());
        Assert.assertTrue(
                dir instanceof Directory);
        Assert.assertEquals(
                DEFAULT_DIRNAME,
                dir.getName().getPath()+"/");
        NSEntry entry = m_subDir.open(URLFactory.createURL(DEFAULT_FILENAME), Flags.NONE.getValue());
        Assert.assertTrue(
                entry instanceof File);
        Assert.assertEquals(
                DEFAULT_FILENAME,
                entry.getName().getPath());
    }

    // TODO move these tests to DirectoryBaseTest when logicaldir will support this
    
    @Test
    public void test_getSizeUrl() throws Exception {
        if (m_file instanceof File) {
            Assert.assertEquals(
                    DEFAULT_CONTENT.length(),
                    ((Directory)m_subDir).getSize(m_fileUrl));
        } else {
            Assert.fail("Not an instance of class: File");
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
            Assert.assertEquals(
                    DEFAULT_CONTENT.length() + new_content.length(),
                    ((Directory)m_dir).getSize(m_dirUrl));
            // delete created file
            file.remove();
        } else {
            Assert.fail("Not an instance of class: File");
        }
        
    }
}
